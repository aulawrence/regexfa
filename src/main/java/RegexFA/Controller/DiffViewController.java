package RegexFA.Controller;

import RegexFA.Alphabet;
import RegexFA.Model.DiffViewModel;
import RegexFA.Model.GraphViewModel;
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

public class DiffViewController extends Controller<DiffViewModel> {
    private final ArrayList<Text> testStringArrayList;
    private final ExecutorService executor;
    private final HashMap<GraphViewModel.GraphChoice, Path> imagePathMap;
    private final HashMap<GraphViewModel.GraphChoice, Boolean> imageSubscriptionMap;
    @FXML
    private ChoiceBox<Alphabet> choiceBox_alphabet;
    @FXML
    private Node textInputView_regex1;
    @FXML
    private Node textInputView_regex2;
    @FXML
    private TextFlow textFlow_testString;
    @FXML
    private Node textInputView_testString;
    @FXML
    private Node graphView;
    @FXML
    private TextInputViewController textInputView_regex1Controller;
    @FXML
    private TextInputViewController textInputView_regex2Controller;
    @FXML
    private TextInputViewController textInputView_testStringController;
    @FXML
    private GraphViewController graphViewController;

    public DiffViewController() throws IOException {
        super(new DiffViewModel());
        testStringArrayList = new ArrayList<>();
        executor = Executors.newFixedThreadPool(3);
        imagePathMap = new HashMap<>();
        imageSubscriptionMap = new HashMap<>();
        for (GraphViewModel.GraphChoice graphChoice : GraphViewModel.GraphChoice.values()) {
            imagePathMap.put(graphChoice, null);
            imageSubscriptionMap.put(graphChoice, true);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        choiceBox_alphabet.getItems().setAll(Alphabet.values());
        choiceBox_alphabet.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != oldValue) {
                model.setAlphabet(newValue);
                if (!model.getRegex1().equals("")) {
                    textInputView_regex1Controller.getObserver().onNext(new TextInputViewController.Message.RecvToggle(2));
                }
                if (!model.getRegex2().equals("")) {
                    textInputView_regex2Controller.getObserver().onNext(new TextInputViewController.Message.RecvToggle(2));
                }
            }
        });
        choiceBox_alphabet.getSelectionModel().select(Alphabet.Binary);

        textInputView_regex1Controller.getObserver().onNext(new TextInputViewController.Message.RecvLabel("Pattern1:"));
        textInputView_regex1Controller.getObserver().onNext(new TextInputViewController.Message.RecvHideResult(false));
        textInputView_regex1Controller.getObserver().onNext(new TextInputViewController.Message.RecvEnabled(true));
        textInputView_regex1Controller.getObservable().subscribe(new Observer<TextInputViewController.Message.EmitBase>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(TextInputViewController.Message.@NonNull EmitBase emitBase) {
                handle(TextChoice.Regex1, emitBase);
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });

        textInputView_regex2Controller.getObserver().onNext(new TextInputViewController.Message.RecvLabel("Pattern2:"));
        textInputView_regex2Controller.getObserver().onNext(new TextInputViewController.Message.RecvHideResult(false));
        textInputView_regex2Controller.getObserver().onNext(new TextInputViewController.Message.RecvEnabled(true));
        textInputView_regex2Controller.getObservable().subscribe(new Observer<TextInputViewController.Message.EmitBase>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(TextInputViewController.Message.@NonNull EmitBase emitBase) {
                handle(TextChoice.Regex2, emitBase);
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });


        textInputView_testStringController.getObserver().onNext(new TextInputViewController.Message.RecvLabel("Test String:"));
        textInputView_testStringController.getObserver().onNext(new TextInputViewController.Message.RecvHideResult(true));
        textInputView_testStringController.getObserver().onNext(new TextInputViewController.Message.RecvEnabled(false));
        textInputView_testStringController.getObservable().subscribe(new Observer<TextInputViewController.Message.EmitBase>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(TextInputViewController.Message.@NonNull EmitBase emitBase) {
                handle(TextChoice.TestString, emitBase);
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });

        graphViewController.setLabel("Result:");
        graphViewController.setGraphLabel(GraphViewModel.GraphChoice.Graph1, "Pattern1 Min-DFA");
        graphViewController.setGraphLabel(GraphViewModel.GraphChoice.Graph2, "Pattern2 Min-DFA");
        graphViewController.setGraphLabel(GraphViewModel.GraphChoice.Graph3, "Xor Min-DFA");
        graphViewController.getObservable().subscribe(new Observer<GraphViewController.Message.EmitBase>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(GraphViewController.Message.@NonNull EmitBase emitBase) {
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
        if (model.isGraphSuccess() && model.isTestStringSuccess()) {
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
        if (model.isGraphSuccess() && model.isTestStringSuccess()) {
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

    private void handle(TextChoice choice, TextInputViewController.Message.EmitBase emitBase) {
        if (emitBase instanceof TextInputViewController.Message.EmitSubmit) {
            handle(choice, (TextInputViewController.Message.EmitSubmit) emitBase);
        } else if (emitBase instanceof TextInputViewController.Message.EmitStartEditing) {
            handle(choice, (TextInputViewController.Message.EmitStartEditing) emitBase);
        } else {
            throw new IllegalStateException();
        }
    }

    private void handle(TextChoice choice, TextInputViewController.Message.EmitSubmit msg) {
        switch (choice) {
            case Regex1:
                executor.execute(
                        () -> {
                            model.setRegex1(msg.string);
                            Platform.runLater(
                                    () -> {
                                        updateRegex1();
                                        updateText();
                                        textInputView_regex1Controller.getObserver().onNext(new TextInputViewController.Message.RecvToggle(msg.count));
                                    }
                            );
                        }
                );
                break;
            case Regex2:
                executor.execute(
                        () -> {
                            model.setRegex2(msg.string);
                            Platform.runLater(
                                    () -> {
                                        updateRegex2();
                                        updateText();
                                        textInputView_regex2Controller.getObserver().onNext(new TextInputViewController.Message.RecvToggle(msg.count));
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
                                        textInputView_testStringController.getObserver().onNext(new TextInputViewController.Message.RecvToggle(msg.count));
                                    }
                            );

                        }
                );
                break;
            default:
                throw new IllegalStateException();
        }
    }

    private void handle(TextChoice choice, TextInputViewController.Message.EmitStartEditing msg) {
        switch (choice) {
            case Regex1:
                textInputView_regex1Controller.getObserver().onNext(new TextInputViewController.Message.RecvToggle(msg.count));
                break;
            case Regex2:
                textInputView_regex2Controller.getObserver().onNext(new TextInputViewController.Message.RecvToggle(msg.count));
                break;
            case TestString:
                textFlow_testString.setVisible(false);
                textInputView_testStringController.getObserver().onNext(new TextInputViewController.Message.RecvToggle(msg.count));
                break;
            default:
                throw new IllegalStateException();
        }
    }


    private void handle(GraphViewController.Message.EmitBase emitBase) {
        if (emitBase instanceof GraphViewController.Message.EmitGraphFocus) {
            handle((GraphViewController.Message.EmitGraphFocus) emitBase);
        } else if (emitBase instanceof GraphViewController.Message.EmitImageSubscription) {
            handle((GraphViewController.Message.EmitImageSubscription) emitBase);
        }
    }

    private void handle(GraphViewController.Message.EmitGraphFocus msg) {
        // Empty
    }

    private void handle(GraphViewController.Message.EmitImageSubscription msg) {
        imageSubscriptionMap.put(msg.graphChoice, msg.subscribe);
        updateImages(true, msg.graphChoice);
    }

    private void updateRegex1() {
        if (model.isRegex1Success()) {
            textInputView_regex1Controller.getObserver().onNext(new TextInputViewController.Message.RecvResult(true, model.getRegex1()));
            if (model.isGraphSuccess()) {
                textInputView_testStringController.getObserver().onNext(new TextInputViewController.Message.RecvEnabled(true));
                textInputView_testStringController.getObserver().onNext(new TextInputViewController.Message.RecvToggle(2));
            }
        } else {
            textInputView_regex1Controller.getObserver().onNext(new TextInputViewController.Message.RecvResult(false, model.getRegex1ErrMsg()));
            textInputView_testStringController.getObserver().onNext(new TextInputViewController.Message.RecvEnabled(false));
        }
    }

    private void updateRegex2() {
        if (model.isRegex2Success()) {
            textInputView_regex2Controller.getObserver().onNext(new TextInputViewController.Message.RecvResult(true, model.getRegex2()));
            if (model.isGraphSuccess()) {
                textInputView_testStringController.getObserver().onNext(new TextInputViewController.Message.RecvEnabled(true));
                textInputView_testStringController.getObserver().onNext(new TextInputViewController.Message.RecvToggle(2));
            }
        } else {
            textInputView_regex2Controller.getObserver().onNext(new TextInputViewController.Message.RecvResult(false, model.getRegex2ErrMsg()));
            textInputView_testStringController.getObserver().onNext(new TextInputViewController.Message.RecvEnabled(false));
        }
    }

    private void updateTestString() {
        if (model.isTestStringSuccess()) {
            textInputView_testStringController.getObserver().onNext(new TextInputViewController.Message.RecvResult(true, model.getTestString()));
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
            textInputView_testStringController.getObserver().onNext(new TextInputViewController.Message.RecvResult(false, model.getTestStringErrorMsg()));
        }
    }

    private void updateText() {
        graphViewController.getObserver().onNext(new GraphViewController.Message.ReceiveText(model.getGraphMessage()));
    }

    private void updateImages() {
        for (GraphViewModel.GraphChoice graphChoice : GraphViewModel.GraphChoice.values()) {
            updateImages(false, graphChoice);
        }
    }

    private void updateImages(boolean nullOnly, GraphViewModel.GraphChoice graphChoice) {
        if (model.isGraphSuccess()) {
            if (imageSubscriptionMap.get(graphChoice) && (!nullOnly || imagePathMap.get(graphChoice) == null)) {
                executor.execute(
                        () -> {
                            imagePathMap.put(graphChoice, model.getImage(graphChoice, graphChoice.toString()));
                            Platform.runLater(
                                    () -> graphViewController.getObserver().onNext(new GraphViewController.Message.ReceiveImage(graphChoice, imagePathMap.get(graphChoice)))
                            );
                        }
                );
            } else if (!nullOnly && !imageSubscriptionMap.get(graphChoice)) {
                imagePathMap.put(graphChoice, null);
                Platform.runLater(
                        () -> graphViewController.getObserver().onNext(new GraphViewController.Message.ReceiveImage(graphChoice, null))
                );
            }
        } else {
            imagePathMap.put(graphChoice, null);
            Platform.runLater(
                    () -> graphViewController.getObserver().onNext(new GraphViewController.Message.ReceiveImage(graphChoice, null))
            );
        }
    }

    private void updatePos() {
        int pos = model.getTestStringPos();
        for (int i = 0; i < testStringArrayList.size(); i++) {
            StringBuilder sb = new StringBuilder();
            if (i < pos) {
                sb.append("-fx-underline:true; ");
            } else if (i == pos) {
                sb.append("-fx-font-weight: bold; -fx-underline:true; ");
            }
            testStringArrayList.get(i).styleProperty().setValue(sb.toString());
        }
        updateImages();
        textFlow_testString.requestFocus();
    }

    public void shutdown() {
        executor.shutdown();
        model.close();
    }

    private enum TextChoice {
        Regex1,
        Regex2,
        TestString;
    }
}
