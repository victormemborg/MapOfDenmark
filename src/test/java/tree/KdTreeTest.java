package tree;

import javafx.geometry.Point2D;
import mod.osm.OsmNode;
import mod.osm.OsmWay;
import mod.tree.KdTree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import mod.tree.Viewport;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class KdTreeTest {
    private OsmWay createOsmWay(double minX, double minY, double maxX, double maxY) {
        OsmNode[] nodes = {new OsmNode(minX, minY, null), new OsmNode(maxX, maxY, null)};
        return new OsmWay(nodes, null);
    }

    @Test
    public void testEmptyTree() {
        KdTree<OsmWay> kdTree = new KdTree<>(2, new ArrayList<>());
        OsmWay test = createOsmWay(0, 0, 1, 1);
        assertThrows(IllegalStateException.class, () -> kdTree.findNearest(test));
    }

    @Test
    public void testSingleElementTree() {
        OsmWay singleWay = createOsmWay(2, 3, 5, 6);
        List<OsmWay> ways = new ArrayList<>();
        ways.add(singleWay);
        KdTree<OsmWay> kdTree = new KdTree<>(2, ways);
        OsmWay nearest = kdTree.findNearest(createOsmWay(3, 4, 4, 5));
        assertEquals(singleWay, nearest);
    }

    @Test
    public void testFindNearest() {
        List<OsmWay> ways = List.of(
                createOsmWay(2, 3, 3, 4),
                createOsmWay(5, 4, 6, 5),
                createOsmWay(9, 6, 10, 7),
                createOsmWay(4, 7, 5, 8),
                createOsmWay(8, 1, 9, 2)
        );
        KdTree<OsmWay> kdTree = new KdTree<>(2, ways);
        OsmWay target = createOsmWay(5, 5, 5, 5);
        OsmWay nearest = kdTree.findNearest(target);
        assertTrue(nearest.intersects(new double[]{4, 4, 6, 6}));
    }

    @Test
    public void testRangeSearch() {
        List<OsmWay> ways = List.of(
                createOsmWay(2, 3, 3, 4),
                createOsmWay(5, 4, 6, 5),
                createOsmWay(9, 6, 10, 7),
                createOsmWay(4, 7, 5, 8),
                createOsmWay(8, 1, 9, 2)
        );
        KdTree<OsmWay> kdTree = new KdTree<>(2, ways);
        List<OsmWay> result = kdTree.getNodesWithin(new double[]{3, 3}, new double[]{7, 7});
        //1st, 2nd, 4th are within the range
        assertEquals(3, result.size());
    }
}
