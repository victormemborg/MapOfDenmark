package mod.tree;

import mod.utils.QuickSelect;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class KdTree<T extends Bounded> implements Serializable {
    private final int dimensions;
    private final Node<T> root;
    private Node<T> best;
    private double bestDistance;
    private List<T> nodesWithin;

    public KdTree(int dimensions, List<T> elements) {
        this.dimensions = dimensions;

        // Wrapping all elements in the private Node wrapper class
        List<Node<T>> wrappedElements = new ArrayList<>();
        for (T element : elements) {
            wrappedElements.add(new Node<T>(element));
        }

        this.root = makeTree(wrappedElements, 0, wrappedElements.size(), 0);
    }


    public T findNearest(T target) {
        if (root == null) {
            throw new IllegalStateException("KdTree is empty!");
        }

        // wrapping
        Node<T> wrappedTarget = new Node<T>(target);
        best = null;
        bestDistance = 0;
        nearest(root, wrappedTarget, 0);

        return best.val;
    }

    public List<T> getNodesWithin(double[] min, double[] max) {
        if (root == null) {
            return null;
        }

        nodesWithin = new ArrayList<>(15000);

        double[] bounds = new double[]{min[0], min[1], max[0], max[1]};
        rangeSearch(root, bounds, 0);

        return nodesWithin;
    }

    /**
     * Returns the distance between the target and its nearest node
     * from the last findNearest() call. Currently, uses the formula
     * for distance on a flat plane. Maybe change to Haversine if
     * more desirable.
     *
     * @return the distance between the target and its nearest node
     * from the last findNearest() call.
     */
    public double distance() {
        return Math.sqrt(bestDistance);
    }

    private void rangeSearch(Node<T> current, double[] bounds, int axis) {
        if (current == null) return;

        if (current.intersects(bounds)) {
            // also unwrapping from the Node class
            nodesWithin.add(current.val);
        }

        int offsetIndex = (axis + (dimensions / 2)) % dimensions;
        boolean currentIsMinimum = axis < (dimensions / 2);
        boolean searchLeft = false;
        boolean searchRight = false;

        if (currentIsMinimum) {
            searchLeft = true;

            if (!(current.get(axis) >= bounds[offsetIndex])) {
                searchRight = true;
            }
        } else {
            searchRight = true;

            if (!(current.get(axis) <= bounds[offsetIndex])) {
                searchLeft = true;
            }
        }

        axis = (axis + 1) % dimensions;

        if (searchLeft) rangeSearch(current.left, bounds, axis);
        if (searchRight) rangeSearch(current.right, bounds, axis);
    }

    private void nearest(Node<T> current, Node<T> target, int axis) {
        if (current == null) return;

        // Check if we have found a closer node
        double dist = current.distance(target);
        if (best == null || dist < bestDistance) {
            bestDistance = dist;
            best = current;
        }

        // If perfect match, return immediately
        if (bestDistance == 0) return;

        // The difference the current axis
        double dk = current.get(axis) - target.get(axis);
        // Determine the axis to compare next
        axis = (axis + 1) % dimensions;

        // Case1: If (dk < 0) then (target is to the right on this axis) so (we go right).
        // Case2: If (dk > 0) then (target is to the left on this axis) so (we go left).
        // Case3: If (dk == 0) then (target[axis] == current[axis]) so (we default to right)
        nearest(dk > 0 ? current.left : current.right, target, axis);

        // Squaring dk to avoid negative distances
        if (dk * dk >= bestDistance) return;
        // If we reach this point, there is still hope to find a closer node
        // in the opposite subtree, even though the distance on this particular
        // axis will be increased. Note that if we previously defaulted (dx == 0),
        // we will now always go both ways.
        nearest(dk > 0 ? current.right : current.left, target, axis);
    }

    private Node<T> makeTree(List<Node<T>> nodes, int begin, int end, int axis) {
        if (end <= begin) return null;

        int mid = begin + (end - begin) / 2;
        Node<T> node = QuickSelect.select(nodes, begin, end - 1, mid, new NodeComparator(axis));
        axis = (axis + 1) % dimensions;

        node.left = makeTree(nodes, begin, mid, axis);
        node.right = makeTree(nodes, mid + 1, end, axis);

        return node;
    }

    private record NodeComparator(int axis) implements Comparator<Node> {
        @Override
        public int compare(Node n1, Node n2) {
            return Double.compare(n1.get(axis), n2.get(axis));
        }
    }

    public static class Node<T extends Bounded> implements Serializable {
        T val;
        private Node<T> left;
        private Node<T> right;

        private Node(T val) {
            this.val = val;
        }

        private double get(int axis) {
            return this.val.getBound(axis);
        }

        private boolean intersects(double[] bounds) {
            return this.val.intersects(bounds);
        }

        /**
         * Method for calculating the distance to another that.
         * Distance in this is method is not the typical interpretation
         * of the word. <br> <br>
         * <p>
         * Say we have 3 points: A=(0,0) B=(3,4) C=(8,6). The actual
         * distance from A to B and C are 5- and 10-units respectively.
         * This is determined using pythagoras theorem. This method of
         * determining distances uses slow sqrt operations, which will be
         * especially noticeable in higher dimensions. Therefor we simplify
         * the calculation to: <br> <br>
         * <p>
         * (p1[0] - p2[0])^2 + (p1[1] - p2[1])^2 + ... + (p1[n] - p2[n])^2 <br> <br>
         * <p>
         * for n dimensions. This might not give a "correct" distance between
         * p1 and p2, but the scale of the relation is still preserved.
         * Using this formula, B is still closer to A than C, which is the
         * only thing we care about when using this method. We have just
         * come to that conclusion significantly quicker.
         *
         * @param that the that to measure distance to
         * @return the "distance" to that.
         */
        private double distance(Node<T> that) {
            double dist = 0;
            for (int i = 0; i < val.getBoundsLength(); ++i) {
                double d = val.getBound(i) - that.val.getBound(i);
                dist += d * d;
            }
            return dist;
        }

        public String toString() {
            StringBuilder s = new StringBuilder("(");
            for (int i = 0; i < val.getBoundsLength(); ++i) {
                if (i > 0) s.append(", ");
                s.append(val.getBound(i));
            }
            s.append(')');
            return s.toString();
        }
    }
}
