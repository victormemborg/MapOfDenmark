package mod.utils;

import mod.osm.OsmEntity;
import mod.osm.OsmTag;
import mod.osm.OsmWay;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public enum Transport implements Serializable {
    CAR, BIKE, WALK;

    private static Transport transport = Transport.CAR;

    public static void setMode(Transport transport) {
        Transport.transport = transport;
    }

    public static Transport getMode() {
        return transport;
    }

    public static Transport[] from(OsmWay way) {
        List<Transport> transports = new ArrayList<>();

        for (OsmTag tag : way.getTags()) {
            switch (tag.getValue()) {
                case "motorway",
                        "trunk",
                        "primary",
                        "secondary",
                        "tertiary",
                        "unclassified",
                        "residential",
                        "living_street",
                        "motorway_link",
                        "trunk_link",
                        "primary_link",
                        "secondary_link",
                        "tertiary_link" -> transports.add(CAR);
            }

            switch (tag.getValue()) {
                case "primary",
                        "secondary",
                        "tertiary",
                        "unclassified",
                        "residential",
                        "living_street",
                        "track",
                        "path",
                        "cycleway",
                        "primary_link",
                        "secondary_link",
                        "tertiary_link" -> transports.add(BIKE);
            }

            switch (tag.getValue()) {
                case "primary",
                        "secondary",
                        "tertiary",
                        "unclassified",
                        "residential",
                        "living_street",
                        "track",
                        "path",
                        "cycleway",
                        "footway",
                        "pedestrian",
                        "primary_link",
                        "secondary_link",
                        "tertiary_link" -> transports.add(WALK);
            }
        }

        return transports.toArray(new Transport[0]);
    }

    @Override
    public String toString() {
        return switch (this) {
            case CAR -> "Car";
            case BIKE -> "Bike";
            case WALK -> "Walk";
        };
    }
}


