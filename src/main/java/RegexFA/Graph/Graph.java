package RegexFA.Graph;

import RegexFA.Alphabet;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class Graph<N extends Node<?>> {

    protected final Alphabet alphabet;
    protected final Set<N> nodes;
    protected final Set<Edge<N>> edges;
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
        this.nodes = new LinkedHashSet<>();
        this.edges = new HashSet<>();
        this.id = 0;
    }

    public abstract N addNode();

    public Set<Edge<N>> removeNode(N node) {
        this.nodes.remove(node);
        Set<Edge<N>> edges = this.edges.stream().filter(x -> x.fromNode == node || x.toNode == node).collect(Collectors.toSet());
        this.edges.removeAll(edges);
        return edges;
    }

    public Edge<N> addEdge(N fromNode, N toNode, char ch) {
        if (fromNode.getGraph() != this || toNode.getGraph() != this) throw new AssertionError();
        Edge<N> edge = new Edge<>(fromNode, toNode, ch);
        if (!this.edges.contains(edge)) {
            this.edges.add(edge);
            return edge;
        }
        return null;
    }

    public void removeEdge(Edge<N> edge) {
        this.edges.remove(edge);
    }

    public String toDotString() {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph {\n");
        sb.append("\n");
        for (N node : nodes) {
            sb.append(String.format("  %s [width=1, height=1];\n", node.getId()));
        }
        sb.append("\n");
        for (Edge<N> edge : edges) {
            sb.append(String.format("  %s->%s[label=\"%s\"];\n", edge.fromNode.getId(), edge.toNode.getId(), edge.label));
        }
        sb.append("\n");
        sb.append("}\n");
        return sb.toString();
    }

    public Alphabet getAlphabet() {
        return alphabet;
    }

    public Set<N> getNodes() {
        return Collections.unmodifiableSet(nodes);
    }

    public Set<Edge<N>> getEdges() {
        return Collections.unmodifiableSet(edges);
    }

    @Override
    public final boolean equals(Object obj) {
        return super.equals(obj);
    }
}




