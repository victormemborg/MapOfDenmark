package mod.utils;

import mod.osm.OsmNode;

public class Coords {
    public static double[] toScreenCoords(double lon, double lat) {
        double[] coords = new double[2];
        coords[0] = 0.56 * lon;
        coords[1] = -lat;
        return coords;
    }

    public static double[] toScreenCoords(OsmNode node) {
        double lon = node.getLon();
        double lat = node.getLat();
        return Coords.toScreenCoords(lon, lat);
    }
}
