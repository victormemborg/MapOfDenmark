package parsing;

import mod.core.Controller;
import mod.io.OsmParser;
import mod.osm.OsmBounds;
import mod.renderer.MapData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class OSMParsingTest {
    private OsmParser parser;
    private File file;

    @BeforeEach
    public void setUp() {
        parser = new OsmParser();
        file = new File("src/main/resources/mod/data/test.osm");
    }

    @Test
    public void testParse() {
        MapData mapData = parser.parse(file,true);
        assertNotNull(mapData);
    }

    @Test
    public void testCorrectBounds() {
        MapData mapData = parser.parse(file,true);
        OsmBounds mapBounds = new OsmBounds(12.3809000, 12.4379000, 55.9615000, 55.9815000); //expected bounds
        assertEquals(mapBounds.toString(), mapData.getBounds().toString());
    }

    @Test
    public void testContainsWays() {
        MapData mapData = parser.parse(file,true);
        assertTrue(mapData.getWays().length > 0);
    }
}
