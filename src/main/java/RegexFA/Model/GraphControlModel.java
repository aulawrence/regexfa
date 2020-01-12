package RegexFA.Model;

import RegexFA.Alphabet;
import RegexFA.Graph.Edge;
import RegexFA.Graph.Graph;
import RegexFA.Graph.Node;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class GraphControlModel extends Model {
    private final Graph graph;

    private final ObservableList<Node> nodeObservableList;

    private final ObservableList<Edge> edgeObservableList;

    public GraphControlModel(Graph graph) {
        this.graph = graph;
        nodeObservableList = FXCollections.observableArrayList();
        edgeObservableList = FXCollections.observableArrayList();
    }

    public GraphControlModel() {
        this(new Graph(Alphabet.Binary));
    }

    public Graph getGraph() {
        return graph;
    }

    public ObservableList<Node> getNodeObservableList() {
        return nodeObservableList;
    }

    public ObservableList<Edge> getEdgeObservableList() {
        return edgeObservableList;
    }

    public List<Character> getAlphabetList() {
        return graph.getAlphabet().alphabetList;
    }

    public String getDotNotation(){
        return graph.toDotString();
    }

    public void addNode() {
        Node node = graph.addNode();
        nodeObservableList.add(node);
    }

    public void addEdge(Node fromNode, Node toNode, Character ch) {
        Edge edge = graph.addEdge(fromNode, toNode, ch);
        if (edge != null) {
            edgeObservableList.add(edge);
        }
    }

    public void removeNode(Node node) {
        List<Edge> edges = graph.removeNode(node);
        nodeObservableList.remove(node);
        edgeObservableList.removeAll(edges);
    }

    public void removeEdge(Edge edge) {
        graph.removeEdge(edge);
        edgeObservableList.remove(edge);
    }

}
