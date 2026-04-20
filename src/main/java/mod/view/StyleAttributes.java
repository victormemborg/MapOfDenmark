package mod.view;

import javafx.scene.paint.Color;
import mod.osm.OsmTag;


public enum StyleAttributes {
    FOOTWAY(Color.rgb(240, 128, 128), Color.rgb(255, 180, 180), Color.rgb(233, 150, 122), 2, 0.6, false),
    PEDESTRIAN(Color.rgb(47, 79, 79), Color.rgb(30, 30, 30), Color.rgb(192, 192, 192), 0.0, 0.6, false),
    ROAD(Color.rgb(79, 79, 79), Color.rgb(20, 20, 20), Color.rgb(192, 192, 192), 0.0, 1.2, false),
    PRIMARY(Color.rgb(236, 148, 164), Color.rgb(170, 90, 20), Color.rgb(145, 163, 176), 0.0, 1.2, false),
    BUILDING(Color.rgb(197, 184, 174), Color.rgb(150,150,150), Color.rgb(89, 89, 89), 0.0, 1, true),
    TRUNK(Color.rgb(72, 75, 80), Color.rgb(20, 20, 20), Color.rgb(61, 64, 67), 0.0, 1.5, false),
    TERTIARY(Color.rgb(72, 75, 80), Color.rgb(20, 20, 20), Color.rgb(61, 64, 67), 0.0, 1.2, false),
    CYCLEWAY(Color.rgb(33, 33, 250), Color.rgb(63, 63, 120), Color.rgb(52, 66, 130), 2, 0.6, false),
    MOTORWAY(Color.rgb(141, 58, 81), Color.rgb(165, 60, 60), Color.rgb(141, 58, 81), 0.0, 1.8, false),
    RAILWAY(Color.rgb(225, 123, 123), Color.rgb(105, 103, 153), Color.rgb(225, 123, 123), 0.0, 0.6, false),
    WATER(Color.rgb(116, 204,244), Color.rgb(25,67,128), Color.rgb(35,137,218), 0.0, 0.0, true),
    FOREST(Color.rgb(173, 209, 158), Color.rgb(53, 109, 107), Color.rgb(173, 209, 158), 0.0, 0.0, true),
    LANDUSE(Color.DARKSEAGREEN, Color.rgb(33, 89, 87), Color.DARKSEAGREEN, 0.0, 0, true),
    RESINDENTIAL(Color.BLACK, Color.rgb(199, 199, 199), Color.BLACK, 0.0, 1, false),
    COASTLINE(Color.rgb(255,240,230), Color.rgb(80,80,80), Color.WHITE, 0.0, 0, true),
    PATHFINDING(Color.BLUE, Color.GREEN, Color.BLUE, 0.0, 5, false);

    private static Theme theme = Theme.DEFAULT;

    private final double lineDashes;
    private final Color defaultMode, colorBlindMode, darkMode;
    private final double lineWidth;
    private final boolean isFillable;

    StyleAttributes(Color defaultMode, Color darkMode, Color colorBlindMode,  double lineDashes, double lineWidth, boolean isFillable) {
        this.defaultMode = defaultMode;
        this.colorBlindMode = colorBlindMode;
        this.darkMode = darkMode;
        this.lineDashes = lineDashes;
        this.lineWidth = lineWidth;
        this.isFillable = isFillable;
    }

    public static StyleAttributes getAttributes(OsmTag[] tags) {
        if (tags == null) {
            return null;
        }

        for (OsmTag tag : tags) {
            OsmTag.Key key = tag.getKey();
            String value = tag.getValue();

            switch (key) {
                case PATHFINDING:
                    return StyleAttributes.PATHFINDING;

                case COASTLINE:
                    return StyleAttributes.COASTLINE;

                case FOOTWAY:
                case FOOTPATH:
                case PATH:
                    return StyleAttributes.FOOTWAY;

                case BUILDING:
                    return StyleAttributes.BUILDING;

                case HIGHWAY:
                    switch (value) {
                        case "mini_roundabout":
                        case "motorway":
                        case "motorway_link":
                            return StyleAttributes.MOTORWAY;

                        case "primary":
                        case "primary_link":
                            return StyleAttributes.PRIMARY;

                        case "footway":
                        case "footpath":
                        case "path":
                            return StyleAttributes.FOOTWAY;

                        case "pedestrian":
                            return StyleAttributes.PEDESTRIAN;

                        case "cycleway":
                        case "track":
                            return StyleAttributes.CYCLEWAY;

                        case "road":
                        case "service":
                            return StyleAttributes.ROAD;

                        case "trunk":
                        case "trunk_link":
                            return StyleAttributes.TRUNK;

                        case "tertiary":
                        case "tertiary_link":
                        case "secondary":
                        case "secondary_link":
                        case "unclassified":
                            return StyleAttributes.TERTIARY;

                        case "residential":
                            return StyleAttributes.RESINDENTIAL;
                    }

                case RAILWAY:
                    switch (value) {
                        case "rail":
                        case "light_rail":
                            return StyleAttributes.RAILWAY;
                    }

                case NATURAL:
                    switch (value) {
                        case "water":
                            return StyleAttributes.WATER;
                        case "wood":
                            return StyleAttributes.FOREST;
                        case "coastline":
                            return StyleAttributes.COASTLINE;
                    }

                case LANDUSE:
                    switch (value) {
                        case "forest":
                            return StyleAttributes.FOREST;
                        case "meadow":
                        case "grass":
                        case "orchard":
                        case "allotments":
                            return StyleAttributes.LANDUSE;
                    }
            }
        }

        return null;
    }

    public static void setTheme(Theme theme) {
        StyleAttributes.theme = theme;
    }

    public static Color getBackgroundColor() {
        if (theme == Theme.DEFAULT || theme == Theme.COLORBLIND) {
            return Color.rgb(116, 204,244);
        } else {
            return Color.rgb(25,67,128);
        }
    }

    public boolean isFilled() {
        return this.isFillable;
    }

    public double getLineWidth() {
        return this.lineWidth;
    }

    public double getLineDashes() {
        return this.lineDashes;
    }

    public Color getColor() {
        return switch (theme) {
            case DARK -> darkMode;
            case COLORBLIND -> colorBlindMode;
            default -> defaultMode;
        };
    }
}
