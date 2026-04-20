package mod.io;

import javafx.application.Platform;
import mod.core.Controller;
import mod.osm.OsmBounds;
import mod.osm.OsmNode;
import mod.osm.OsmTag;
import mod.osm.OsmWay;
import mod.pathfinding.AddressTrie;
import mod.pathfinding.Digraph;
import mod.pathfinding.Dijkstra;
import mod.pathfinding.Edge;
import mod.renderer.LevelOfDetail;
import mod.renderer.MapData;
import mod.tree.KdTree;
import mod.utils.Address;
import mod.utils.Transport;
import mod.view.StyleAttributes;
import org.openstreetmap.osmosis.core.container.v0_6.*;
import org.openstreetmap.osmosis.core.domain.v0_6.*;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.xml.v0_6.XmlReader;

import java.io.*;
import java.util.*;

public class OsmParser {
    MapData mapData;

    public MapData parse(File file) {
        return this.parse(file, false);
    }

    // isTest is a flag to prevent UI updates during testing
    // could've used Platform.isFxApplicationThread(), but our app is on a different thread
    public MapData parse(File file, boolean isTest) {
        Sink sink = new Sink() {
            HashMap<Long, OsmNode> nodes;
            OsmBounds bounds;
            AddressTrie addressTrie;
            List<OsmWay>[] ways;
            Set<OsmWay> highways; // might not need to be a Set

            @Override
            public void initialize(Map<String, Object> metaData) {
                this.nodes = new HashMap<>();
                this.addressTrie = new AddressTrie();
                this.ways = new ArrayList[LevelOfDetail.values().length];
                this.highways = new HashSet<>();

                for (int i = 0; i < ways.length; i++) {
                    ways[i] = new ArrayList<>();
                }
            }

            @Override
            public void process(EntityContainer entityContainer) {
                if (entityContainer instanceof NodeContainer) {
                    Node node = ((NodeContainer) entityContainer).getEntity();
                    handleNode(node);
                    setProgress(0.0, isTest);
                } else if (entityContainer instanceof WayContainer) {
                    Way way = ((WayContainer) entityContainer).getEntity();
                    handleWay(way);
                    setProgress(0.2, isTest);
                } else if (entityContainer instanceof RelationContainer) {
                    Relation relation = ((RelationContainer) entityContainer).getEntity();
                    handleRelation(relation);
                    setProgress(0.35, isTest);
                } else if (entityContainer instanceof BoundContainer) {
                    Bound bound = ((BoundContainer) entityContainer).getEntity();
                    handleBound(bound);
                }
            }

            @Override
            public void complete() {
                KdTree<OsmWay>[] lodTrees = new KdTree[LevelOfDetail.values().length];
                for (int i = 0; i < ways.length; i++) {
                    KdTree<OsmWay> lodTree = new KdTree<>(4, this.ways[i]);
                    lodTrees[i] = lodTree;
                }

                // creating highway digraph. This should be done in handleWay(), on a way per way basis, so we don't need
                // an ever-growing set of OsmWays constantly in memory as we are parsing. But I wanted to make the logic
                // obvious, for now. This implementation does not take different transportation methods into account.
                setProgress(0.5, isTest);
                Digraph graph = new Digraph();

                for (OsmWay highway : highways) {
                    Dijkstra.Direction direction = Dijkstra.getDirection(highway);

                    OsmNode[] nodes = highway.getNodes();
                    OsmNode lastNode = nodes[0];

                    for (int i = 1; i < nodes.length; i++) {
                        // We can sacrifice resolution/accuracy for better memory consumption
                        // if we only consider junction-nodes. But let's wait and see.
                        OsmNode currentNode = nodes[i];

                        if (direction == Dijkstra.Direction.SINGLE) {
                            Edge edge = new Edge(lastNode, currentNode, highway);
                            graph.addEdge(edge);
                        } else if (direction == Dijkstra.Direction.REVERSE) {
                            Edge edge = new Edge(currentNode, lastNode, highway);
                            graph.addEdge(edge);
                        } else { // default to both directions
                            Edge edge1 = new Edge(lastNode, currentNode, highway);
                            Edge edge2 = new Edge(currentNode, lastNode, highway);
                            graph.addEdge(edge1);
                            graph.addEdge(edge2);
                        }

                        lastNode = currentNode;
                    }
                }

                setProgress(0.7, isTest);
                Dijkstra dijkstra = new Dijkstra(graph);

                // Setting the nearest highway-node for all addresses
                List<OsmNode> highwayNodes = new ArrayList<>();
                for (OsmWay highway : highways) {
                    // Only accept highways that allows for all transport options
                    if (!(Transport.from(highway).length == 3)) continue;
                    highwayNodes.addAll(Arrays.asList(highway.getNodes()));
                }

                KdTree<OsmNode> highwayTree = new KdTree<>(2, highwayNodes);
                Set<Address> addresses = addressTrie.getAllAddresses();

                for (Address addr : addresses) {
                    OsmNode nearestHighway = highwayTree.findNearest(addr.getNode());
                    addr.setNearestHighway(nearestHighway);
                }

                List<OsmWay> coastlines = null;
                try {
                    InputStream is = OsmParser.class.getResourceAsStream("/mod/data/coastlines.coast");
                    ObjectInputStream ois = new ObjectInputStream(is);
                    coastlines = (List<OsmWay>) ois.readObject();

                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }

                // Initializing mapData
                mapData = new MapData(this.bounds, this.addressTrie, lodTrees, dijkstra, coastlines);

                this.nodes = null;
                this.bounds = null;
                this.addressTrie = null;
                this.ways = null;
                this.highways = null;
            }

            @Override
            public void close() {
                System.gc();
                setProgress(0.8, isTest);
            }

            private void handleNode(Node node) {
                long nodeId = node.getId();
                double nodeLat = node.getLatitude();
                double nodeLon = node.getLongitude();

                OsmTag[] nodeTags = this.parseTags(node.getTags());

                if (nodeTags == null) {
                    nodes.put(nodeId, new OsmNode(nodeLat, nodeLon, null));
                    return;
                }

                HashMap<OsmTag.Key, String> address = new HashMap<>();
                for (OsmTag tag : nodeTags) {
                    switch (tag.getKey()) {
                        case HOUSE_NUMBER -> address.put(OsmTag.Key.HOUSE_NUMBER, tag.getValue());
                        case STREET -> address.put(OsmTag.Key.STREET, tag.getValue());
                        case POSTAL_CODE -> address.put(OsmTag.Key.POSTAL_CODE, tag.getValue());
                        case CITY -> address.put(OsmTag.Key.CITY, tag.getValue());
                    }
                }

                OsmNode newNode = new OsmNode(nodeLat, nodeLon, nodeTags);

                if (address.get(OsmTag.Key.HOUSE_NUMBER) != null) {
                    String street = address.get(OsmTag.Key.STREET);
                    String houseNumber = address.get(OsmTag.Key.HOUSE_NUMBER);
                    String postcode = address.get(OsmTag.Key.POSTAL_CODE);
                    String city = address.get(OsmTag.Key.CITY);
                    this.addressTrie.add(new Address(newNode, street, houseNumber, postcode, city));
                }

                this.nodes.put(nodeId, newNode);
            }

            private void handleWay(Way way) {
                OsmTag[] wayTags = this.parseTags(way.getTags());
                if (StyleAttributes.getAttributes(wayTags) == null || containsCoastline(wayTags)) {
                    return;
                }

                List<WayNode> rawWayNodes = way.getWayNodes();
                OsmNode[] wayNodes = new OsmNode[rawWayNodes.size()];

                for (int i = 0; i < wayNodes.length; i++) {
                    long nodeId = rawWayNodes.get(i).getNodeId();
                    OsmNode node = this.nodes.get(nodeId);
                    wayNodes[i] = node;
                }

                for (OsmTag tag : wayTags) {
                    LevelOfDetail levelOfDetail = OsmTag.getLevelOfDetail(tag);

                    if (levelOfDetail == null) {
                        continue;
                    }

                    OsmWay newWay = new OsmWay(wayNodes, wayTags);
                    this.ways[levelOfDetail.into()].add(newWay);

                    if (tag.getKey() == OsmTag.Key.HIGHWAY) {
                        highways.add(newWay);
                    }
                }
            }

            private boolean containsCoastline(OsmTag[] arr) {
                for (OsmTag tag : arr) {
                    if (tag.getValue().equals("coastline")) {
                        return true;
                    }
                }
                return false;
            }

            private void handleRelation(Relation relation) {
            }

            private void handleBound(Bound bound) {
                double minLat = bound.getLeft();
                double maxLat = bound.getRight();
                double minLon = bound.getBottom();
                double maxLon = bound.getTop();
                this.bounds = new OsmBounds(minLat, maxLat, minLon, maxLon);
            }

            private OsmTag[] parseTags(Collection<Tag> allTags) {
                List<OsmTag> filteredTags = allTags.stream().map(tag -> {
                    OsmTag.Key key = OsmTag.Key.from(tag.getKey());
                    if (key == null) return null;
                    return OsmTag.from(tag.getKey(), tag.getValue());
                }).filter(Objects::nonNull).toList();

                if (filteredTags.isEmpty()) {
                    return null;
                }

                OsmTag[] tags = new OsmTag[filteredTags.size()];
                for (int i = 0; i < filteredTags.size(); i++) {
                    tags[i] = filteredTags.get(i);
                }

                return tags;
            }
        };

        String extension = file.getName().substring(file.getName().lastIndexOf(".") + 1);

        CompressionMethod compressionMethod = switch (extension) {
            case "gz" -> CompressionMethod.GZip;
            case "bz2" -> CompressionMethod.BZip2;
            default -> CompressionMethod.None;
        };

        XmlReader xmlReader = new XmlReader(file, false, compressionMethod);
        xmlReader.setSink(sink);
        xmlReader.run();

        return this.mapData;
    }

    public void cleanup() {
        // Delete the reference to mapData from the parser.
        // This makes the garbage collector able the remove the
        // reference to mapData from memory.
        this.mapData = null;
    }

    public void setProgress(double progress, boolean isTest) {
        if(isTest) return;
        Controller.setProgressBarProgress(progress);
    }
}
