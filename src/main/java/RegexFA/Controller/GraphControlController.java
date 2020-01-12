package RegexFA.Controller;

import RegexFA.Graph.Edge;
import RegexFA.Graph.Node;
import RegexFA.Model.GraphControlModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class GraphControlController extends Controller<GraphControlModel> {
    @FXML
    public Button button_addNode;
    @FXML
    public Button button_addEdge;
    @FXML
    public ListView<Node> list_fromNode;
    @FXML
    public ListView<Node> list_toNode;
    @FXML
    public ListView<Character> list_char;
    @FXML
    public ListView<Edge> list_edge;
    @FXML
    public Button button_removeNode;
    @FXML
    public Button button_removeEdge;
    @FXML
    public TextArea text_dotNotation;

    private final GraphViewController graphViewController;


    private Node fromNode = null;
    private Node toNode = null;
    private Character ch = null;
    private Edge edge = null;

    public GraphControlController() {
        this(new GraphControlModel());
    }

    public GraphControlController(GraphControlModel model) {
        super(model);
        this.graphViewController = new GraphViewController(model.getGraph());
    }

    @FXML
    public void onMouseClick_button_addNode(MouseEvent mouseEvent) {
        model.addNode();
        graphViewController.updateImage();
        text_dotNotation.setText(model.getDotNotation());
    }

    @FXML
    public void onMouseClick_button_removeNode(MouseEvent mouseEvent) {
        if (fromNode != null) {
            model.removeNode(fromNode);
            graphViewController.updateImage();
            text_dotNotation.setText(model.getDotNotation());
        }
    }

    @FXML
    public void onMouseClick_button_addEdge(MouseEvent mouseEvent) {
        if (fromNode != null && toNode != null && ch != null) {
            model.addEdge(fromNode, toNode, ch);
            graphViewController.updateImage();
            text_dotNotation.setText(model.getDotNotation());
        }
    }

    @FXML
    public void onMouseClick_button_removeEdge(MouseEvent mouseEvent) {
        if (edge != null) {
            model.removeEdge(edge);
            graphViewController.updateImage();
            text_dotNotation.setText(model.getDotNotation());
        }
    }

    private void updateNodeButtons() {
        if (fromNode != null) {
            if (toNode != null && ch != null) {
                button_addEdge.setDisable(false);
                button_addEdge.setText(String.format("Add Edge %s -> %s [%s]", fromNode.getId(), toNode.getId(), ch));
            } else {
                button_addEdge.setDisable(true);
                button_addEdge.setText("Add Edge");
            }
            button_removeNode.setDisable(false);
            button_removeNode.setText(String.format("Remove Node %s", fromNode.getId()));
        } else {
            button_removeNode.setDisable(true);
            button_removeNode.setText("Remove Node");
        }
    }

    private void updateEdgeButtons() {
        if (edge != null) {
            button_removeEdge.setDisable(false);
            button_removeEdge.setText(String.format("Remove Edge  %s -> %s [%s]", edge.fromNode.getId(), edge.toNode.getId(), edge.label));
        } else {
            button_removeEdge.setDisable(true);
            button_removeEdge.setText("Remove Edge");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        list_fromNode.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        list_toNode.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        list_char.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        list_edge.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        list_char.getItems().setAll(model.getAlphabetList());
        list_fromNode.setItems(model.getNodeObservableList());
        list_toNode.setItems(model.getNodeObservableList());
        list_edge.setItems(model.getEdgeObservableList());

        list_fromNode.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            this.fromNode = newVal;
            updateNodeButtons();
        });
        list_toNode.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            this.toNode = newVal;
            updateNodeButtons();
        });
        list_char.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            this.ch = newVal;
            updateNodeButtons();
        });
        list_edge.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            this.edge = newVal;
            updateEdgeButtons();
        });


        Callback<ListView<Node>, ListCell<Node>> nodeCellFactory = param -> new ListCell<>() {
            @Override
            protected void updateItem(Node item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setText("");
                } else {
                    setText(item.getId());
                }
            }
        };

        Callback<ListView<Edge>, ListCell<Edge>> edgeCellFactory = param -> new ListCell<>() {
            @Override
            protected void updateItem(Edge item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setText("");
                } else {
                    setText(String.format("%s -> %s [ %s ]", item.fromNode.getId(), item.toNode.getId(), item.label));
                }
            }
        };

        list_fromNode.setCellFactory(nodeCellFactory);
        list_toNode.setCellFactory(nodeCellFactory);
        list_edge.setCellFactory(edgeCellFactory);


        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/graph-view.fxml"));
            loader.setController(graphViewController);
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Graph View");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
