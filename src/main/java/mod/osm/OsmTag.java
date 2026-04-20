package mod.osm;

import mod.renderer.LevelOfDetail;

import java.io.Serializable;
import java.util.List;

/**
 * Represents an OsmTag with a value and a key.
 * <br/>
 * The keys are represented by an enum because this take way less memory
 * than storing it as a string.
 */
public class OsmTag implements Serializable {
    private final Key key;
    private final String value;

    public OsmTag(Key key, String value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Create a {@link OsmTag} two string.
     * <br/>
     * This is necessary because we don't want to return an exception if the
     * tag is not recognised. Creating an exception takes a lot more memory and CPU time
     * than just returning null. Since we very often ignore tag, its is way better to just
     * return null instead of creating the exception.
     * <br/>
     * Tags that does not need a value, can create a new {@link OsmTag} where the value
     * parameter is null. This will then not set the string and therefore spare memory
     * <br/>
     * The tag's value are also immutable strings that might occur multiple times. Therefore,
     * we use the intern method on the strings, so they are only stored once in memory, even tho
     * the string value might be represented for multiple different keys.
     *
     * @param keyRaw The raw string representation of the tag.
     * @param value  The string value of the tag.
     * @return Null if the raw key is not a recognised tag or {@link OsmTag}.
     */
    public static OsmTag from(String keyRaw, String value) {
        Key key = Key.from(keyRaw);
        if (key == null) return null;
        if (value == null) return new OsmTag(key, null);
        return new OsmTag(key, value.intern());
    }

    /**
     * Return which level of detail a given tag belongs to.
     *
     * @param tag The key that belongs to some level of detail.
     * @return The level og detail the tag belongs to.
     */
    public static LevelOfDetail getLevelOfDetail(OsmTag tag) {
        switch (tag.getKey()) {
            case BUILDING:
                return LevelOfDetail.Level0;

            case RAILWAY:
                switch (tag.getValue()) {
                    case "rail":
                        return LevelOfDetail.Level8;
                    case "light_rail":
                        return LevelOfDetail.Level5;
                    case "monorail":
                    case "subway":
                    case "tram":
                        return LevelOfDetail.Level3;
                    default:
                        return LevelOfDetail.Level2;
                }

            case FOOTWAY, FOOTPATH:
                switch (tag.getValue()) {
                    case "sidewalk":
                        return LevelOfDetail.Level1;
                    default:
                        return LevelOfDetail.Level0;
                }

            case NATURAL:
                switch (tag.getValue()) {
                    case "coastline":
                        return LevelOfDetail.Level8;
                    case "grassland":
                    case "heath":
                    case "wood":
                        return LevelOfDetail.Level6;
                    case "water":
                        return LevelOfDetail.Level4;
                }

            case LANDUSE:
                switch (tag.getValue()) {
                    case "farmland":
                    case "forest":
                        return LevelOfDetail.Level6;
                    case "vineyard":
                    case "orchard":
                    case "residential":
                    case "industrial":
                    case "commercial":
                    case "retail":
                        return LevelOfDetail.Level4;
                    case "farmyard":
                    case "quarry":
                    case "allotments":
                    case "grass":
                    case "meadow":
                        return LevelOfDetail.Level3;
                    case "basin":
                    case "salt_pound":
                        return LevelOfDetail.Level1;
                    default:
                        return LevelOfDetail.Level0;
                }

            case HIGHWAY:
                switch (tag.getValue()) {
                    case "motorway":
                    case "trunk":
                        return LevelOfDetail.Level8;
                    case "primary":
                    case "secondary":
                        return LevelOfDetail.Level7;
                    case "tertiary":
                        return LevelOfDetail.Level5;
                    case "residential":
                        return LevelOfDetail.Level2;
                    case "unclassified":
                        return LevelOfDetail.Level1;
                    default:
                        return LevelOfDetail.Level0;
                }

            default:
                return null;
        }
    }

    public Key getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return this.getKey().toString() + "=" + this.getValue();
    }

    public enum Key {
        PATH,
        FOOTPATH,
        FOOTWAY,
        HIGHWAY,
        RAILWAY,
        BUILDING,
        NATURAL,
        LANDUSE,
        HOUSE_NUMBER,
        STREET,
        CITY,
        POSTAL_CODE,
        JUNCTION,
        NAME,
        ONEWAY,
        MAX_SPEED,
        PATHFINDING,
        COASTLINE;

        public static Key from(String string) {
            return switch (string) {
                case "path" -> PATH;
                case "footpath" -> FOOTPATH;
                case "footway" -> FOOTWAY;
                case "highway" -> HIGHWAY;
                case "railway" -> RAILWAY;
                case "building" -> BUILDING;
                case "natural" -> NATURAL;
                case "landuse" -> LANDUSE;
                case "addr:housenumber" -> HOUSE_NUMBER;
                case "addr:street" -> STREET;
                case "addr:city" -> CITY;
                case "addr:postcode" -> POSTAL_CODE;
                case "junction" -> JUNCTION;
                case "name" -> NAME;
                case "oneway" -> ONEWAY;
                case "maxspeed" -> MAX_SPEED;
                default -> null;
            };
        }
    }
}
