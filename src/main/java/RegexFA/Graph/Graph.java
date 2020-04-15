package RegexFA.Graph;

import RegexFA.Alphabet;
import guru.nidi.graphviz.engine.Engine;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Graph {

    private final Alphabet alphabet;
    protected final ArrayList<Node> nodeList;
    protected final ArrayList<Edge> edgeList;
    private int id;

    protected static String getIDString(int id) {
        assert id > 0;
        StringBuilder sb = new StringBuilder();
        int b = 1;
        int x = 0;
        while (x <= id) {
            x += b;
            b *= 26;
        }
        b /= 26;
        x = (id - (x - b));
        b /= 26;
        while (b >= 1) {
            sb.append((char) ('A' + x / b));
            x %= b;
            b /= 26;
        }
        return sb.toString();
    }

    protected int getNextID() {
        return ++this.id;
    }

    public Graph(Alphabet alphabet) {
        this.alphabet = alphabet;
        this.nodeList = new ArrayList<>();
        this.edgeList = new ArrayList<>();
        this.id = 0;
    }

    public Node addNode() {
        String idString = getIDString(getNextID());
        Node node = new Node(this, idString);
        this.nodeList.add(node);
        return node;
    }

    public List<Edge> removeNode(Node node) {
        this.nodeList.remove(node);
        List<Edge> edges = this.edgeList.stream().filter(x -> x.fromNode == node || x.toNode == node).collect(Collectors.toList());
        this.edgeList.removeAll(edges);
        return edges;
    }

    public Edge addEdge(Node fromNode, Node toNode, char ch) {
        if (fromNode.getGraph() != this || toNode.getGraph() != this) throw new AssertionError();
        Edge edge = new Edge(fromNode, toNode, ch);
        if (!this.edgeList.contains(edge)) {
            this.edgeList.add(edge);
            return edge;
        }
        return null;
    }

    public void removeEdge(Edge edge) {
        this.edgeList.remove(edge);
    }

    public String toDotString() {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph {\n");
        sb.append("\n");
        for (Node node : nodeList) {
            sb.append(String.format("  %s [width=1, height=1];\n", node.getId()));
        }
        sb.append("\n");
        for (Edge edge : edgeList) {
            sb.append(String.format("  %s->%s[label=\"%s\"];\n", edge.fromNode.getId(), edge.toNode.getId(), edge.label));
        }
        sb.append("\n");
        sb.append("}\n");
        return sb.toString();
    }

    public static InputStream getImageStream(String dotString) {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            MutableGraph g = new Parser().read(dotString);
            Graphviz.fromGraph(g).engine(Engine.DOT).render(Format.PNG).toOutputStream(os);
            return new ByteArrayInputStream(os.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Alphabet getAlphabet() {
        return alphabet;
    }

    public List<Node> getNodeList() {
        return nodeList;
    }

    public List<Edge> getEdgeList() {
        return edgeList;
    }

}




