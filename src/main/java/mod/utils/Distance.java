package mod.utils;

import mod.osm.OsmNode;

public class Distance {
    public static double screenDistanceBetween(double lat1, double lon1, double lat2, double lon2) {
        return Math.sqrt((lon2 - lon1) * (lon2 - lon1) + (lat2 - lat1) * (lat2 - lat1));
    }

    public static double screenDistanceBetween(OsmNode node1, OsmNode node2) {
        double lat1 = node1.getLat();
        double lon1 = node1.getLon();
        double lat2 = node2.getLat();
        double lon2 = node2.getLon();
        return screenDistanceBetween(lat1, lon1, lat2, lon2);
    }

    public static double distanceBetween(double lat1, double lon1, double lat2, double lon2) {
        double radius = 6371; // Radius of Earth

        // Distance between lat and lon in radians
        double rLatDistance = Math.toRadians(lat2 - lat1);
        double rLonDistance = Math.toRadians(lon2 - lon1);

        // Haversine formula
        double a = (Math.sin(rLatDistance / 2) * Math.sin(rLatDistance / 2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(rLonDistance / 2) * Math.sin(rLonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return radius * c;
    }

    public static double distanceBetween(OsmNode node1, OsmNode node2) {
        double lat1 = node1.getLat();
        double lon1 = node1.getLon();
        double lat2 = node2.getLat();
        double lon2 = node2.getLon();
        return distanceBetween(lat1, lon1, lat2, lon2);
    }
}
