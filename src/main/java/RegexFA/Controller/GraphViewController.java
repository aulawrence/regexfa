package RegexFA.Controller;

import RegexFA.Alphabet;
import RegexFA.Graph.Graph;
import RegexFA.Model.GraphViewModel;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ResourceBundle;

public class GraphViewController extends Controller<GraphViewModel> {
    @FXML
    public ImageView imgView;

    public GraphViewController() {
        this(new Graph(Alphabet.Binary));
    }

    public GraphViewController(Graph graph) {
        this(new GraphViewModel(graph));
    }

    public GraphViewController(GraphViewModel model) {
        super(model);
    }

    public void updateImage() {
        imgView.setImage(model.getImage());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
