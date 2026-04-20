package pathfinding;

import mod.io.OsmParser;
import mod.osm.OsmNode;
import mod.osm.OsmTag;
import mod.osm.OsmWay;
import mod.pathfinding.Digraph;
import mod.pathfinding.Dijkstra;
import mod.pathfinding.Edge;
import mod.renderer.MapData;
import mod.utils.Transport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DijkstraTest {
    @BeforeEach
    public void setUp() {
        OsmParser parser = new OsmParser();
        File file = new File("src/main/resources/mod/data/test.osm");
        MapData mapData = parser.parse(file, true);
    }

    @Test
    public void testShortestPath() {
        Digraph graph = new Digraph();

        OsmNode node1 = new OsmNode(9.1, 0.1, null);
        OsmNode node2 = new OsmNode(0.4, 2.1, null);
        OsmNode node3 = new OsmNode(1.7, 8.2, null);
        OsmNode node4 = new OsmNode(6.4, 9.4, null);
        OsmNode node5 = new OsmNode(4.2, 5.2, null);

        OsmWay way1 = new OsmWay(
            new OsmNode[] {
                node1, node3
            },
            new OsmTag[]{
                new OsmTag(OsmTag.Key.HIGHWAY, "primary")
            }
        );
        OsmWay way2 = new OsmWay(
            new OsmNode[] {
                node3, node2
            },
            new OsmTag[]{
                new OsmTag(OsmTag.Key.HIGHWAY, "primary")
            }
        );
        OsmWay way3 = new OsmWay(
            new OsmNode[] {
                node2, node5
            },
            new OsmTag[]{
                new OsmTag(OsmTag.Key.HIGHWAY, "primary")
            }
        );
        OsmWay way4 = new OsmWay(
            new OsmNode[] {
                node5, node4
            },
            new OsmTag[]{
                new OsmTag(OsmTag.Key.HIGHWAY, "primary")
            }
        );

        Edge edge1 = new Edge(node1, node3, way1);
        Edge edge2 = new Edge(node3, node2, way2);
        Edge edge3 = new Edge(node2, node5, way3);
        Edge edge4 = new Edge(node5, node4, way4);

        graph.addEdge(edge1);
        graph.addEdge(edge2);
        graph.addEdge(edge3);
        graph.addEdge(edge4);

        Dijkstra dijkstra = new Dijkstra(graph);
        assertDoesNotThrow(() -> dijkstra.getShortestPath(node1, node5, Transport.CAR));
    }

    @Test
    public void testNoShortestPath() {
        Digraph graph = new Digraph();

        OsmNode node1 = new OsmNode(9.1, 0.1, null);
        OsmNode node2 = new OsmNode(0.4, 2.1, null);
        OsmNode node3 = new OsmNode(1.7, 8.2, null);
        OsmNode node4 = new OsmNode(6.4, 9.4, null);
        OsmNode node5 = new OsmNode(4.2, 5.2, null);

        OsmWay way1 = new OsmWay(
            new OsmNode[] {
                node1, node3
            },
            new OsmTag[]{
                new OsmTag(OsmTag.Key.HIGHWAY, "primary")
            }
        );
        OsmWay way2 = new OsmWay(
            new OsmNode[] {
                node3, node2
            },
            new OsmTag[]{
                new OsmTag(OsmTag.Key.HIGHWAY, "primary")
            }
        );
        OsmWay way3 = new OsmWay(
            new OsmNode[] {
                node2, node5
            },
            new OsmTag[]{
                new OsmTag(OsmTag.Key.HIGHWAY, "primary")
            }
        );
        OsmWay way4 = new OsmWay(
            new OsmNode[] {
                node5, node4
            },
            new OsmTag[]{
                new OsmTag(OsmTag.Key.HIGHWAY, "primary")
            }
        );

        Edge edge1 = new Edge(node1, node3, way1);
        Edge edge2 = new Edge(node3, node2, way2);
        Edge edge3 = new Edge(node2, node3, way3);
        // There is no way to edge4.
        Edge edge4 = new Edge(node5, node4, way4);

        graph.addEdge(edge1);
        graph.addEdge(edge2);
        graph.addEdge(edge3);
        graph.addEdge(edge4);

        Dijkstra dijkstra = new Dijkstra(graph);
        assertThrows(RuntimeException.class, () -> dijkstra.getShortestPath(node1, node4, Transport.CAR));
    }

    @Test
    public void testTransportShortestPath() {
        Digraph graph = new Digraph();

        OsmNode node1 = new OsmNode(9.1, 0.1, null);
        OsmNode node2 = new OsmNode(0.4, 2.1, null);
        OsmNode node3 = new OsmNode(1.7, 8.2, null);
        OsmNode node4 = new OsmNode(6.4, 9.4, null);
        OsmNode node5 = new OsmNode(4.2, 5.2, null);

        OsmWay way1 = new OsmWay(
                new OsmNode[] {
                        node1, node3
                },
                new OsmTag[]{
                        new OsmTag(OsmTag.Key.HIGHWAY, "motorway")
                }
        );
        OsmWay way2 = new OsmWay(
                new OsmNode[] {
                        node3, node2
                },
                new OsmTag[]{
                        new OsmTag(OsmTag.Key.HIGHWAY, "motorway")
                }
        );
        OsmWay way3 = new OsmWay(
                new OsmNode[] {
                        node2, node5
                },
                new OsmTag[]{
                        new OsmTag(OsmTag.Key.HIGHWAY, "motorway")
                }
        );
        OsmWay way4 = new OsmWay(
                new OsmNode[] {
                        node5, node4
                },
                new OsmTag[]{
                        new OsmTag(OsmTag.Key.HIGHWAY, "motorway")
                }
        );

        Edge edge1 = new Edge(node1, node3, way1);
        Edge edge2 = new Edge(node3, node2, way2);
        Edge edge3 = new Edge(node2, node5, way3);
        Edge edge4 = new Edge(node5, node4, way4);

        graph.addEdge(edge1);
        graph.addEdge(edge2);
        graph.addEdge(edge3);
        graph.addEdge(edge4);

        Dijkstra dijkstra = new Dijkstra(graph);
        // You should not be able to ride your bike along a motorway.
        assertThrows(RuntimeException.class, () -> dijkstra.getShortestPath(node1, node4, Transport.BIKE));
    }
}
