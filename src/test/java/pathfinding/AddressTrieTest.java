package pathfinding;

import mod.osm.OsmNode;
import mod.pathfinding.AddressTrie;
import mod.utils.Address;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AddressTrieTest {
    private AddressTrie addressTrie;
    private Address address;

    @BeforeEach
    public void setUp() {
        this.addressTrie = new AddressTrie();
        this.address = new Address(new OsmNode(50, 50, null), "Ved Lindelund 256, 2605 Brøndby");
    }

    @Test
    public void testAdd() {
        addressTrie.add(address);
        assertEquals(1, addressTrie.getSize());
    }

    @Test
    public void testAutoComplete() {
        Address address1 = new Address((new OsmNode(50, 50, null)),"Asminderødgade", "37", "3480", "Fredensborg");
        Address address2 = new Address((new OsmNode(50, 50, null)),"Asminderødgade", "3", "3480", "Fredensborg");
        addressTrie.add(address1);
        addressTrie.add(address2);

        String[] completions = addressTrie.getCompletions("Asmind", 2);
        assertNotNull(completions);
        assertEquals(2, completions.length);
    }

    @Test
    public void testNonExistentAutoComplete() {
        String[] completions = addressTrie.getCompletions("random nonexistent address", 1);
        assertNotNull(completions);
        assertEquals(null, completions[0]);
    }
}