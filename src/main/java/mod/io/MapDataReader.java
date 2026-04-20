package mod.io;

import mod.osm.OsmBounds;
import mod.osm.OsmWay;
import mod.pathfinding.AddressTrie;
import mod.pathfinding.Dijkstra;
import mod.renderer.MapData;
import mod.tree.KdTree;

import java.io.*;
import java.util.List;

public class MapDataReader {
    public MapData read(File file) throws IOException, ClassNotFoundException {
        ProgressInputStream pis = new ProgressInputStream(file);
        return this.read(pis);
    }


    @SuppressWarnings("unchecked")
    public MapData read(InputStream is) throws IOException, ClassNotFoundException {
        BufferedInputStream bif = new BufferedInputStream(is);
        ObjectInputStream ois = new ObjectInputStream(bif);

        MapData mapData = new MapData(
                (OsmBounds) ois.readObject(),
                (AddressTrie) ois.readObject(),
                (KdTree<OsmWay>[]) ois.readObject(),
                (Dijkstra) ois.readObject(),
                (List<OsmWay>) ois.readObject()
        );

        ois.close();
        bif.close();
        is.close();

        System.gc();

        return mapData;
    }
}
