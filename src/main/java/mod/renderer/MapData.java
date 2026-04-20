package mod.renderer;

import mod.osm.OsmBounds;
import mod.osm.OsmNode;
import mod.osm.OsmWay;
import mod.pathfinding.AddressTrie;
import mod.pathfinding.Dijkstra;
import mod.tree.KdTree;
import mod.view.PointOfInterest;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

public class MapData implements Serializable {
    public final OsmBounds bounds;
    public final AddressTrie addressTrie;
    public final KdTree<OsmWay>[] ways;
    public final List<PointOfInterest> pointsOfInterest;
    public OsmWay path;
    public final Dijkstra dijkstra;
    public final List<OsmWay> coastlines;

    public MapData(OsmBounds bounds, AddressTrie addressTrie, KdTree<OsmWay>[] ways, Dijkstra dijkstra, List<OsmWay> coastlines) {
        this.bounds = bounds;
        this.addressTrie = addressTrie;
        this.ways = ways;
        this.pointsOfInterest = new ArrayList<>();
        this.dijkstra = dijkstra;
        this.coastlines = coastlines;

    }

    public OsmBounds getBounds() {
        return this.bounds;
    }

    public AddressTrie getAddressTrie() {
        return this.addressTrie;
    }

    public KdTree<OsmWay>[] getWays() {
        return this.ways;
    }

    public List<PointOfInterest> getPointsOfInterest() {
        return this.pointsOfInterest;
    }

    public OsmWay getPath() {
        return this.path;
    }

    public Dijkstra getDijkstra() {
        return this.dijkstra;
    }

    public List<OsmWay> getCoastlines() {
        return this.coastlines;
    }

    public void setPath(OsmWay newPath) {
        this.path = newPath;
    }
}
