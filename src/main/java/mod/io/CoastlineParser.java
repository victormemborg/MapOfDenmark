package mod.io;

import mod.osm.OsmBounds;
import mod.osm.OsmNode;
import mod.osm.OsmTag;
import mod.osm.OsmWay;
import mod.view.StyleAttributes;
import org.openstreetmap.osmosis.core.container.v0_6.*;
import org.openstreetmap.osmosis.core.domain.v0_6.*;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.xml.v0_6.XmlReader;

import java.io.*;
import java.util.*;

import static mod.osm.OsmTag.Key.COASTLINE;

public class CoastlineParser {
    public void parse(File file) {
        Sink sink = new Sink() {
            HashMap<Long, OsmNode> nodes;
            OsmBounds bounds;
            Set<OsmWay> coastlines;
            Map<OsmNode, OsmWay> startNode;

            @Override
            public void initialize(Map<String, Object> metaData) {
                this.nodes = new HashMap<>();
                this.coastlines = new HashSet<>();
                this.startNode = new HashMap<>();
            }

            @Override
            public void process(EntityContainer entityContainer) {
                if (entityContainer instanceof NodeContainer) {
                    Node node = ((NodeContainer) entityContainer).getEntity();
                    handleNode(node);
                } else if (entityContainer instanceof WayContainer) {
                    Way way = ((WayContainer) entityContainer).getEntity();
                    handleWay(way);
                }
            }

            @Override
            public void complete() {
                Map<Integer, List<OsmNode>> closedShapes = this.mergeCoastlines();
                List<OsmWay> coasts = new ArrayList<>();

                for (Integer id : closedShapes.keySet()) {
                    List<OsmNode> wayNodes = closedShapes.get(id);
                    OsmNode[] nodeArray = wayNodes.toArray(new OsmNode[0]);

                    OsmWay coast = new OsmWay(nodeArray, new OsmTag[]{new OsmTag(COASTLINE, null)});
                    coasts.add(coast);
                }

                try {
                    FileOutputStream fos = new FileOutputStream("src/main/resources/mod/data/coastlines.coast");
                    ObjectOutputStream oop = new ObjectOutputStream(fos);
                    oop.writeObject(coasts);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                this.nodes = null;
                this.bounds = null;
                this.coastlines = null;
            }

            @Override
            public void close() {
                System.gc();
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

                OsmNode newNode = new OsmNode(nodeLat, nodeLon, nodeTags);
                this.nodes.put(nodeId, newNode);
            }

            private void handleWay(Way way) {
                OsmTag[] wayTags = this.parseTags(way.getTags());
                if (StyleAttributes.getAttributes(wayTags) == null) {
                    return;
                }

                if(!containsCoastline(wayTags)) {
                    return;
                }

                List<WayNode> rawWayNodes = way.getWayNodes();
                OsmNode[] wayNodes = new OsmNode[rawWayNodes.size()];

                for (int i = 0; i < wayNodes.length; i++) {
                    long nodeId = rawWayNodes.get(i).getNodeId();
                    OsmNode node = this.nodes.get(nodeId);
                    wayNodes[i] = this.nodes.put(nodeId, node);
                }

                OsmWay newCoastline = new OsmWay(wayNodes, null);

                this.startNode.put(wayNodes[0], newCoastline);
                this.coastlines.add(newCoastline);
            }

            private Map<Integer, List<OsmNode>> mergeCoastlines() {
                Map<Integer, List<OsmNode>> closedShapes = new HashMap<>();
                int id = -1;

                while (!coastlines.isEmpty()) {
                    closedShapes.put(++id, new ArrayList<>());

                    OsmWay current = coastlines.iterator().next();
                    OsmWay first = current;
                    boolean firstPass = true;

                    while ( (current != null && current != first) || firstPass) {
                        firstPass = false;

                        OsmNode[] coastNodes = current.getNodes();
                        List<OsmNode> nodesList = Arrays.asList(coastNodes);

                        closedShapes.get(id).addAll(nodesList);
                        coastlines.remove(current);

                        OsmNode lastNode = coastNodes[coastNodes.length - 1];
                        current = startNode.get(lastNode);
                    }
                }

                return closedShapes;
            }

            private boolean containsCoastline(OsmTag[] wayTags) {
                for (OsmTag tag : wayTags) {
                    if (tag.getValue().equals("coastline")) {
                        return true;
                    }
                }
                return false;
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

        XmlReader xmlReader = new XmlReader(file, false, CompressionMethod.None);
        xmlReader.setSink(sink);
        xmlReader.run();
    }

    public static void main(String[] args) {
        CoastlineParser coastParser = new CoastlineParser();
        File file = new File("../OsmFiles/denmarkOSM.osm"); // Some local path to *entirety* of denmark
        coastParser.parse(file);
    }
}