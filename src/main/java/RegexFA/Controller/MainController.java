package RegexFA.Controller;

import RegexFA.Alphabet;
import RegexFA.Graph.FAGraph;
import RegexFA.Model.MainModel;
import RegexFA.Parser.ParserException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.embed.swing.SwingFXUtils;


public class MainController extends Controller<MainModel> {
    public TextArea textArea_regex;
    public Button button_regex;
    public TextArea textArea_display;
    public ChoiceBox<Alphabet> choiceBox_alphabet;
    public ImageView image_nfa;
    public ImageView image_dfa;
    public ImageView image_min_dfa;

    public MainController() {
        this(new MainModel());
    }

    protected MainController(MainModel model) {
        super(model);
    }

    @FXML
    private void onClick_buttonRegex(MouseEvent event) {
        String s = textArea_regex.getText();
        try {
            FAGraph graph = model.getGraph(s, choiceBox_alphabet.getSelectionModel().getSelectedItem());
            FAGraph dfa = FAGraph.toDFA(graph);
            FAGraph min_dfa = FAGraph.minimize(dfa);
            textArea_display.setText(graph.toDotString());
            image_nfa.setImage(SwingFXUtils.toFXImage(graph.getImage(), null));
            image_dfa.setImage(SwingFXUtils.toFXImage(dfa.getImage(), null));
            image_min_dfa.setImage(SwingFXUtils.toFXImage(min_dfa.getImage(), null));
        } catch (ParserException e) {
            textArea_display.setText(e.getMessage());
            image_nfa.setImage(null);
            image_dfa.setImage(null);
            image_min_dfa.setImage(null);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        choiceBox_alphabet.getItems().setAll(Alphabet.values());
        choiceBox_alphabet.getSelectionModel().select(Alphabet.Binary);
    }
}

