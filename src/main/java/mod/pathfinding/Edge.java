package mod.pathfinding;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import mod.osm.OsmNode;
import mod.osm.OsmWay;
import mod.utils.Coords;
import mod.utils.Transport;

import java.io.Serializable;

public record Edge(OsmNode from, OsmNode to, OsmWay way) implements Serializable {
    boolean allowsTransport(Transport mode) {
        for (Transport validTrans : Transport.from(way)) {
            if (validTrans == mode) return true;
        }
        return false;
    }

    public void draw(GraphicsContext gc) {
        double[] f = Coords.toScreenCoords(from);
        double[] t = Coords.toScreenCoords(to);

        gc.save();
        gc.beginPath();
        gc.setStroke(Color.GREEN);
        gc.moveTo(f[0], f[1]);
        gc.lineTo(t[0], t[1]);
        gc.stroke();
        gc.restore();
    }
}
