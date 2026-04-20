package mod.osm;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.FillRule;
import mod.tree.Bounded;
import mod.utils.Coords;
import mod.utils.Distance;
import mod.view.Drawable;
import mod.view.StyleAttributes;

import java.io.Serializable;
import java.text.ParseException;

public class OsmWay extends OsmEntity implements Serializable, Bounded, Drawable {
    private final OsmNode[] nodes;
    private final double[] bounds;

    public OsmWay(OsmNode[] nodes, OsmTag[] tags) {
        super(tags);
        this.nodes = nodes;
        this.bounds = new double[4];

        this.bounds[0] = Double.MAX_VALUE; // minX
        this.bounds[1] = Double.MAX_VALUE; // minY
        this.bounds[2] = -Double.MAX_VALUE; // maxX
        this.bounds[3] = -Double.MAX_VALUE; // maxY

        for (OsmNode node : this.nodes) {
            double x = node.getLat();
            double y = node.getLon();

            if (x < this.bounds[0]) this.bounds[0] = x; // minX
            if (x > this.bounds[2]) this.bounds[2] = x; // maxX
            if (y < this.bounds[1]) this.bounds[1] = y; // minY
            if (y > this.bounds[3]) this.bounds[3] = y; // maxY
        }
    }

    public OsmNode[] getNodes() {
        return this.nodes;
    }

    @Override
    public boolean intersects(double[] rect) {
        // Trying to check for intersection between anything but a rectangle
        // of the same dimensions as this.bounds is undefined. Checking for other dimensions
        // could be done, but I don't see the usage.
        if (rect.length != 4) {
            throw new RuntimeException("The bounding rectangle has an unexpected amount of dimensions");
        }

        //Cond1. If A's left edge is to the right of the B's right edge, - then A is Totally to right Of B
        boolean cond1 = this.bounds[0] > rect[2];
        //Cond2. If A's right edge is to the left of the B's left edge, - then A is Totally to left Of B
        boolean cond2 = this.bounds[2] < rect[0];
        //Cond3. If A's top edge is below B's bottom edge, - then A is Totally below B
        boolean cond3 = this.bounds[3] < rect[1];
        //Cond4. If A's bottom edge is above B's top edge, - then A is Totally above B
        boolean cond4 = this.bounds[1] > rect[3];

        return !cond1 && !cond2 && !cond3 && !cond4;
    }

    @Override
    public double getBound(int index) {
        return this.bounds[index];
    }

    @Override
    public int getBoundsLength() {
        return this.bounds.length;
    }

    public short getSpeedLimit() {
        for (OsmTag tag : this.getTags()) {
            if (tag.getKey() == OsmTag.Key.MAX_SPEED) {
                try {
                    return Short.parseShort(tag.getValue());
                } catch (NumberFormatException ignored) {
                    break;
                }
            }
        }

        return 50;
    }

    public void draw(GraphicsContext gc) {
        StyleAttributes style = StyleAttributes.getAttributes(this.getTags());
        if (style == null) return;

        double width = gc.getLineWidth();

        // return if too small to see anyway
        if (style.isFilled()) {
            double lenX = this.getBound(2) - this.getBound(0);
            double lenY = this.getBound(3) - this.getBound(1);

            if (lenX < width * 3 || lenY < width * 3) {
                return;
            }
        }

        gc.save();
        gc.beginPath();

        OsmNode[] nodes = this.getNodes();

        OsmNode firstNode = nodes[0];
        OsmNode lastNode = nodes[nodes.length - 1];

        double[] firstCoords = Coords.toScreenCoords(firstNode);
        gc.moveTo(firstCoords[0], firstCoords[1]);

        OsmNode lastDrawNode = firstNode;

        for (int i = 1; i < nodes.length - 1; i++) {
            OsmNode node = nodes[i];

            if (!(style == StyleAttributes.BUILDING)) {
                // Ignore nodes in close proximity.
                if (Distance.screenDistanceBetween(lastDrawNode, node) < width * 5) {
                    continue;
                }
            }

            double[] coords = Coords.toScreenCoords(node);
            gc.lineTo(coords[0], coords[1]);

            lastDrawNode = node;
        }

        double[] lastCoords = Coords.toScreenCoords(lastNode);
        gc.lineTo(lastCoords[0], lastCoords[1]);

        gc.setStroke(style.getColor());
        gc.setLineWidth(width * style.getLineWidth());
        gc.setLineDashes(width * style.getLineDashes());

        if (style.isFilled()) {
            gc.setFill(style.getColor());
            gc.setFillRule(FillRule.NON_ZERO);
            gc.fill();
        }

        gc.stroke();
        gc.restore();
    }
}
