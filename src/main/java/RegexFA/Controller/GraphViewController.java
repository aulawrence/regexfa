package RegexFA.Controller;

import RegexFA.Model.GraphViewModel;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.ResourceBundle;

public class GraphViewController extends Controller<GraphViewModel> {
    @FXML
    private TextArea textArea;
    @FXML
    private GridPane gridPane;
    @FXML
    private Node graphZoomViewNFA;
    @FXML
    private Node graphZoomViewDFA;
    @FXML
    private Node graphZoomViewMinDFA;
    @FXML
    private GraphZoomViewController graphZoomViewNFAController;
    @FXML
    private GraphZoomViewController graphZoomViewDFAController;
    @FXML
    private GraphZoomViewController graphZoomViewMinDFAController;

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
        graphZoomViewNFAController.getObservable().map(msg -> new Message.ZoomViewEmit(GraphViewModel.GraphChoice.NFA, msg)).subscribe(zoomViewObserver);
        zoomViewObservable.filter(msg -> msg.graphChoice == GraphViewModel.GraphChoice.NFA).map(msg -> msg.msg).subscribe(graphZoomViewNFAController.getObserver());
        graphZoomViewDFAController.getObservable().map(msg -> new Message.ZoomViewEmit(GraphViewModel.GraphChoice.DFA, msg)).subscribe(zoomViewObserver);
        zoomViewObservable.filter(msg -> msg.graphChoice == GraphViewModel.GraphChoice.DFA).map(msg -> msg.msg).subscribe(graphZoomViewDFAController.getObserver());
        graphZoomViewMinDFAController.getObservable().map(msg -> new Message.ZoomViewEmit(GraphViewModel.GraphChoice.MinDFA, msg)).subscribe(zoomViewObserver);
        zoomViewObservable.filter(msg -> msg.graphChoice == GraphViewModel.GraphChoice.MinDFA).map(msg -> msg.msg).subscribe(graphZoomViewMinDFAController.getObserver());

        zoomViewObservable.onNext(new Message.ZoomViewRecv(GraphViewModel.GraphChoice.NFA, new GraphZoomViewController.Message.RecvLabel("NFA")));
        zoomViewObservable.onNext(new Message.ZoomViewRecv(GraphViewModel.GraphChoice.DFA, new GraphZoomViewController.Message.RecvLabel("DFA")));
        zoomViewObservable.onNext(new Message.ZoomViewRecv(GraphViewModel.GraphChoice.MinDFA, new GraphZoomViewController.Message.RecvLabel("MinDFA")));

        gridPaneColumnNumHashMap.put(GraphViewModel.GraphChoice.NFA, 0);
        gridPaneColumnNumHashMap.put(GraphViewModel.GraphChoice.DFA, 1);
        gridPaneColumnNumHashMap.put(GraphViewModel.GraphChoice.MinDFA, 2);

        graphZoomViewHashMap.put(GraphViewModel.GraphChoice.NFA, graphZoomViewNFA);
        graphZoomViewHashMap.put(GraphViewModel.GraphChoice.DFA, graphZoomViewDFA);
        graphZoomViewHashMap.put(GraphViewModel.GraphChoice.MinDFA, graphZoomViewMinDFA);

        updateLabels();
    }

    private void handle(Message.RecvBase recvBase) {
        if (recvBase instanceof Message.ReceiveDotString) {
            handle((Message.ReceiveDotString) recvBase);
        } else if (recvBase instanceof Message.ReceiveImage) {
            handle((Message.ReceiveImage) recvBase);
        } else {
            throw new IllegalStateException();
        }
    }

    private void handle(Message.ReceiveImage msg) {
        zoomViewObservable.onNext(new Message.ZoomViewRecv(msg.graphChoice, new GraphZoomViewController.Message.RecvImage(msg.imagePath)));
    }

    private void handle(Message.ReceiveDotString msg) {
        textArea.setText(msg.dotString);
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
        model.setDotStringChoice(graphChoice);
        updateTextArea();
        updateLabels();
    }


    private void updateTextArea() {
        observable.onNext(new Message.EmitDotStringRequest(model.getDotStringChoice()));
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
            if (graphChoice == model.getDotStringChoice()) {
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

    public Observable<Message.EmitBase> getObservable() {
        return observable;
    }

    public Observer<Message.RecvBase> getObserver() {
        return observer;
    }

    public static final class Message {
        public static abstract class EmitBase {
        }

        public static final class EmitDotStringRequest extends EmitBase {
            public final GraphViewModel.GraphChoice graphChoice;

            public EmitDotStringRequest(GraphViewModel.GraphChoice graphChoice) {
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

        public static final class ReceiveDotString extends RecvBase {
            public final String dotString;

            public ReceiveDotString(String dotString) {
                this.dotString = dotString;
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
