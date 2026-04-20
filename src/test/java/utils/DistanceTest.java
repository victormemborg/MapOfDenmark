package utils;

import mod.osm.OsmNode;
import org.junit.jupiter.api.Test;
import mod.utils.Distance;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DistanceTest {

    @Test
    public void testDistanceBetweenLatLon() {
        double lat1 = 55.9615000;
        double lon1 = 12.3809000;
        double lat2 = 55.9815000;
        double lon2 = 12.4379000;
        // Round to 3 decimal places
        double distance = Math.round(Distance.distanceBetween(lat1, lon1, lat2, lon2) * 1000.0) / 1000.0;
        assertEquals(4.186, distance); // According to https://latlongdata.com/distance-calculator/ it should be 4.186 km
    }

    @Test
    public void testDistOSMNode() {
        OsmNode node1 = new OsmNode(55.9615000, 12.3809000, null);
        OsmNode node2 = new OsmNode(55.9815000, 12.4379000, null);
        // Round to 3 decimal places
        double distance = Math.round(Distance.distanceBetween(node1, node2) * 1000.0) / 1000.0;
        assertEquals(4.186, distance); // According to https://latlongdata.com/distance-calculator/ it should be 4.186 km
    }

    @Test
    public void testScreenDistanceBetweenLatLon() {
        double lat1 = 55.9615000;
        double lon1 = 12.3809000;
        double lat2 = 55.9815000;
        double lon2 = 12.4379000;
        // Round to 3 decimal places
        double distance = Math.round(Distance.screenDistanceBetween(lat1, lon1, lat2, lon2) * 1000.0) / 1000.0;
        assertEquals(0.06, distance);
    }

    @Test
    public void testScreenDistanceBetweenOSMNode() {
        OsmNode node1 = new OsmNode(55.9615000, 12.3809000, null);
        OsmNode node2 = new OsmNode(55.9815000, 12.4379000, null);
        double distance = Math.round(Distance.screenDistanceBetween(node1, node2) * 1000.0) / 1000.0;
        assertEquals(0.06, distance);
    }
}
