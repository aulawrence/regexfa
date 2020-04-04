package RegexFA.Controller;

import RegexFA.Alphabet;
import RegexFA.Model.MainModel;
import RegexFA.Parser.ParserException;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.util.ResourceBundle;

import static RegexFA.Model.MainModel.GraphChoice.*;


public class MainController extends Controller<MainModel> {
    public TextArea textArea_regex;
    public Button button_regex;
    public TextArea textArea_display;
    public ChoiceBox<Alphabet> choiceBox_alphabet;
    public ImageView image_nfa;
    public ImageView image_dfa;
    public ImageView image_min_dfa;
    public Label label_nfa;
    public Label label_dfa;
    public Label label_min_dfa;

    public MainController() {
        this(new MainModel());
    }

    protected MainController(MainModel model) {
        super(model);
    }

    @FXML
    private void onClick_buttonRegex(MouseEvent event) {
        String s = textArea_regex.getText();
        Alphabet alphabet = choiceBox_alphabet.getSelectionModel().getSelectedItem();
        try {
            model.generate_graph(s, alphabet);
            textArea_display.setText(model.getDotString());
            image_nfa.setImage(SwingFXUtils.toFXImage(model.getImage(NFA), null));
            image_dfa.setImage(SwingFXUtils.toFXImage(model.getImage(DFA), null));
            image_min_dfa.setImage(SwingFXUtils.toFXImage(model.getImage(MinDFA), null));
        } catch (ParserException e) {
            textArea_display.setText(e.getMessage());
            image_nfa.setImage(null);
            image_dfa.setImage(null);
            image_min_dfa.setImage(null);
        }
    }

    public void onClick_NFA(MouseEvent mouseEvent) {
        model.setSelection(NFA);
        updateText();
        updateLabels();
    }

    public void onClick_DFA(MouseEvent mouseEvent) {
        model.setSelection(DFA);
        updateText();
        updateLabels();
    }

    public void onClick_MinDFA(MouseEvent mouseEvent) {
        model.setSelection(MinDFA);
        updateText();
        updateLabels();
    }

    private void updateText() {
        if (model.isSuccess()) {
            textArea_display.setText(model.getDotString());
        }
    }

    private void updateLabels() {
        label_nfa.setUnderline(false);
        label_nfa.setStyle("-fx-font-weight: normal;");
        label_dfa.setUnderline(false);
        label_dfa.setStyle("-fx-font-weight: normal;");
        label_min_dfa.setUnderline(false);
        label_min_dfa.setStyle("-fx-font-weight: normal;");
        switch (model.getSelection()) {
            case DFA:
                label_dfa.setUnderline(true);
                label_dfa.setStyle("-fx-font-weight: bold;");
                break;
            case NFA:
                label_nfa.setUnderline(true);
                label_nfa.setStyle("-fx-font-weight: bold;");
                break;
            case MinDFA:
                label_min_dfa.setUnderline(true);
                label_min_dfa.setStyle("-fx-font-weight: bold;");
                break;
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        choiceBox_alphabet.getItems().setAll(Alphabet.values());
        choiceBox_alphabet.getSelectionModel().select(Alphabet.Binary);
        updateLabels();
    }


}

