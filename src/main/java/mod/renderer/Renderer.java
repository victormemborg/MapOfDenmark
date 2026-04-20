package mod.renderer;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import mod.osm.OsmBounds;
import mod.osm.OsmWay;
import mod.pathfinding.Edge;
import mod.tree.Viewport;
import mod.view.MapCanvas;
import mod.view.PointOfInterest;
import mod.view.StyleAttributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;


public class Renderer {
    private static final double ABSOLUTE_MIN_ZOOM = 176.434531; // The height to lat ratio of all of Denmark
    private final Affine transform;
    private final MapCanvas canvas;
    private final GraphicsContext gc;
    private double MIN_ZOOM;
    private double MAX_ZOOM;
    private MapData mapData;
    private boolean viewDebug = false;
    private boolean viewHeuristics = false;

    public Renderer(MapCanvas canvas, Affine transform) {
        this.transform = transform;
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.gc.setTransform(this.transform);
    }

    public void render(MapData data) {
        this.reset();

        this.mapData = data;
        OsmBounds bounds = data.getBounds();

        this.MIN_ZOOM = canvas.getHeight() / (bounds.MAX_LAT - bounds.MIN_LAT);
        this.MAX_ZOOM = ABSOLUTE_MIN_ZOOM * 2000;

        // Force pan and zoom at first render.
        transform.prependTranslation(-0.56 * bounds.MIN_LON, bounds.MAX_LAT);

        transform.prependTranslation(-0, -0);
        transform.prependScale(MIN_ZOOM, MIN_ZOOM);
        transform.prependTranslation(0, 0);

        this.draw();
    }

    public void draw() {
        this.reset();

        Viewport area = this.canvas.getViewBounds();
        if (viewDebug) {
            area.decreaseViewport(0.3);
        }

        int lod = getLevelOfDetail();

        List<OsmWay> waysToDraw = new ArrayList<>();
        int maxLod = LevelOfDetail.values().length - 1;

        for (OsmWay coastline : mapData.getCoastlines()) {
            coastline.draw(gc);
        }

        for (int i = maxLod; i >= lod; i--) {
            List<OsmWay> ways = mapData.ways[i].getNodesWithin(area.getMin(), area.getMax());
            if (ways == null) continue;
            waysToDraw.addAll(ways);
        }

        for (OsmWay way : waysToDraw) {
            way.draw(gc);
        }

        for (PointOfInterest poi : mapData.getPointsOfInterest()) {
            poi.draw(gc);
        }

        OsmWay path = mapData.getPath();
        if (path != null) {
            path.draw(gc);
        }

        Set<Edge> considered = mapData.getDijkstra().getConsidered();
        if (considered != null && viewHeuristics) {
            for (Edge edge : considered) {
                edge.draw(gc);
            }
        }
    }

    public void update() {
        if (this.mapData == null) return;

        this.draw();
        this.canvas.drawScaleBar();
    }

    public void reset() {
        gc.setTransform(new Affine());

        // Overrides previously drawn lines.
        gc.setFill(StyleAttributes.getBackgroundColor());
        gc.fillRect(0, 0, this.canvas.getWidth(), this.canvas.getHeight());

        gc.setTransform(this.transform);

        // Secures consistent line width.
        gc.setLineWidth(1 / Math.sqrt(this.transform.determinant()));
    }

    public double getMinZoom() {
        return MIN_ZOOM;
    }

    public double getMaxZoom() {
        return MAX_ZOOM;
    }

    public MapData getMapData() {
        return this.mapData;
    }

    private int getLevelOfDetail() {
        double currentZoom = transform.getMxx();
        double initialDoubling = Math.log(ABSOLUTE_MIN_ZOOM) / Math.log(2.05); //increasing this number = further between each lod-level
        double timesDoubled = Math.log(currentZoom) / Math.log(2.05) - initialDoubling; // must be same number

        int lodFlipped = (int) Math.floor(timesDoubled);
        int maxLod = LevelOfDetail.values().length - 1;

        if (lodFlipped < 0) lodFlipped = 0;
        else if (lodFlipped > maxLod) lodFlipped = maxLod;

        return maxLod - lodFlipped;
    }

    public void toggleViewDebug() {
        this.viewDebug = !this.viewDebug;
        this.update();
    }

    public void toggleViewHeuristics() {
        this.viewHeuristics = !this.viewHeuristics;
        this.update();
    }
}
