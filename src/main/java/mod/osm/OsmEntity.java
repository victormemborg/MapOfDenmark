package mod.osm;

import java.io.Serializable;

/**
 * A general OSM entity. This should be inherited by every class that
 * represents an entity in the OSM library (Nodes, Ways, Relations...).
 */
abstract public class OsmEntity implements Serializable {
    private OsmTag[] tags;

    public OsmEntity(OsmTag[] tags) {
        this.tags = tags;
    }

    public OsmTag[] getTags() {
        return this.tags;
    }

    public void setTags(OsmTag[] tags) {
        this.tags = tags;
    }
}
