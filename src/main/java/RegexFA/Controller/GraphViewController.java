package RegexFA.Controller;

import RegexFA.Model.GraphViewModel;
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

public class GraphViewController extends Controller<GraphViewModel> {
    @FXML
    private Label label;
    @FXML
    private TextArea textArea;
    @FXML
    private GridPane gridPane;
    @FXML
    private Node graphZoomView1;
    @FXML
    private Node graphZoomView2;
    @FXML
    private Node graphZoomView3;
    @FXML
    private GraphZoomViewController graphZoomView1Controller;
    @FXML
    private GraphZoomViewController graphZoomView2Controller;
    @FXML
    private GraphZoomViewController graphZoomView3Controller;

    private final PublishSubject<Message.EmitBase> observable;
    private final Observer<Message.RecvBase> observer;

    private final PublishSubject<Message.ZoomViewRecv> zoomViewObservable;
    private final Observer<Message.ZoomViewEmit> zoomViewObserver;

    private final HashMap<GraphViewModel.GraphChoice, Node> graphZoomViewHashMap;
    private final HashMap<GraphViewModel.GraphChoice, Integer> gridPaneColumnNumHashMap;

    public GraphViewController() {
        super(new GraphViewModel());
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
            public void onNext(Message.@NonNull ZoomViewEmit zoomViewEmit) {
                handle(zoomViewEmit);
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        };
        graphZoomViewHashMap = new HashMap<>();
        gridPaneColumnNumHashMap = new HashMap<>();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        graphZoomView1Controller.getObservable().map(msg -> new Message.ZoomViewEmit(GraphViewModel.GraphChoice.Graph1, msg)).subscribe(zoomViewObserver);
        zoomViewObservable.filter(msg -> msg.graphChoice == GraphViewModel.GraphChoice.Graph1).map(msg -> msg.msg).subscribe(graphZoomView1Controller.getObserver());
        graphZoomView2Controller.getObservable().map(msg -> new Message.ZoomViewEmit(GraphViewModel.GraphChoice.Graph2, msg)).subscribe(zoomViewObserver);
        zoomViewObservable.filter(msg -> msg.graphChoice == GraphViewModel.GraphChoice.Graph2).map(msg -> msg.msg).subscribe(graphZoomView2Controller.getObserver());
        graphZoomView3Controller.getObservable().map(msg -> new Message.ZoomViewEmit(GraphViewModel.GraphChoice.Graph3, msg)).subscribe(zoomViewObserver);
        zoomViewObservable.filter(msg -> msg.graphChoice == GraphViewModel.GraphChoice.Graph3).map(msg -> msg.msg).subscribe(graphZoomView3Controller.getObserver());

        gridPaneColumnNumHashMap.put(GraphViewModel.GraphChoice.Graph1, 0);
        gridPaneColumnNumHashMap.put(GraphViewModel.GraphChoice.Graph2, 1);
        gridPaneColumnNumHashMap.put(GraphViewModel.GraphChoice.Graph3, 2);

        graphZoomViewHashMap.put(GraphViewModel.GraphChoice.Graph1, graphZoomView1);
        graphZoomViewHashMap.put(GraphViewModel.GraphChoice.Graph2, graphZoomView2);
        graphZoomViewHashMap.put(GraphViewModel.GraphChoice.Graph3, graphZoomView3);

        updateLabels();
    }

    public void setLabel(String label) {
        this.label.setText(label);
    }

    public void setGraphLabel(GraphViewModel.GraphChoice graphChoice, String label) {
        zoomViewObservable.onNext(new Message.ZoomViewRecv(graphChoice, new GraphZoomViewController.Message.RecvLabel(label)));
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
        zoomViewObservable.onNext(new Message.ZoomViewRecv(msg.graphChoice, new GraphZoomViewController.Message.RecvImage(msg.imagePath)));
    }

    private void handle(Message.ReceiveText msg) {
        textArea.setText(msg.text);
    }

    private void handle(Message.ZoomViewEmit msg) {
        if (msg.msg instanceof GraphZoomViewController.Message.EmitClickImage) {
            handle(msg.graphChoice, (GraphZoomViewController.Message.EmitClickImage) msg.msg);
        } else if (msg.msg instanceof GraphZoomViewController.Message.EmitClickLabel) {
            handle(msg.graphChoice, (GraphZoomViewController.Message.EmitClickLabel) msg.msg);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private void handle(GraphViewModel.GraphChoice graphChoice, GraphZoomViewController.Message.EmitClickImage msg) {
        clickGraph(graphChoice, msg.clickCount);
    }

    private void handle(GraphViewModel.GraphChoice graphChoice, GraphZoomViewController.Message.EmitClickLabel msg) {
        clickGraph(graphChoice, msg.clickCount);
    }

    private void clickGraph(GraphViewModel.GraphChoice graphChoice, int clicks) {
        modelSetSelection(graphChoice);
        if (clicks == 2) {
            if (model.getGraphPaneChoice() == GraphViewModel.GraphPaneChoice.ALL) {
                model.setGraphPaneChoice(GraphViewModel.GraphPaneChoice.fromGraphChoice(graphChoice));
            } else {
                model.setGraphPaneChoice(GraphViewModel.GraphPaneChoice.ALL);
            }
            updateImageVisibility();
        }
    }

    private void modelSetSelection(GraphViewModel.GraphChoice graphChoice) {
        model.setGraphFocus(graphChoice);
        updateTextArea();
        updateLabels();
    }


    private void updateTextArea() {
        observable.onNext(new Message.EmitGraphFocus(model.getGraphFocus()));
    }

    private void updateImageVisibility() {
        for (GraphViewModel.GraphChoice graphChoice : GraphViewModel.GraphChoice.values()) {
            if (getVisibility(graphChoice)) {
                graphZoomViewHashMap.get(graphChoice).setVisible(true);
                observable.onNext(new Message.EmitImageSubscription(graphChoice, true));
            } else {
                graphZoomViewHashMap.get(graphChoice).setVisible(false);
                observable.onNext(new Message.EmitImageSubscription(graphChoice, false));
            }
            gridPane.getColumnConstraints().get(gridPaneColumnNumHashMap.get(graphChoice)).setPercentWidth(getColumnWidth(graphChoice));
        }
    }

    private void updateLabels() {
        for (GraphViewModel.GraphChoice graphChoice : GraphViewModel.GraphChoice.values()) {
            if (graphChoice == model.getGraphFocus()) {
                zoomViewObservable.onNext(new Message.ZoomViewRecv(graphChoice, new GraphZoomViewController.Message.RecvLabelFormat("-fx-font-weight: bold;", true)));
            } else {
                zoomViewObservable.onNext(new Message.ZoomViewRecv(graphChoice, new GraphZoomViewController.Message.RecvLabelFormat("-fx-font-weight: normal;", false)));
            }
        }
    }

    private boolean getVisibility(GraphViewModel.GraphChoice graphChoice) {
        switch (model.getGraphPaneChoice()) {
            case ALL:
                return true;
            case Pane1:
                return graphChoice == GraphViewModel.GraphChoice.Graph1;
            case Pane2:
                return graphChoice == GraphViewModel.GraphChoice.Graph2;
            case Pane3:
                return graphChoice == GraphViewModel.GraphChoice.Graph3;
        }
        throw new IllegalStateException();
    }

    private double getColumnWidth(GraphViewModel.GraphChoice graphChoice) {
        switch (model.getGraphPaneChoice()) {
            case ALL:
                return 33.3;
            case Pane1:
                return graphChoice == GraphViewModel.GraphChoice.Graph1 ? 100 : 0;
            case Pane2:
                return graphChoice == GraphViewModel.GraphChoice.Graph2 ? 100 : 0;
            case Pane3:
                return graphChoice == GraphViewModel.GraphChoice.Graph3 ? 100 : 0;
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
            public final GraphViewModel.GraphChoice graphChoice;

            public EmitGraphFocus(GraphViewModel.GraphChoice graphChoice) {
                this.graphChoice = graphChoice;
            }
        }

        public static final class EmitImageSubscription extends EmitBase {
            public final GraphViewModel.GraphChoice graphChoice;
            public final boolean subscribe;

            public EmitImageSubscription(GraphViewModel.GraphChoice graphChoice, boolean subscribe) {
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
            public final GraphViewModel.GraphChoice graphChoice;
            public final Path imagePath;

            public ReceiveImage(GraphViewModel.GraphChoice graphChoice, Path imagePath) {
                this.graphChoice = graphChoice;
                this.imagePath = imagePath;
            }
        }

        private static final class ZoomViewEmit {
            public final GraphViewModel.GraphChoice graphChoice;
            public final GraphZoomViewController.Message.EmitBase msg;

            public ZoomViewEmit(GraphViewModel.GraphChoice graphChoice, GraphZoomViewController.Message.EmitBase msg) {
                this.graphChoice = graphChoice;
                this.msg = msg;
            }
        }

        private static final class ZoomViewRecv {
            public final GraphViewModel.GraphChoice graphChoice;
            public final GraphZoomViewController.Message.RecvBase msg;

            public ZoomViewRecv(GraphViewModel.GraphChoice graphChoice, GraphZoomViewController.Message.RecvBase msg) {
                this.graphChoice = graphChoice;
                this.msg = msg;
            }
        }
    }

}
