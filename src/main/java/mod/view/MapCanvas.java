package mod.view;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import mod.osm.OsmBounds;
import mod.osm.OsmNode;
import mod.renderer.Renderer;
import mod.tree.Viewport;
import mod.utils.Distance;


public class MapCanvas extends Canvas {
    private final Affine transform;
    private final Renderer renderer;

    public MapCanvas() {
        this.transform = new Affine();
        this.renderer = new Renderer(this, this.transform);
    }

    public Affine getTransform() {
        return this.transform;
    }

    public Renderer getRenderer() {
        return this.renderer;
    }

    public void pan(double lat, double lon) {
        transform.prependTranslation(lat, lon);
        this.renderer.update();
    }

    public void zoom(double factor, double lat, double lon) {
        double currentZoom = transform.getMxx();
        double minZoom = renderer.getMinZoom();
        double maxZoom = renderer.getMaxZoom();

        if (currentZoom * factor < minZoom - 0.001) {
            factor = minZoom / currentZoom;
        } else if (currentZoom * factor > maxZoom + 0.001) {
            factor = maxZoom / currentZoom;
        }

        transform.prependTranslation(-lat, -lon);
        transform.prependScale(factor, factor);
        transform.prependTranslation(lat, lon);
        this.renderer.update();
    }

    /**
     * Translates mouse coordinates to map coordinates.
     *
     * @param lat the latitude of the mouse
     * @param lon the longitude of the mouse
     * @return a Point2D object with the translated coordinates
     */
    public Point2D toMapCoords(double lat, double lon) {
        try {
            Point2D translatedCoords = transform.inverseTransform(lat, lon);
            lat = translatedCoords.getX() / 0.56;
            lon = translatedCoords.getY() * -1;
            return new Point2D(lat, lon);
        } catch (NonInvertibleTransformException ignore) {
            return new Point2D(0, 0);
        }
    }

    public Viewport getViewBounds() {
        double x1 = transform.getTx() / Math.sqrt(transform.determinant());
        double y1 = (-transform.getTy()) / Math.sqrt(transform.determinant());
        double x2 = getWidth() - x1;
        double y2 = getHeight() - y1;

        Point2D p1 = toMapCoords(x1, y1);
        Point2D p2 = toMapCoords(x2, y2);

        return new Viewport(p2.getY(), p1.getX(), p1.getY(), p2.getX());
    }

    public void drawScaleBar() {
        GraphicsContext gc = this.getGraphicsContext2D();

        gc.setTransform(new Affine());

        double scaleBarWidth = 100;
        double scaleBarX = this.getWidth() - 250;
        double scaleBarY = this.getHeight() - 50;

        // Draw scale bar.
        gc.setFill(Color.BLACK);
        gc.fillRect(scaleBarX, scaleBarY, scaleBarWidth, 2);

        //make small lines in the start and end of the scale bar.
        gc.fillRect(scaleBarX, scaleBarY - 10, 2, 10);
        gc.fillRect(scaleBarX + scaleBarWidth - 2, scaleBarY - 10, 2, 10);

        // Convert screen distance to geographic coordinates.
        Point2D startBarMap = this.toMapCoords(scaleBarX, scaleBarY);
        Point2D endBarMap = this.toMapCoords(scaleBarX + scaleBarWidth, scaleBarY);

        // Using the distance between the two points to calculate the real world distance.
        double realWorldDistance = Distance.distanceBetween(startBarMap.getY(), startBarMap.getX(), endBarMap.getY(), endBarMap.getX());

        gc.setFill(Color.BLACK);
        if (realWorldDistance < 1) {
            realWorldDistance *= 1000;
            gc.fillText(String.format("%.0f m", realWorldDistance), scaleBarX + scaleBarWidth / 2 - 10, scaleBarY - 5);
        } else {
            gc.fillText(String.format("%.2f km", realWorldDistance), scaleBarX + scaleBarWidth / 2 - 15, scaleBarY - 5);
        }
    }

    public double getZoomLevel() {
        return transform.getMxx();
    }

    public void focusOn(double lon, double lat, double zoomPercentage) {
        double currentZoom = this.getZoomLevel();

        // Zooming in the desired amount
        double desiredZoom = this.renderer.getMaxZoom() * zoomPercentage;
        double dz = desiredZoom / currentZoom;
        this.zoom(dz, 0, 0);

        // Converting to screenCoords
        double targetX = lon * 0.56;
        double targetY = -lat;

        // Updating currentZoom
        currentZoom = this.getZoomLevel();

        // Applying the transform to X and Y
        targetX = targetX * currentZoom;
        targetX = targetX + transform.getTx();
        targetY = targetY * currentZoom;
        targetY = targetY + transform.getTy();

        // Locating center of screen
        double x1 = transform.getTx() / Math.sqrt(transform.determinant());
        double y1 = (-transform.getTy()) / Math.sqrt(transform.determinant());
        double x2 = getWidth() - x1;
        double y2 = getHeight() - y1;

        double centerX = (x2 - x1)/2;
        double centerY = (y2 - y1)/2;

        // Determining distance from center to target
        double dx = targetX - centerX;
        double dy = targetY - centerY;

        this.pan(-dx, -dy);
        this.renderer.update();
    }

    public void focusOn(OsmNode node1, OsmNode node2) {
        double lon1 = node1.getLon();
        double lat1 = node1.getLat();
        double lon2 = node2.getLon();
        double lat2 = node2.getLat();
        focusOn(lon1, lat1, lon2, lat2);
    }

    public void focusOn(double lon1, double lat1, double lon2, double lat2) {
        double targetLon = (lon1 + lon2)/2;
        double targetLat = (lat1 + lat2)/2;

        OsmBounds bounds = this.getRenderer().getMapData().getBounds();

        double maxDist = Distance.screenDistanceBetween(bounds.MIN_LAT, bounds.MIN_LON, bounds.MAX_LAT, bounds.MAX_LON);
        double dist = Distance.screenDistanceBetween(lat1, lon1, lat2, lon2);

        double minZoom = this.getRenderer().getMinZoom();
        double maxZoom = this.getRenderer().getMaxZoom();
        double zoomPercentage = ((minZoom * (maxDist / dist)) / maxZoom) / 4; // this is so shit

        focusOn(targetLon, targetLat, zoomPercentage);
    }
}
