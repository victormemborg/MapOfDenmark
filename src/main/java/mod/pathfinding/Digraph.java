package mod.pathfinding;

import mod.osm.OsmNode;

import java.io.Serializable;
import java.util.*;

public class Digraph implements Serializable {
    private final Map<OsmNode, Set<Edge>> adjacent; // vertex -> adjacent vertices
    private int edges;

    public Digraph() {
        this.adjacent = new HashMap<>();
    }

    public Set<Edge> getAdjacentTo(OsmNode vertex) { // returns null if no adjacent vertices
        return this.adjacent.get(vertex);
    }

    public void addEdge(Edge edge) {
        OsmNode from = edge.from();

        if (!this.adjacent.containsKey(from)) {
            this.adjacent.put(from, new HashSet<>());
        }

        Set<Edge> adjToFrom = this.adjacent.get(from);
        adjToFrom.add(edge);

        this.edges++;
    }

    public int sizeV() {
        return this.adjacent.size();
    }

    public int sizeE() {
        return this.edges;
    }
}
