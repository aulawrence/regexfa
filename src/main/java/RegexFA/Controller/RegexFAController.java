package RegexFA.Controller;

import RegexFA.Alphabet;
import RegexFA.Model.RegexFAModel;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.input.KeyEvent;
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

import static RegexFA.Model.GraphPanelModel.GraphChoice;


public class RegexFAController extends Controller<RegexFAModel> {
    @FXML
    private ChoiceBox<Alphabet> choiceBox_alphabet;
    @FXML
    private TextFlow textFlow_testString;
    @FXML
    private Node textInputView_regex;
    @FXML
    private Node textInputView_testString;
    @FXML
    private Node graphPanel;

    @FXML
    private TextInputController textInputView_regexController;
    @FXML
    private TextInputController textInputView_testStringController;
    @FXML
    private GraphPanelController graphPanelController;

    private final ArrayList<Text> testStringArrayList;
    private final ExecutorService executor;

    private final HashMap<GraphChoice, Path> imagePath;
    private final HashMap<GraphChoice, Boolean> imageSubscription;

    private GraphChoice dotStringChoice = GraphChoice.Graph1;

    public RegexFAController() throws IOException {
        super(new RegexFAModel());
        testStringArrayList = new ArrayList<>();
        executor = Executors.newFixedThreadPool(3);
        imagePath = new HashMap<>();
        imageSubscription = new HashMap<>();
        for (GraphChoice graphChoice : GraphChoice.values()) {
            imagePath.put(graphChoice, null);
            imageSubscription.put(graphChoice, true);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        choiceBox_alphabet.getItems().setAll(Alphabet.values());
        choiceBox_alphabet.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != oldValue) {
                model.setAlphabet(newValue);
                if (!model.getRegex().equals("")) {
                    textInputView_regexController.getObserver().onNext(new TextInputController.Message.RecvToggle(2));
                }
            }
        });
        choiceBox_alphabet.getSelectionModel().select(Alphabet.Binary);

        textInputView_regexController.getObserver().onNext(new TextInputController.Message.RecvLabel("Regex:"));
        textInputView_regexController.getObserver().onNext(new TextInputController.Message.RecvHideResult(false));
        textInputView_regexController.getObserver().onNext(new TextInputController.Message.RecvEnabled(true));
        textInputView_regexController.getObservable().subscribe(new Observer<TextInputController.Message.EmitBase>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(TextInputController.Message.@NonNull EmitBase emitBase) {
                handle(TextChoice.Regex, emitBase);
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });

        textInputView_testStringController.getObserver().onNext(new TextInputController.Message.RecvLabel("Test String:"));
        textInputView_testStringController.getObserver().onNext(new TextInputController.Message.RecvHideResult(true));
        textInputView_testStringController.getObserver().onNext(new TextInputController.Message.RecvEnabled(false));
        textInputView_testStringController.getObservable().subscribe(new Observer<TextInputController.Message.EmitBase>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(TextInputController.Message.@NonNull EmitBase emitBase) {
                handle(TextChoice.TestString, emitBase);
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });

        graphPanelController.setLabel("Graph (Dot Notation):");
        graphPanelController.setGraphLabel(GraphChoice.Graph1, "NFA");
        graphPanelController.setGraphLabel(GraphChoice.Graph2, "DFA");
        graphPanelController.setGraphLabel(GraphChoice.Graph3, "Min-DFA");
        graphPanelController.getObservable().subscribe(new Observer<GraphPanelController.Message.EmitBase>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(GraphPanelController.Message.@NonNull EmitBase emitBase) {
                handle(emitBase);
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
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

    private void handle(GraphPanelController.Message.EmitBase emitBase) {
        if (emitBase instanceof GraphPanelController.Message.EmitGraphFocus) {
            GraphPanelController.Message.EmitGraphFocus msg = (GraphPanelController.Message.EmitGraphFocus) emitBase;
            handle(msg);
        } else if (emitBase instanceof GraphPanelController.Message.EmitImageSubscription) {
            GraphPanelController.Message.EmitImageSubscription msg = (GraphPanelController.Message.EmitImageSubscription) emitBase;
            handle(msg);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private void handle(GraphPanelController.Message.EmitImageSubscription msg) {
        imageSubscription.put(msg.graphChoice, msg.subscribe);
        updateImages(true, msg.graphChoice);
    }

    private void handle(GraphPanelController.Message.EmitGraphFocus msg) {
        dotStringChoice = msg.graphChoice;
        updateDotString();
    }

    private void handle(TextChoice choice, TextInputController.Message.EmitBase emitBase) {
        if (emitBase instanceof TextInputController.Message.EmitSubmit) {
            handle(choice, (TextInputController.Message.EmitSubmit) emitBase);
        } else if (emitBase instanceof TextInputController.Message.EmitStartEditing) {
            handle(choice, (TextInputController.Message.EmitStartEditing) emitBase);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private void handle(TextChoice choice, TextInputController.Message.EmitSubmit msg) {
        switch (choice) {
            case Regex:
                executor.execute(
                        () -> {
                            model.setRegex(msg.string);
                            Platform.runLater(
                                    () -> {
                                        updateRegex();
                                        updateDotString();
                                        textInputView_regexController.getObserver().onNext(new TextInputController.Message.RecvToggle(msg.count));
                                    }
                            );
                        }
                );
                break;
            case TestString:
                executor.execute(
                        () -> {
                            model.setTestString(msg.string);
                            Platform.runLater(
                                    () -> {
                                        updateTestString();
                                        textInputView_testStringController.getObserver().onNext(new TextInputController.Message.RecvToggle(msg.count));
                                    }
                            );
                        }
                );
                break;
            default:
                throw new IllegalStateException();
        }
    }

    private void handle(TextChoice choice, TextInputController.Message.EmitStartEditing msg) {
        switch (choice) {
            case Regex:
                textInputView_regexController.getObserver().onNext(new TextInputController.Message.RecvToggle(msg.count));
                break;
            case TestString:
                textFlow_testString.setVisible(false);
                textInputView_testStringController.getObserver().onNext(new TextInputController.Message.RecvToggle(msg.count));
                break;
            default:
                throw new IllegalStateException();
        }
    }

    private void updateRegex() {
        if (model.isRegexSuccess()) {
            textInputView_regexController.getObserver().onNext(new TextInputController.Message.RecvResult(true, model.getRegex()));
            textInputView_testStringController.getObserver().onNext(new TextInputController.Message.RecvEnabled(true));
            textInputView_testStringController.getObserver().onNext(new TextInputController.Message.RecvToggle(2));
        } else {
            textInputView_regexController.getObserver().onNext(new TextInputController.Message.RecvResult(false, model.getRegexErrMsg()));
            textInputView_testStringController.getObserver().onNext(new TextInputController.Message.RecvEnabled(false));
        }
    }

    private void updateTestString() {
        if (model.isTestStringSuccess()) {
            textInputView_testStringController.getObserver().onNext(new TextInputController.Message.RecvResult(true, model.getTestString()));
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
            textInputView_testStringController.getObserver().onNext(new TextInputController.Message.RecvResult(false, model.getTestStringErrorMsg()));
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
            graphPanelController.getObserver().onNext(new GraphPanelController.Message.ReceiveText(model.getDotString(dotStringChoice)));
        } else {
            graphPanelController.getObserver().onNext(new GraphPanelController.Message.ReceiveText(""));
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
                                    () -> graphPanelController.getObserver().onNext(new GraphPanelController.Message.ReceiveImage(graphChoice, imagePath.get(graphChoice)))
                            );
                        }
                );
            } else if (!nullOnly && !imageSubscription.get(graphChoice)) {
                imagePath.put(graphChoice, null);
                Platform.runLater(
                        () -> graphPanelController.getObserver().onNext(new GraphPanelController.Message.ReceiveImage(graphChoice, null))
                );
            }
        } else {
            imagePath.put(graphChoice, null);
            Platform.runLater(
                    () -> graphPanelController.getObserver().onNext(new GraphPanelController.Message.ReceiveImage(graphChoice, null))
            );
        }
    }

    public void shutdown() {
        executor.shutdown();
        model.close();
    }

    private enum TextChoice {
        Regex,
        TestString;
    }
}

