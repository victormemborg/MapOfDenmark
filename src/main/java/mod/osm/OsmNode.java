package mod.osm;

import mod.tree.Bounded;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class OsmNode extends OsmEntity implements Serializable, Bounded {
    private final double lat, lon;

    public OsmNode(double lat, double lon, OsmTag[] tags) {
        super(tags);
        this.lat = lat;
        this.lon = lon;
    }

    public double getLon() {
        return this.lon;
    }

    public double getLat() {
        return this.lat;
    }

    @Override
    public boolean intersects(double[] bounds) {
        return false; // not used
    }

    @Override
    public double getBound(int index) {
        if (index == 0) return this.lon;
        return this.lat;
    }

    @Override
    public int getBoundsLength() {
        return 2;
    }

    public void trimTags() {
        List<OsmTag> trimmed = Arrays.stream(this.getTags()).map(tag -> switch (tag.getKey()) {
            case HOUSE_NUMBER, CITY, POSTAL_CODE, STREET -> null;
            default -> tag;
        }).filter(Objects::nonNull).toList();

        OsmTag[] array = new OsmTag[trimmed.size()];

        for (int i = 0; i < trimmed.size(); i++) {
            array[i] = trimmed.get(i);
        }

        this.setTags(array);
    }
}
