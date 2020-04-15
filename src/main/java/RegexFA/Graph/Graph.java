package RegexFA.Graph;

import RegexFA.Alphabet;
import guru.nidi.graphviz.engine.Engine;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Graph<N extends Node> {

    protected final Alphabet alphabet;
    protected final ArrayList<N> nodeList;
    protected final ArrayList<Edge<N>> edgeList;
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

    public abstract N addNode();

    public List<Edge<N>> removeNode(N node) {
        this.nodeList.remove(node);
        List<Edge<N>> edges = this.edgeList.stream().filter(x -> x.fromNode == node || x.toNode == node).collect(Collectors.toList());
        this.edgeList.removeAll(edges);
        return edges;
    }

    public Edge<N> addEdge(N fromNode, N toNode, char ch) {
        if (fromNode.getGraph() != this || toNode.getGraph() != this) throw new AssertionError();
        Edge<N> edge = new Edge<>(fromNode, toNode, ch);
        if (!this.edgeList.contains(edge)) {
            this.edgeList.add(edge);
            return edge;
        }
        return null;
    }

    public void removeEdge(Edge<N> edge) {
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
        for (Edge<N> edge : edgeList) {
            sb.append(String.format("  %s->%s[label=\"%s\"];\n", edge.fromNode.getId(), edge.toNode.getId(), edge.label));
        }
        sb.append("\n");
        sb.append("}\n");
        return sb.toString();
    }

    public static BufferedImage getImage(String dotString) {
        try {
            MutableGraph g = new Parser().read(dotString);
            return Graphviz.fromGraph(g).engine(Engine.DOT).render(Format.PNG).toImage();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Alphabet getAlphabet() {
        return alphabet;
    }

    public List<N> getNodeList() {
        return nodeList;
    }

    public List<Edge<N>> getEdgeList() {
        return edgeList;
    }

}




