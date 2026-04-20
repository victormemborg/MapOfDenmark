package mod.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import mod.utils.Coords;

public class PointOfInterest {
    private final String name;
    private final double latitude;
    private final double longitude;
    private Color color;
    private boolean isTemporary = false;

    public PointOfInterest(String name, double latitude, double longitude, Color color) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public double getLat() {
        return latitude;
    }

    public double getLong() {
        return longitude;
    }

    public void draw(GraphicsContext gc) {
        gc.save();
        gc.beginPath();
        gc.setStroke(this.color);

        double[] coords = Coords.toScreenCoords(this.longitude, this.latitude);
        double width = gc.getLineWidth() * 5;

        double[] ll = new double[]{coords[0] - width, coords[1] - width};
        double[] ur = new double[]{coords[0] + width, coords[1] + width};
        double[] ul = new double[]{coords[0] - width, coords[1] + width};
        double[] lr = new double[]{coords[0] + width, coords[1] - width};

        gc.moveTo(ll[0], ll[1]);
        gc.lineTo(ur[0], ur[1]);
        gc.moveTo(ul[0], ul[1]);
        gc.lineTo(lr[0], lr[1]);

        gc.stroke();
        gc.restore();
    }

    public void setColor(Color selectedColor) {
        this.color = selectedColor;
    }

    public String getColor() {
        return switch (this.color.toString()) {
            case "0x0000ffff" -> "Blue";
            case "0xff0000ff" -> "Red";
            case "0x008000ff" -> "Green";
            case "0xff4500ff" -> "Orange";
            default -> "Unknown";
        };
    }

    public void setTemporary(boolean temporary) {
        this.isTemporary = temporary;
    }

    public boolean isTemporary() {
        return this.isTemporary;
    }
}
