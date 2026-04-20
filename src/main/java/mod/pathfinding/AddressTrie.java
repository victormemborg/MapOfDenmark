package mod.pathfinding;

import mod.tree.RedBlackBST;
import mod.utils.Address;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class AddressTrie implements Serializable {
    private final RedBlackBST<String, Node> addresses;
    private int size;

    public AddressTrie() {
        this.addresses = new RedBlackBST<>();
    }

    public int getSize() {
        return this.size;
    }

    public void add(Address address) {
        if (address == null) return;

        Node newNode = new Node(address);
        Node oldNode = this.addresses.put(address.toString(), newNode);
        newNode.setNext(oldNode);

        this.size++;
    }

    public Node find(Address address) {
        if (address == null) return null;
        return this.find(address.toString());
    }

    public Node find(String addressString) {
        if (addressString == null) return null;
        return this.addresses.get(addressString);
    }

    public String[] getCompletions(String input, int amount) {
        if (amount < 1) {
            throw new IllegalArgumentException("amount must be greater than 0");
        }

        if (input == null) {
            return new String[0];
        }

        String greatestPossibleMatch = input.concat("\uFFFF");
        String[] completions = new String[amount];

        for (int i = 0; i < amount; i++) {
            input = this.addresses.higherKey(input);
            if (input == null || input.compareTo(greatestPossibleMatch) > 0) break;
            completions[i] = input;
        }

        return completions;
    }

    public Set<Address> getAllAddresses() {
        Set<Node> nodes = this.addresses.getAllValues();
        Set<Address> addrSet = new HashSet<>();

        for (Node node : nodes) {
            Node current = node;
            while (current != null) {
                addrSet.add(current.getAddress());
                current = current.getNext();
            }
        }

        return addrSet;
    }

    public boolean contains(Address address) {
        Node possibleHouses = this.addresses.get(address.toString());
        if (possibleHouses == null) return false;
        return possibleHouses.contains(address);
    }

    public static class Node implements Serializable {
        private final Address address;

        private Node next;

        public Node(Address address) {
            this.address = address;
        }

        public Address getAddress() {
            return this.address;
        }

        public Node getNext() {
            return this.next;
        }

        public void setNext(Node next) {
            this.next = next;
        }

        boolean contains(Address address) {
            if (this.getAddress() == address) return true;
            if (this.getNext() == null) return false;
            return this.getNext().contains(address);
        }
    }
}
