package RegexFA.Controller;

import RegexFA.Alphabet;
import RegexFA.Model.MainModel;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static RegexFA.Model.GraphViewModel.GraphChoice;


public class MainController extends Controller<MainModel> {
    public ChoiceBox<Alphabet> choiceBox_alphabet;
    public TextField textField_regex;
    public Text text_regex;
    public Text text_errRegex;
    public Button button_regex;
    public TextField textField_testString;
    public Text text_errTestString;
    public Button button_testString;
    public TextFlow textFlow_testString;
    public AnchorPane anchorPane_graphView;

    private GraphViewController graphViewController;

    private final ArrayList<Text> testStringArrayList;
    private final ExecutorService executor;

    private final HashMap<GraphChoice, Path> imagePath;
    private final HashMap<GraphChoice, Boolean> imageSubscription;

    private GraphChoice dotStringChoice = GraphChoice.NFA;

    private boolean regexEditing = true;
    private boolean testStringEditing = true;

    public MainController() throws IOException {
        super(new MainModel());
        testStringArrayList = new ArrayList<>();
        executor = Executors.newFixedThreadPool(3);
        imagePath = new HashMap<>();
        imageSubscription = new HashMap<>();
        for (GraphChoice graphChoice : GraphChoice.values()) {
            imagePath.put(graphChoice, null);
            imageSubscription.put(graphChoice, true);
        }
    }

    @FXML
    private void onClick_buttonRegex(MouseEvent event) {
        toggleRegex();
    }

    @FXML
    private void onClick_buttonTestString(MouseEvent event) {
        toggleTestString();
    }

    @FXML
    private void onKeyReleased_buttonRegex(KeyEvent keyEvent) {
        switch (keyEvent.getCode()) {
            case SPACE:
            case ENTER:
                toggleRegex();
                break;
        }
    }

    @FXML
    private void onKeyReleased_buttonTestString(KeyEvent keyEvent) {
        switch (keyEvent.getCode()) {
            case SPACE:
            case ENTER:
                toggleTestString();
                break;
        }
    }

    @FXML
    private void onKeyReleased_textFieldRegex(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            toggleRegex();
        }
    }

    @FXML
    private void onKeyReleased_textFieldTestString(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            toggleTestString();
        }
    }

    @FXML
    private void onKeyPressed_textFlowTestString(KeyEvent keyEvent) {
        if (model.isRegexSuccess() && model.isTestStringSuccess()) {
            switch (keyEvent.getCode()) {
                case HOME:
                case END:
                case UP:
                case DOWN:
                case LEFT:
                case RIGHT:
                    keyEvent.consume();
                    break;
            }
        }
    }

    @FXML
    private void onKeyReleased_textFlowTestString(KeyEvent keyEvent) {
        if (model.isRegexSuccess() && model.isTestStringSuccess()) {
            int curr = model.getTestStringPos();
            int target = curr;
            int lim = model.getTestString().length() - 1;
            switch (keyEvent.getCode()) {
                case UP:
                case HOME:
                    target = -1;
                    break;
                case DOWN:
                case END:
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

    private void toggleRegex() {
        if (regexEditing) {
            executor.execute(
                    () -> {
                        model.setRegex(textField_regex.getText());
                        Platform.runLater(
                                () -> {
                                    updateRegex();
                                    updateDotString();
                                    updateImages();
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

    private void handle(GraphViewController.Message.RequestImageSubscription msg) {
        imageSubscription.put(msg.graphChoice, msg.subscribe);
        updateImages(true, msg.graphChoice);
    }

    private void handle(GraphViewController.Message.RequestDotString msg) {
        dotStringChoice = msg.graphChoice;
        updateDotString();
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
        for (int i = 0; i < testStringArrayList.size(); i++) {
            StringBuilder sb = new StringBuilder();
            if (model.getTestStringAcceptance(i)) {
                sb.append("-fx-fill: darkgreen; ");
            }
            if (i < pos) {
                sb.append("-fx-underline:true; ");
            } else if (i == pos) {
                sb.append("-fx-font-weight: bold; -fx-underline:true; ");
            }
            testStringArrayList.get(i).styleProperty().setValue(sb.toString());
        }
        updateDotString();
        updateImages();
        textFlow_testString.requestFocus();
    }

    private void updateDotString() {
        if (model.isRegexSuccess()) {
            graphViewController.getObserver().onNext(new GraphViewController.Message.ReceiveDotString(model.getDotString(dotStringChoice)));
        } else {
            graphViewController.getObserver().onNext(new GraphViewController.Message.ReceiveDotString(""));
        }
    }

    private void updateImages() {
        for (GraphChoice graphChoice : GraphChoice.values()) {
            updateImages(false, graphChoice);
        }
    }

    private void updateImages(boolean nullOnly, GraphChoice graphChoice) {
        if (model.isRegexSuccess()) {
            if (imageSubscription.get(graphChoice) && (!nullOnly || imagePath.get(graphChoice) == null)) {
                executor.execute(
                        () -> {
                            imagePath.put(graphChoice, model.getImage(graphChoice, graphChoice.toString()));
                            Platform.runLater(
                                    () -> graphViewController.getObserver().onNext(new GraphViewController.Message.ReceiveImage(graphChoice, imagePath.get(graphChoice)))
                            );
                        }
                );
            } else if (!nullOnly && !imageSubscription.get(graphChoice)) {
                imagePath.put(graphChoice, null);
                Platform.runLater(
                        () -> graphViewController.getObserver().onNext(new GraphViewController.Message.ReceiveImage(graphChoice, null))
                );
            }
        } else {
            imagePath.put(graphChoice, null);
            Platform.runLater(
                    () -> graphViewController.getObserver().onNext(new GraphViewController.Message.ReceiveImage(graphChoice, null))
            );
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

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/graphView.fxml"));
        try {
            Node node = loader.load();
            graphViewController = loader.getController();
            graphViewController.getObservable().subscribe(new Observer<GraphViewController.Message.EmitBase>() {
                @Override
                public void onSubscribe(@NonNull Disposable d) {

                }

                @Override
                public void onNext(GraphViewController.Message.EmitBase emitBase) {
                    if (emitBase instanceof GraphViewController.Message.RequestDotString) {
                        GraphViewController.Message.RequestDotString msg = (GraphViewController.Message.RequestDotString) emitBase;
                        handle(msg);
                    } else if (emitBase instanceof GraphViewController.Message.RequestImageSubscription) {
                        GraphViewController.Message.RequestImageSubscription msg = (GraphViewController.Message.RequestImageSubscription) emitBase;
                        handle(msg);
                    }
                }

                @Override
                public void onError(@NonNull Throwable e) {

                }

                @Override
                public void onComplete() {

                }
            });
            anchorPane_graphView.getChildren().add(node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        executor.shutdown();
        model.close();
    }
}

