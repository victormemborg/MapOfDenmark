package mod.core;

import mod.io.MapDataReader;
import mod.io.MapDataWriter;
import mod.io.OsmParser;
import mod.renderer.MapData;

public class Model {
    private final OsmParser parser;
    private final MapDataReader mapDataReader;
    private final MapDataWriter mapDataWriter;

    private boolean mapLoaded;
    private MapData mapData;

    public Model() {
        this.parser = new OsmParser();
        this.mapDataReader = new MapDataReader();
        this.mapDataWriter = new MapDataWriter();
        this.mapLoaded = false;
    }

    public boolean getMapLoaded() {
        return this.mapLoaded;
    }

    public void setMapLoaded(boolean mapLoaded) {
        this.mapLoaded = mapLoaded;
    }

    public MapData getMapData() {
        return this.mapData;
    }

    public void setMapData(MapData mapData) {
        this.mapData = mapData;
    }

    public OsmParser getOsmParser() {
        return this.parser;
    }

    public MapDataReader getBinaryReader() {
        return this.mapDataReader;
    }

    public MapDataWriter getBinaryWriter() {
        return this.mapDataWriter;
    }
}