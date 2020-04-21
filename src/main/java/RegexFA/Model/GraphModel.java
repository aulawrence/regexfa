package RegexFA.Model;

import java.nio.file.Path;

public class GraphModel extends Model {
    private String label;
    private Path imagePath;
    private double zoom;

    public GraphModel() {
        label = "Graph";
        imagePath = null;
        zoom = 1.0;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Path getImagePath() {
        return imagePath;
    }

    public void setImagePath(Path imagePath) {
        this.imagePath = imagePath;
    }

    public double getZoom() {
        return zoom;
    }

    public void setZoom(double zoom) {
        this.zoom = zoom;
    }
}
