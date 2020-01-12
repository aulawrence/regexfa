package Test4.Model;

import Test4.Graph.Graph;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public class GraphViewModel extends Model {
    private final Graph graph;

    public GraphViewModel(Graph graph) {
        this.graph = graph;
    }

    public Image getImage() {
        return SwingFXUtils.toFXImage(graph.getImage(), null);
    }
}
