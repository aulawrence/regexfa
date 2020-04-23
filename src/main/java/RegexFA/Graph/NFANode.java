package RegexFA.Graph;

public class NFANode extends Node<NFAGraph> {
    public NFANode(NFAGraph graph, String id) {
        super(graph, id);
    }

    @Override
    public boolean isAccept() {
        return graph.getTerminalNode() == this;
    }

    @Override
    public String toRepr() {
        return getId();
    }

    @Override
    public String toString() {
        return "NFANode(" + toRepr() + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != NFANode.class) {
            return false;
        }
        NFANode other = (NFANode) obj;
        return this.getGraph().equals(other.getGraph()) && this.getId().equals(other.getId());
    }
}
