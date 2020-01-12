package Test4.Graph;

import Test4.Alphabet;

import java.util.Set;

public class SimpleNode extends Node {
    private final Alphabet alphabet;
    private final Node[] edges;


    public SimpleNode(Graph graph, String id) {
        this(graph, id, null);
    }

    public SimpleNode(Graph graph, String id, Set<Node> nodeSet) {
        super(graph, id, nodeSet);
        this.alphabet = graph.getAlphabet();
        this.edges = new SimpleNode[alphabet.n];
    }

    public void setEdge(char ch, Node node){
        edges[alphabet.invertMap.get(ch)] = node;
    }

    public Node[] getEdges() {
        return edges;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != SimpleNode.class) {
            return false;
        }
        SimpleNode other = (SimpleNode) obj;
        return this.getGraph().equals(other.getGraph()) && this.getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
