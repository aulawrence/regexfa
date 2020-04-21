package RegexFA.Controller;

import RegexFA.Model.GraphPanelModel;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.ResourceBundle;

public class GraphPanelController extends Controller<GraphPanelModel> {
    @FXML
    private Label label;
    @FXML
    private TextArea textArea;
    @FXML
    private GridPane gridPane;
    @FXML
    private Node graphView1;
    @FXML
    private Node graphView2;
    @FXML
    private Node graphView3;
    @FXML
    private GraphController graphView1Controller;
    @FXML
    private GraphController graphView2Controller;
    @FXML
    private GraphController graphView3Controller;

    private final PublishSubject<Message.EmitBase> observable;
    private final Observer<Message.RecvBase> observer;

    private final PublishSubject<Message.GraphControllerRecv> zoomViewObservable;
    private final Observer<Message.GraphControllerEmit> zoomViewObserver;

    private final HashMap<GraphPanelModel.GraphChoice, Node> graphViewHashMap;
    private final HashMap<GraphPanelModel.GraphChoice, Integer> gridPaneColumnNumHashMap;

    public GraphPanelController() {
        super(new GraphPanelModel());
        observable = PublishSubject.create();
        observer = new Observer<>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(Message.RecvBase recvBase) {
                handle(recvBase);
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        };
        zoomViewObservable = PublishSubject.create();
        zoomViewObserver = new Observer<>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(Message.GraphControllerEmit graphControllerEmit) {
                handle(graphControllerEmit);
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        };
        graphViewHashMap = new HashMap<>();
        gridPaneColumnNumHashMap = new HashMap<>();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        graphView1Controller.getObservable().map(msg -> new Message.GraphControllerEmit(GraphPanelModel.GraphChoice.Graph1, msg)).subscribe(zoomViewObserver);
        zoomViewObservable.filter(msg -> msg.graphChoice == GraphPanelModel.GraphChoice.Graph1).map(msg -> msg.msg).subscribe(graphView1Controller.getObserver());
        graphView2Controller.getObservable().map(msg -> new Message.GraphControllerEmit(GraphPanelModel.GraphChoice.Graph2, msg)).subscribe(zoomViewObserver);
        zoomViewObservable.filter(msg -> msg.graphChoice == GraphPanelModel.GraphChoice.Graph2).map(msg -> msg.msg).subscribe(graphView2Controller.getObserver());
        graphView3Controller.getObservable().map(msg -> new Message.GraphControllerEmit(GraphPanelModel.GraphChoice.Graph3, msg)).subscribe(zoomViewObserver);
        zoomViewObservable.filter(msg -> msg.graphChoice == GraphPanelModel.GraphChoice.Graph3).map(msg -> msg.msg).subscribe(graphView3Controller.getObserver());

        gridPaneColumnNumHashMap.put(GraphPanelModel.GraphChoice.Graph1, 0);
        gridPaneColumnNumHashMap.put(GraphPanelModel.GraphChoice.Graph2, 1);
        gridPaneColumnNumHashMap.put(GraphPanelModel.GraphChoice.Graph3, 2);

        graphViewHashMap.put(GraphPanelModel.GraphChoice.Graph1, graphView1);
        graphViewHashMap.put(GraphPanelModel.GraphChoice.Graph2, graphView2);
        graphViewHashMap.put(GraphPanelModel.GraphChoice.Graph3, graphView3);

        updateLabels();
    }

    public void setLabel(String label) {
        this.label.setText(label);
    }

    public void setGraphLabel(GraphPanelModel.GraphChoice graphChoice, String label) {
        zoomViewObservable.onNext(new Message.GraphControllerRecv(graphChoice, new GraphController.Message.RecvLabel(label)));
    }

    private void handle(Message.RecvBase recvBase) {
        if (recvBase instanceof Message.ReceiveText) {
            handle((Message.ReceiveText) recvBase);
        } else if (recvBase instanceof Message.ReceiveImage) {
            handle((Message.ReceiveImage) recvBase);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private void handle(Message.ReceiveImage msg) {
        zoomViewObservable.onNext(new Message.GraphControllerRecv(msg.graphChoice, new GraphController.Message.RecvImage(msg.imagePath)));
    }

    private void handle(Message.ReceiveText msg) {
        textArea.setText(msg.text);
    }

    private void handle(Message.GraphControllerEmit msg) {
        if (msg.msg instanceof GraphController.Message.EmitClickImage) {
            handle(msg.graphChoice, (GraphController.Message.EmitClickImage) msg.msg);
        } else if (msg.msg instanceof GraphController.Message.EmitClickLabel) {
            handle(msg.graphChoice, (GraphController.Message.EmitClickLabel) msg.msg);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private void handle(GraphPanelModel.GraphChoice graphChoice, GraphController.Message.EmitClickImage msg) {
        clickGraph(graphChoice, msg.clickCount);
    }

    private void handle(GraphPanelModel.GraphChoice graphChoice, GraphController.Message.EmitClickLabel msg) {
        clickGraph(graphChoice, msg.clickCount);
    }

    private void clickGraph(GraphPanelModel.GraphChoice graphChoice, int clicks) {
        modelSetSelection(graphChoice);
        if (clicks == 2) {
            if (model.getGraphPaneChoice() == GraphPanelModel.GraphPaneChoice.ALL) {
                model.setGraphPaneChoice(GraphPanelModel.GraphPaneChoice.fromGraphChoice(graphChoice));
            } else {
                model.setGraphPaneChoice(GraphPanelModel.GraphPaneChoice.ALL);
            }
            updateImageVisibility();
        }
    }

    private void modelSetSelection(GraphPanelModel.GraphChoice graphChoice) {
        model.setGraphFocus(graphChoice);
        updateTextArea();
        updateLabels();
    }


    private void updateTextArea() {
        observable.onNext(new Message.EmitGraphFocus(model.getGraphFocus()));
    }

    private void updateImageVisibility() {
        for (GraphPanelModel.GraphChoice graphChoice : GraphPanelModel.GraphChoice.values()) {
            if (getVisibility(graphChoice)) {
                graphViewHashMap.get(graphChoice).setVisible(true);
                observable.onNext(new Message.EmitImageSubscription(graphChoice, true));
            } else {
                graphViewHashMap.get(graphChoice).setVisible(false);
                observable.onNext(new Message.EmitImageSubscription(graphChoice, false));
            }
            gridPane.getColumnConstraints().get(gridPaneColumnNumHashMap.get(graphChoice)).setPercentWidth(getColumnWidth(graphChoice));
        }
    }

    private void updateLabels() {
        for (GraphPanelModel.GraphChoice graphChoice : GraphPanelModel.GraphChoice.values()) {
            if (graphChoice == model.getGraphFocus()) {
                zoomViewObservable.onNext(new Message.GraphControllerRecv(graphChoice, new GraphController.Message.RecvLabelFormat("-fx-font-weight: bold;", true)));
            } else {
                zoomViewObservable.onNext(new Message.GraphControllerRecv(graphChoice, new GraphController.Message.RecvLabelFormat("-fx-font-weight: normal;", false)));
            }
        }
    }

    private boolean getVisibility(GraphPanelModel.GraphChoice graphChoice) {
        switch (model.getGraphPaneChoice()) {
            case ALL:
                return true;
            case Pane1:
                return graphChoice == GraphPanelModel.GraphChoice.Graph1;
            case Pane2:
                return graphChoice == GraphPanelModel.GraphChoice.Graph2;
            case Pane3:
                return graphChoice == GraphPanelModel.GraphChoice.Graph3;
        }
        throw new IllegalStateException();
    }

    private double getColumnWidth(GraphPanelModel.GraphChoice graphChoice) {
        switch (model.getGraphPaneChoice()) {
            case ALL:
                return 33.3;
            case Pane1:
                return graphChoice == GraphPanelModel.GraphChoice.Graph1 ? 100 : 0;
            case Pane2:
                return graphChoice == GraphPanelModel.GraphChoice.Graph2 ? 100 : 0;
            case Pane3:
                return graphChoice == GraphPanelModel.GraphChoice.Graph3 ? 100 : 0;
        }
        throw new IllegalStateException();
    }

    public Observable<Message.EmitBase> getObservable() {
        return observable;
    }

    public Observer<Message.RecvBase> getObserver() {
        return observer;
    }

    public static final class Message {
        public static abstract class EmitBase {
        }

        public static final class EmitGraphFocus extends EmitBase {
            public final GraphPanelModel.GraphChoice graphChoice;

            public EmitGraphFocus(GraphPanelModel.GraphChoice graphChoice) {
                this.graphChoice = graphChoice;
            }
        }

        public static final class EmitImageSubscription extends EmitBase {
            public final GraphPanelModel.GraphChoice graphChoice;
            public final boolean subscribe;

            public EmitImageSubscription(GraphPanelModel.GraphChoice graphChoice, boolean subscribe) {
                this.graphChoice = graphChoice;
                this.subscribe = subscribe;
            }
        }

        public static abstract class RecvBase {
        }

        public static final class ReceiveText extends RecvBase {
            public final String text;

            public ReceiveText(String text) {
                this.text = text;
            }
        }

        public static final class ReceiveImage extends RecvBase {
            public final GraphPanelModel.GraphChoice graphChoice;
            public final Path imagePath;

            public ReceiveImage(GraphPanelModel.GraphChoice graphChoice, Path imagePath) {
                this.graphChoice = graphChoice;
                this.imagePath = imagePath;
            }
        }

        private static final class GraphControllerEmit {
            public final GraphPanelModel.GraphChoice graphChoice;
            public final GraphController.Message.EmitBase msg;

            public GraphControllerEmit(GraphPanelModel.GraphChoice graphChoice, GraphController.Message.EmitBase msg) {
                this.graphChoice = graphChoice;
                this.msg = msg;
            }
        }

        private static final class GraphControllerRecv {
            public final GraphPanelModel.GraphChoice graphChoice;
            public final GraphController.Message.RecvBase msg;

            public GraphControllerRecv(GraphPanelModel.GraphChoice graphChoice, GraphController.Message.RecvBase msg) {
                this.graphChoice = graphChoice;
                this.msg = msg;
            }
        }
    }

}
