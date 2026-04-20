package utils;

import mod.osm.OsmNode;
import org.junit.jupiter.api.BeforeEach;
import mod.utils.Address;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class AddressTest {
    private Address address;
    private OsmNode node;

    @BeforeEach
    public void setUp() {
        node = new OsmNode(0.0, 0.0, null);
        address = new Address(node, "Test", "1", "1234", "TestCity");
    }

    @Test
    public void testGetStreet() {
        assertEquals("Test", address.getStreet());
    }

    @Test
    public void testGetHouseNumber() {
        assertEquals("1", address.getHouseNumber());
    }

    @Test
    public void testGetPostalCode() {
        assertEquals("1234", address.getPostalCode());
    }

    @Test
    public void testGetCity() {
        assertEquals("TestCity", address.getCity());
    }

    @Test
    public void testGetNode() {
        assertEquals(node, address.getNode());
    }

    @Test
    public void testGetNearestHighway() {
        // The nearest highway is set when parsing so it should be null
        assertNull(address.getNearestHighway());
        // if we set it to something else it should be that
        address.setNearestHighway(node);
        assertEquals(node, address.getNearestHighway());
    }
}
