package mod.tree;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Simple implementation of a Red-Black binary search tree.
 * Implementation is a mix of the TreeMap implementation from
 * java standard library, and the BinarySearchTree.java from Robert Sedgewick
 * and Kevin Wayne's book "Algorithms, fourth edition"
 *
 * @param <K>
 * @param <V>
 * @author Jon Zeppieri
 * @author Bryce McKinlay
 * @author Eric Blake (ebb9@email.byu.edu)
 * @author Andrew John Hughes (gnu_andrew@member.fsf.org)
 * @author Robert Sedgewick
 * @author Kevin Wayne
 */
public class RedBlackBST<K extends Comparable<K>, V> implements Serializable {
    private static final boolean RED = true;
    private static final boolean BLACK = false;

    Node root;
    int size;

    public V put(K key, V val) {
        if (root == null) {
            root = new Node(key, val, BLACK);
            return null;
        }

        Node current = root;
        Node parent = null;
        int cmp = 0;

        while (current != null) {
            parent = current;
            cmp = key.compareTo(current.key);

            if (cmp < 0) current = current.left;
            else if (cmp > 0) current = current.right;
            else return current.setValue(val);
        }

        // if the tree did not contain the key
        Node newNode = new Node(key, val, RED);
        newNode.parent = parent;

        if (cmp < 0) parent.left = newNode;
        else parent.right = newNode;

        size++;
        this.fixup(newNode);
        return null;
    }

    public V get(K key) {
        Node current = root;

        while (current != null) {
            int cmp = key.compareTo(current.key);

            if (cmp < 0) current = current.left;
            else if (cmp > 0) current = current.right;
            else return current.val;
        }

        return null;
    }

    public K higherKey(K key) {
        return higherKey(root, key);
    }

    public int size() {
        return size + 1;
    }

    private K higherKey(Node n, K key) {
        if (n == null) return null;
        int cmp = key.compareTo(n.key);

        //if (cmp == 0) return n.key; // uncomment to return on perfect match
        if (cmp >= 0) return higherKey(n.right, key);

        K t = higherKey(n.left, key);
        if (t != null) return t;
        else return n.key;
    }

    private void fixup(Node n) {
        // Only need to re-balance when parent is a RED node, and while at least
        // 2 levels deep into the tree (ie: node has a grandparent).

        while (isRed(n.parent) && n.parent.parent != null) {
            if (n.parent == n.parent.parent.left) {
                Node uncle = n.parent.parent.right;
                // Uncle may be null, in which case it is BLACK.
                if (isRed(uncle)) {
                    // Case 1. Uncle is RED: Change colors of parent, uncle,
                    // and grandparent, and move n to grandparent.
                    n.parent.color = BLACK;
                    uncle.color = BLACK;
                    uncle.parent.color = RED;
                    n = uncle.parent;
                } else {
                    if (n == n.parent.right) {
                        // Case 2. Uncle is BLACK and x is right child.
                        // Move n to parent, and rotate n left.
                        n = n.parent;
                        rotateLeft(n);
                    }
                    // Case 3. Uncle is BLACK and x is left child.
                    // Recolor parent, grandparent, and rotate grandparent right.
                    n.parent.color = BLACK;
                    n.parent.parent.color = RED;
                    rotateRight(n.parent.parent);
                }
            } else {
                // Mirror image of above code.
                Node uncle = n.parent.parent.left;
                // Uncle may be null, in which case it is BLACK.
                if (isRed(uncle)) {
                    // Case 1. Uncle is RED: Change colors of parent, uncle,
                    // and grandparent, and move n to grandparent.
                    n.parent.color = BLACK;
                    uncle.color = BLACK;
                    uncle.parent.color = RED;
                    n = uncle.parent;
                } else {
                    if (n == n.parent.left) {
                        // Case 2. Uncle is BLACK and x is left child.
                        // Move n to parent, and rotate n right.
                        n = n.parent;
                        rotateRight(n);
                    }
                    // Case 3. Uncle is BLACK and x is right child.
                    // Recolor parent, grandparent, and rotate grandparent left.
                    n.parent.color = BLACK;
                    n.parent.parent.color = RED;
                    rotateLeft(n.parent.parent);
                }
            }
        }
        root.color = BLACK;
    }

    private boolean isRed(Node node) {
        if (node == null) return false;
        return node.color == RED;
    }

    // make a left-leaning glue link lean to the right
    private void rotateRight(Node node) {
        Node child = node.left;

        // Establish node.left link.
        node.left = child.right;
        if (child.right != null) {
            child.right.parent = node;
        }

        // Establish child->parent link.
        child.parent = node.parent;
        if (node.parent != null) {
            if (node == node.parent.right) {
                node.parent.right = child;

            } else node.parent.left = child;

        } else root = child;

        // Link node and child.
        child.right = node;
        node.parent = child;
    }

    // make a right-leaning glue link lean to the left
    private void rotateLeft(Node node) {
        Node child = node.right;

        // Establish node.right link.
        node.right = child.left;
        if (child.left != null) {
            child.left.parent = node;
        }

        // Establish child->parent link.
        child.parent = node.parent;
        if (node.parent != null) {
            if (node == node.parent.left) {
                node.parent.left = child;

            } else node.parent.right = child;

        } else root = child;

        // Link node and child.
        child.left = node;
        node.parent = child;
    }

    public Set<V> getAllValues() {
        Set<V> elements = new HashSet<>();
        this.traverse(root, elements);
        return elements;
    }

    private void traverse(Node current, Set<V> elements) {
        if (current == null) return;

        elements.add(current.val);
        this.traverse(current.left, elements);
        this.traverse(current.right, elements);
    }

    /**
     * Helper data type, representing a nodes in the RedBlackBST
     */
    private class Node implements Serializable {
        private final K key;
        private V val;
        private Node left, right, parent;
        private boolean color;

        private Node(K key, V val, boolean color) {
            this.key = key;
            this.val = val;
            this.color = color;
        }

        private V setValue(V newVal) {
            V oldVal = this.val;
            this.val = newVal;
            return oldVal;
        }
    }
}
