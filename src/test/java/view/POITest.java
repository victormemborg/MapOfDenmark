package view;

import javafx.scene.paint.Color;
import mod.view.PointOfInterest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class POITest {
    private PointOfInterest poi;

    @BeforeEach
    public void setUp() {
        poi = new PointOfInterest("Test", 0.0, 0.0, Color.BLUE);
    }

    @Test
    public void testSetColor() {
        poi.setColor(Color.RED);
        assertEquals("Red", poi.getColor());
    }

    @Test
    public void testTemporary() {
        poi.setTemporary(true);
        assertTrue(poi.isTemporary());
    }

    @Test
    public void testCoordinates() {
        assertEquals(0.0, poi.getLat());
        assertEquals(0.0, poi.getLong());
    }

    @Test
    public void testGetName() {
        assertEquals("Test", poi.getName());
    }
}
