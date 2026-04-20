package mod.tree;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Viewport {
    private double minX, minY, maxX, maxY;

    /**
     * Viewport is a class that is used to represent the view that the user has of the map.
     *
     * @param minX the minimum x value
     * @param minY the minimum y value
     * @param maxX the maximum x value
     * @param maxY the maximum y value
     */
    public Viewport(double minX, double minY, double maxX, double maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    /**
     * Decreases the viewport by a factor.
     *
     * @param factor the factor to decrease the viewport by
     */
    public void decreaseViewport(double factor) {
        double dx = (maxX - minX) * factor;
        double dy = (maxY - minY) * factor;
        minX += dx;
        minY += dy;
        maxX -= dx;
        maxY -= dy;
    }

    /**
     * Draws a rectangle around the viewport - useful for debugging.
     *
     * @param gc the graphics context to draw on
     */
    public void draw(GraphicsContext gc) {
        gc.setStroke(Color.RED); // Change color for visibility

        gc.beginPath();
        gc.setStroke(Color.BLACK);
        gc.moveTo(minX, minY);
        gc.lineTo(minX, maxY);
        gc.lineTo(maxX, maxY);
        gc.lineTo(maxX, minY);
        gc.lineTo(minX, minY);
        gc.stroke();
    }

    /**
     * Returns the minimum x and y values of the viewport.
     *
     * @return an array containing the minimum x and y values
     */
    public double[] getMin() {
        return new double[]{minX, minY};
    }

    /**
     * Returns the maximum x and y values of the viewport.
     *
     * @return an array containing the maximum x and y values
     */
    public double[] getMax() {
        return new double[]{maxX, maxY};
    }
}