package mod.io;

import mod.renderer.MapData;

import java.io.*;

public class MapDataWriter {
    public void write(File file, MapData mapData) throws IOException, ClassNotFoundException {
        FileOutputStream fos = new FileOutputStream(file);
        BufferedOutputStream bof = new BufferedOutputStream(fos);
        ObjectOutputStream oos = new ObjectOutputStream(bof);

        oos.writeObject(mapData.getBounds());
        oos.writeObject(mapData.getAddressTrie());
        oos.writeObject(mapData.getWays());
        oos.writeObject(mapData.getDijkstra());
        oos.writeObject(mapData.getCoastlines());

        oos.flush();
        bof.flush();
        fos.flush();

        oos.close();
        bof.close();
        fos.close();

        System.gc();
    }
}
