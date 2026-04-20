package tree;

import mod.osm.OsmNode;
import mod.osm.OsmWay;
import mod.tree.KdTree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import mod.tree.Viewport;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ViewportTest {
    private KdTree<OsmWay> kdTree;
    private Viewport viewport;

    @BeforeEach
    public void setUp() {
        List<OsmWay> ways = new ArrayList<>();

        OsmNode node1 = new OsmNode(1, 1, null);
        OsmNode node2 = new OsmNode(2, 2, null);
        OsmNode node3 = new OsmNode(3, 3, null);
        OsmNode node4 = new OsmNode(4, 4, null);
        OsmNode node5 = new OsmNode(5, 5, null);
        OsmNode node6 = new OsmNode(6, 6, null);

        ways.add(new OsmWay(new OsmNode[]{node1, node2}, null));
        ways.add(new OsmWay(new OsmNode[]{node3, node4}, null));
        ways.add(new OsmWay(new OsmNode[]{node5, node6}, null));

        kdTree = new KdTree<>(2, ways);
        viewport = new Viewport(0, 0, 10, 10);
    }

    @Test
    public void testViewportInitialization() {
        double[] expectedMin = {0, 0};
        double[] expectedMax = {10, 10};
        assertArrayEquals(expectedMin, viewport.getMin());
        assertArrayEquals(expectedMax, viewport.getMax());
    }

    @Test
    public void testDecreaseViewport() {
        viewport.decreaseViewport(0.1);
        double[] expectedMin = {1, 1};
        double[] expectedMax = {9, 9};
        assertArrayEquals(expectedMin, viewport.getMin());
        assertArrayEquals(expectedMax, viewport.getMax());
    }

    @Test
    public void testViewportKdTreeIntegration() {
        viewport.decreaseViewport(0.5);
        List<OsmWay> visibleWays = kdTree.getNodesWithin(viewport.getMin(), viewport.getMax());
        assertEquals(1, visibleWays.size());
    }
}