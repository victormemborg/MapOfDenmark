package mod.pathfinding;

import mod.osm.OsmEntity;
import mod.osm.OsmNode;
import mod.osm.OsmTag;
import mod.utils.Distance;
import mod.utils.Transport;

import java.io.Serializable;
import java.util.*;

public class Dijkstra implements Serializable {
    private final Digraph graph;
    private Set<OsmNode> settled;
    private PriorityQueue<Node> queue;
    private Map<OsmNode, Float> timeTo;
    private Map<OsmNode, Edge> edgeTo;
    private Transport mode;
    private Set<Edge> considered;


    public Dijkstra(Digraph graph) {
        this.graph = graph;
    }

    @SuppressWarnings("uncheked")
    public static <T extends OsmEntity> Direction getDirection(T element) {
        Direction direction = null;

        for (OsmTag tag : element.getTags()) {
            if (direction != null) {
                break;
            }

            direction = switch (tag.getKey()) {
                case ONEWAY -> switch (tag.getValue()) {
                    case "yes", "true", "1" -> Direction.SINGLE;
                    case "no", "false", "0" -> Direction.BOTH;
                    default -> Direction.REVERSE;
                };
                case JUNCTION -> switch (tag.getValue()) {
                    case "roundabout" -> Direction.SINGLE;
                    default -> Direction.BOTH;
                };
                default -> null;
            };
        }

        return direction;
    }

    public List<OsmNode> getShortestPath(OsmNode from, OsmNode to, Transport mode) {
        this.settled = new HashSet<>();
        this.queue = new PriorityQueue<>();
        this.timeTo = new HashMap<>();
        this.edgeTo = new HashMap<>();
        this.mode = mode;
        this.considered = new HashSet<>();

        queue.add(new Node(from, 0f));
        timeTo.put(from, 0f);

        while (!queue.isEmpty()) {
            OsmNode current = queue.poll().node();

            if (current == to) break;
            if (settled.contains(current)) continue;

            settled.add(current);
            relax(current, to);
        }

        if (edgeTo.get(to) == null) {
            throw new RuntimeException(
                    "There exists no path between the chosen locations. \n" +
                    "Verify that you have chosen the correct addresses and transport mode"
            );
        }

        LinkedList<OsmNode> path = new LinkedList<>();
        path.add(to);

        OsmNode current = path.getLast();
        while(current != from) {
            path.add(edgeTo.get(current).from());
            current = path.getLast();
        }
        path.add(from);

        this.settled = null;
        this.queue = null;
        this.timeTo = null;
        this.edgeTo = null;
        this.mode = null;

        return path;
    }


    private void relax(OsmNode vertex, OsmNode target) {
        Set<Edge> adjacentEdges = graph.getAdjacentTo(vertex);
        if (adjacentEdges == null) return;

        for (Edge edge : adjacentEdges) {
            if (!edge.allowsTransport(mode)) continue;
            considered.add(edge);

            OsmNode adjVertex = edge.to();
            short speed = mode == Transport.CAR ? edge.way().getSpeedLimit() : 1;

            float edgeTime = (float) Distance.screenDistanceBetween(vertex, adjVertex) / speed;
            float totalTime = timeTo.get(vertex) + edgeTime;

            float bestTime = timeTo.computeIfAbsent(adjVertex, v -> Float.MAX_VALUE);

            if (totalTime < bestTime) {
                timeTo.put(adjVertex, totalTime);
                edgeTo.put(adjVertex, edge);
            }

            if (settled.contains(adjVertex)) continue;

            float weight = timeTo.get(adjVertex) + heuristics(adjVertex, target, speed);
            queue.add(new Node(adjVertex, weight));
        }
    }

    private float heuristics(OsmNode vertex, OsmNode target, short speed) {
        float distToTarget = (float) Distance.screenDistanceBetween(vertex, target);

        return distToTarget / speed;
    }

    public enum Direction implements Serializable {
        SINGLE, BOTH, REVERSE
    }

    public record Node(OsmNode node, float weight) implements Comparable<Node>, Serializable {
        @Override
        public int compareTo(Node that) {
            return Float.compare(this.weight(), that.weight());
        }
    }

    public Set<Edge> getConsidered() {
        return this.considered;
    }
}
