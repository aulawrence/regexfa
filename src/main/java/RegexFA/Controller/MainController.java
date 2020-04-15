package RegexFA.Controller;

import RegexFA.Alphabet;
import RegexFA.Model.MainModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static RegexFA.Model.MainModel.GraphChoice.*;


public class MainController extends Controller<MainModel> {
    public TextArea textArea_display;
    public ChoiceBox<Alphabet> choiceBox_alphabet;
    public ImageView image_nfa;
    public ImageView image_dfa;
    public ImageView image_min_dfa;
    public Label label_nfa;
    public Label label_dfa;
    public Label label_min_dfa;
    public TextField textField_regex;
    public Text text_regex;
    public Text text_errRegex;
    public Button button_regex;
    public TextField textField_testString;
    public Text text_errTestString;
    public Button button_testString;
    public TextFlow textFlow_testString;

    private final ArrayList<Text> testStringArrayList;
    private final ExecutorService executor;


    private boolean regexEditing = true;
    private boolean testStringEditing = true;

    public MainController() {
        this(new MainModel());
    }

    protected MainController(MainModel model) {
        super(model);
        testStringArrayList = new ArrayList<>();
        executor = Executors.newFixedThreadPool(3);
    }

    @FXML
    private void onClick_buttonRegex(MouseEvent event) {
        toggleRegex();
    }

    @FXML
    private void onClick_buttonTestString(MouseEvent event) {
        toggleTestString();
    }

    public void onClick_NFA(MouseEvent mouseEvent) {
        modelSetSelection(NFA);
    }

    public void onClick_DFA(MouseEvent mouseEvent) {
        modelSetSelection(DFA);
    }

    public void onClick_MinDFA(MouseEvent mouseEvent) {
        modelSetSelection(MinDFA);
    }

    private void modelSetSelection(MainModel.GraphChoice choice) {
        executor.execute(
                () -> {
                    model.setSelection(choice);
                    Platform.runLater(
                            () -> {
                                updateTextArea();
                                updateLabels();
                            }
                    );
                }
        );
    }

    private void toggleRegex() {
        if (regexEditing) {
            executor.execute(
                    () -> {
                        model.setRegex(textField_regex.getText());
                        Platform.runLater(
                                () -> {
                                    updateRegex();
                                    updateImages();
                                    updateTextArea();
                                }
                        );
                    }
            );
        } else {
            updateRegex();
        }
    }

    private void toggleTestString() {
        if (testStringEditing) {
            executor.execute(
                    () -> {
                        model.setTestString(textField_testString.getText());
                        Platform.runLater(
                                () -> updateTestString()
                        );
                    }
            );
        } else {
            updateTestString();
        }
    }

    private void updateTextArea() {
        if (model.isRegexSuccess()) {
            textArea_display.setText(model.getDotString());
        } else {
            textArea_display.setText("");
        }
    }

    private void updateImages() {
        if (model.isRegexSuccess()) {
            executor.execute(
                    () -> {
                        try {
                            Image i_nfa = new Image(model.getImageStream(NFA));
                            Image i_dfa = new Image(model.getImageStream(DFA));
                            Image i_min_dfa = new Image(model.getImageStream(MinDFA));

                            Platform.runLater(
                                    () -> {
                                        image_nfa.setImage(i_nfa);
                                        image_dfa.setImage(i_dfa);
                                        image_min_dfa.setImage(i_min_dfa);
                                    }
                            );
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
            );
        } else {
            image_nfa.setImage(null);
            image_dfa.setImage(null);
            image_min_dfa.setImage(null);
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

    private void updateRegex() {
        if (regexEditing) {
            if (model.isRegexSuccess()) {
                button_regex.setText("Edit");
                textField_regex.setVisible(false);
                text_regex.setVisible(true);
                text_regex.setText(model.getRegex());
                text_errRegex.setVisible(false);
                regexEditing = false;

                textField_testString.setDisable(false);
                button_testString.setDisable(false);
                if (!model.getTestString().equals("")) {
                    toggleTestString();
                    toggleTestString();
                }
            } else {
                text_errRegex.setVisible(true);
                text_errRegex.setText(model.getRegexErrMsg());
            }
        } else {
            button_regex.setText("Ok");
            textField_regex.setVisible(true);
            text_regex.setVisible(false);
            regexEditing = true;

            textField_testString.setDisable(true);
            button_testString.setDisable(true);
        }
    }

    private void updateTestString() {
        if (testStringEditing) {
            if (model.isTestStringSuccess()) {
                button_testString.setText("Edit");
                textField_testString.setVisible(false);
                text_errTestString.setVisible(false);
                testStringEditing = false;

                String testString = model.getTestString();
                model.setTestStringPos(testString.length() - 1);
                testStringArrayList.clear();
                for (int i = 0; i < testString.length(); i++) {
                    int pos = i;
                    Text node = new Text(Character.toString(testString.charAt(i)));
                    node.setOnMouseClicked(event -> {
                        model.setTestStringPos(pos);
                        updatePos();
                    });
                    testStringArrayList.add(node);
                }
                textFlow_testString.getChildren().setAll(testStringArrayList);
                textFlow_testString.setVisible(true);
                updatePos();
            } else {
                text_errTestString.setVisible(true);
                text_errTestString.setText(model.getTestStringErrorMsg());
            }
        } else {
            button_testString.setText("Ok");
            textField_testString.setVisible(true);
            textFlow_testString.setVisible(false);
            testStringEditing = true;
        }
    }

    private void updatePos() {
        int pos = model.getTestStringPos();

        if (pos == -1) {
            for (int i = 0; i < testStringArrayList.size(); i++) {
                testStringArrayList.get(i).styleProperty().setValue("");
            }
        } else {
            for (int i = 0; i < testStringArrayList.size(); i++) {
                if (i < pos) {
                    testStringArrayList.get(i).styleProperty().setValue("-fx-underline:true");
                } else if (i == pos) {
                    testStringArrayList.get(i).styleProperty().setValue("-fx-font-weight: bold; -fx-underline:true");
                } else {
                    testStringArrayList.get(i).styleProperty().setValue("");
                }
            }

        }
        updateTextArea();
        updateImages();
        textFlow_testString.requestFocus();
    }

    public void onKeyPressed_textFlowTestString(KeyEvent keyEvent) {
        if (model.isRegexSuccess() && model.isTestStringSuccess()) {
            switch (keyEvent.getCode()) {
                case UP:
                case DOWN:
                case LEFT:
                case RIGHT:
                    keyEvent.consume();
                    break;
            }
        }
    }

    public void onKeyReleased_textFlowTestString(KeyEvent keyEvent) {
        if (model.isRegexSuccess() && model.isTestStringSuccess()) {
            int curr = model.getTestStringPos();
            int target = curr;
            int lim = model.getTestString().length() - 1;
            switch (keyEvent.getCode()) {
                case UP:
                    target = -1;
                    break;
                case DOWN:
                    target = lim;
                    break;
                case LEFT:
                    target = curr - 1;
                    break;
                case RIGHT:
                    target = curr + 1;
                    break;
            }
            target = Integer.max(-1, Integer.min(target, lim));
            if (target != curr) {
                model.setTestStringPos(target);
                updatePos();
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        choiceBox_alphabet.getItems().setAll(Alphabet.values());
        choiceBox_alphabet.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != oldValue) {
                model.setAlphabet(newValue);
                if (!model.getRegex().equals("")) {
                    toggleRegex();
                    toggleRegex();
                }
            }
        });
        choiceBox_alphabet.getSelectionModel().select(Alphabet.Binary);

        text_regex.setVisible(false);
        text_errRegex.setVisible(false);
        textFlow_testString.setVisible(false);
        text_errTestString.setVisible(false);

        updateLabels();
    }

    public void shutdown() {
        executor.shutdown();
    }

}

