package RegexFA.Graph;

import RegexFA.Alphabet;

import java.util.Set;

public class DFANode extends Node {
    private final Alphabet alphabet;
    private final DFANode[] edges;


    public DFANode(Graph<DFANode> graph, String id) {
        this(graph, id, null);
    }

    public DFANode(Graph<DFANode> graph, String id, Set<Node> nodeSet) {
        super(graph, id, nodeSet);
        this.alphabet = graph.getAlphabet();
        this.edges = new DFANode[alphabet.n];
    }

    public void setEdge(char ch, DFANode node) {
        edges[alphabet.invertMap.get(ch)] = node;
    }

    public DFANode getEdge(char ch) {
        return edges[alphabet.invertMap.get(ch)];
    }

    public DFANode getEdge(int i) {
        return edges[i];
    }

    @Override
    public String toString() {
        return "DFANode(" + toRepr() + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != DFANode.class) {
            return false;
        }
        DFANode other = (DFANode) obj;
        return this.getGraph().equals(other.getGraph()) && this.getId().equals(other.getId());
    }
}
