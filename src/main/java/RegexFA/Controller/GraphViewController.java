package RegexFA.Controller;

import RegexFA.Model.GraphViewModel;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;

public class GraphViewController extends Controller<GraphViewModel> {
    public static class EmitMessage {
        public static abstract class Base {
        }

        public static class RequestDotString extends Base {
            public final GraphViewModel.GraphChoice graphChoice;

            public RequestDotString(GraphViewModel.GraphChoice graphChoice) {
                this.graphChoice = graphChoice;
            }
        }

        public static class RequestImageSubscription extends Base {
            public final GraphViewModel.GraphChoice graphChoice;
            public final boolean subscribe;

            public RequestImageSubscription(GraphViewModel.GraphChoice graphChoice, boolean subscribe) {
                this.graphChoice = graphChoice;
                this.subscribe = subscribe;
            }
        }
    }

    public static class RecvMessage {
        public static abstract class Base {
        }

        public static class ReceiveDotString extends Base {
            public final String dotString;

            public ReceiveDotString(String dotString) {
                this.dotString = dotString;
            }
        }

        public static class ReceiveImage extends Base {
            public final GraphViewModel.GraphChoice graphChoice;
            public final Path imagePath;

            public ReceiveImage(GraphViewModel.GraphChoice graphChoice, Path imagePath) {
                this.graphChoice = graphChoice;
                this.imagePath = imagePath;
            }
        }
    }


    public GridPane gridPane_images;
    public TextArea textArea_display;
    public Label label_nfa;
    public Label label_dfa;
    public Label label_min_dfa;
    public ImageView imageView_nfa;
    public ImageView imageView_dfa;
    public ImageView imageView_min_dfa;
    public Slider slider_nfa;
    public Slider slider_dfa;
    public Slider slider_min_dfa;

    private final ExecutorService executor;
    private final PublishSubject<EmitMessage.Base> observable;
    private final Observer<RecvMessage.Base> observer;

    private final HashMap<GraphViewModel.GraphChoice, Label> labelHashMap;
    private final HashMap<GraphViewModel.GraphChoice, ImageView> imageViewHashMap;
    private final HashMap<GraphViewModel.GraphChoice, Slider> sliderHashMap;

    protected GraphViewController(GraphViewModel model, ExecutorService executor) {
        super(model);
        this.executor = executor;
        observable = PublishSubject.create();
        observer = new Observer<>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(RecvMessage.@NonNull Base base) {
                if (base instanceof RecvMessage.ReceiveDotString) {
                    RecvMessage.ReceiveDotString msg = (RecvMessage.ReceiveDotString) base;
                    handle(msg);
                } else if (base instanceof RecvMessage.ReceiveImage) {
                    RecvMessage.ReceiveImage msg = (RecvMessage.ReceiveImage) base;
                    handle(msg);
                } else {
                    throw new IllegalStateException();
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        };

        labelHashMap = new HashMap<>();
        imageViewHashMap = new HashMap<>();
        sliderHashMap = new HashMap<>();
    }

    @FXML
    private void onClick_NFA(MouseEvent mouseEvent) {
        onClick(GraphViewModel.GraphChoice.NFA, GraphViewModel.GraphPaneChoice.NFA, mouseEvent.getClickCount());
    }

    @FXML
    private void onClick_DFA(MouseEvent mouseEvent) {
        onClick(GraphViewModel.GraphChoice.DFA, GraphViewModel.GraphPaneChoice.DFA, mouseEvent.getClickCount());
    }

    @FXML
    private void onClick_MinDFA(MouseEvent mouseEvent) {
        onClick(GraphViewModel.GraphChoice.MinDFA, GraphViewModel.GraphPaneChoice.MinDFA, mouseEvent.getClickCount());
    }

    private void onClick(GraphViewModel.GraphChoice graphChoice, GraphViewModel.GraphPaneChoice graphPaneChoice, int clicks) {
        modelSetSelection(graphChoice);
        if (clicks == 2) {
            if (model.getGraphPaneChoice() == GraphViewModel.GraphPaneChoice.ALL) {
                model.setGraphPaneChoice(graphPaneChoice);
            } else {
                model.setGraphPaneChoice(GraphViewModel.GraphPaneChoice.ALL);
            }
            updateImageVisibility();
        }
    }

    private void modelSetSelection(GraphViewModel.GraphChoice graphChoice) {
        executor.execute(
                () -> {
                    model.setDotStringChoice(graphChoice);
                    updateTextArea();
                    Platform.runLater(
                            () -> {
                                updateLabels();
                            }
                    );
                }
        );
    }

    private void handle(RecvMessage.ReceiveImage msg) {
        ImageView target = imageViewHashMap.get(msg.graphChoice);
        if (msg.imagePath != null) {
            target.setImage(new Image(msg.imagePath.toUri().toString()));
        } else {
            target.setImage(null);
        }
    }

    private void handle(RecvMessage.ReceiveDotString msg) {
        textArea_display.setText(msg.dotString);
    }

    private void updateTextArea() {
        observable.onNext(new EmitMessage.RequestDotString(model.getDotStringChoice()));
    }

    private void updateImageVisibility() {
        for (GraphViewModel.GraphChoice graphChoice : GraphViewModel.GraphChoice.values()) {
            if (getVisibility(graphChoice)) {
                labelHashMap.get(graphChoice).setVisible(true);
                imageViewHashMap.get(graphChoice).setVisible(true);
                sliderHashMap.get(graphChoice).setVisible(true);
                observable.onNext(new EmitMessage.RequestImageSubscription(graphChoice, true));
            } else {
                labelHashMap.get(graphChoice).setVisible(false);
                imageViewHashMap.get(graphChoice).setVisible(false);
                sliderHashMap.get(graphChoice).setVisible(false);
                observable.onNext(new EmitMessage.RequestImageSubscription(graphChoice, false));
            }
        }
        gridPane_images.getColumnConstraints().get(0).setPercentWidth(getColumnWidth(GraphViewModel.GraphChoice.NFA));
        gridPane_images.getColumnConstraints().get(1).setPercentWidth(getColumnWidth(GraphViewModel.GraphChoice.DFA));
        gridPane_images.getColumnConstraints().get(2).setPercentWidth(getColumnWidth(GraphViewModel.GraphChoice.MinDFA));
    }

    private void updateLabels() {
        label_nfa.setUnderline(false);
        label_nfa.setStyle("-fx-font-weight: normal;");
        label_dfa.setUnderline(false);
        label_dfa.setStyle("-fx-font-weight: normal;");
        label_min_dfa.setUnderline(false);
        label_min_dfa.setStyle("-fx-font-weight: normal;");
        switch (model.getDotStringChoice()) {
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

    private boolean getVisibility(GraphViewModel.GraphChoice graphChoice) {
        switch (model.getGraphPaneChoice()) {
            case ALL:
                return true;
            case NFA:
                return graphChoice == GraphViewModel.GraphChoice.NFA;
            case DFA:
                return graphChoice == GraphViewModel.GraphChoice.DFA;
            case MinDFA:
                return graphChoice == GraphViewModel.GraphChoice.MinDFA;
        }
        throw new IllegalStateException();
    }

    private double getColumnWidth(GraphViewModel.GraphChoice graphChoice) {
        switch (model.getGraphPaneChoice()) {
            case ALL:
                return 33.3;
            case NFA:
                return graphChoice == GraphViewModel.GraphChoice.NFA ? 100 : 0;
            case DFA:
                return graphChoice == GraphViewModel.GraphChoice.DFA ? 100 : 0;
            case MinDFA:
                return graphChoice == GraphViewModel.GraphChoice.MinDFA ? 100 : 0;
        }
        throw new IllegalStateException();
    }

    public Observable<EmitMessage.Base> getObservable() {
        return observable;
    }

    public Observer<RecvMessage.Base> getObserver() {
        return observer;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        labelHashMap.put(GraphViewModel.GraphChoice.NFA, label_nfa);
        labelHashMap.put(GraphViewModel.GraphChoice.DFA, label_dfa);
        labelHashMap.put(GraphViewModel.GraphChoice.MinDFA, label_min_dfa);

        imageViewHashMap.put(GraphViewModel.GraphChoice.NFA, imageView_nfa);
        imageViewHashMap.put(GraphViewModel.GraphChoice.DFA, imageView_dfa);
        imageViewHashMap.put(GraphViewModel.GraphChoice.MinDFA, imageView_min_dfa);

        sliderHashMap.put(GraphViewModel.GraphChoice.NFA, slider_nfa);
        sliderHashMap.put(GraphViewModel.GraphChoice.DFA, slider_dfa);
        sliderHashMap.put(GraphViewModel.GraphChoice.MinDFA, slider_min_dfa);

        StringConverter<Double> labelConverter = new StringConverter<>() {
            @Override
            public String toString(Double object) {
                return String.format("%.2fx", Math.pow(2, object));
            }

            @Override
            public Double fromString(String string) {
                return Math.log(Double.parseDouble(string)) / Math.log(2);
            }
        };
        slider_nfa.setLabelFormatter(labelConverter);
        slider_dfa.setLabelFormatter(labelConverter);
        slider_min_dfa.setLabelFormatter(labelConverter);
        updateLabels();
    }
}
