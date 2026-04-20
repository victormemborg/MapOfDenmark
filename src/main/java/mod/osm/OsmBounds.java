package mod.osm;

import java.io.Serializable;

public class OsmBounds implements Serializable {
    public final double MIN_LON, MAX_LON, MIN_LAT, MAX_LAT;

    public OsmBounds(double minLon, double maxLon, double minLat, double maxLat) {
        this.MIN_LON = minLon;
        this.MAX_LON = maxLon;
        this.MIN_LAT = minLat;
        this.MAX_LAT = maxLat;
    }

    @Override
    public String toString() {
        return "OsmBounds{" + "MIN_LON=" + MIN_LON + ", MAX_LON=" + MAX_LON + ", MIN_LAT=" + MIN_LAT + ", MAX_LAT=" + MAX_LAT + '}';
    }
}
