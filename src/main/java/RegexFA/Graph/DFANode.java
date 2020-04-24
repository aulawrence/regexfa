package RegexFA.Graph;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class DFANode extends Node<DFAGraph> {
    private Set<Node<?>> nodeSet;
    private boolean accept;


    public DFANode(DFAGraph graph, String id) {
        this(graph, id, null);
    }

    public DFANode(DFAGraph graph, String id, Set<Node<?>> nodeSet) {
        super(graph, id);
        this.nodeSet = nodeSet == null ? null : Collections.unmodifiableSet(nodeSet);
        this.accept = false;
    }

    public Set<Node<?>> getNodeSet() {
        return nodeSet;
    }

    public void clearNodeSet() {
        this.nodeSet = null;
    }

    public boolean isAccept() {
        return accept;
    }

    public void setAccept(boolean accept) {
        this.accept = accept;
    }

    public String toRepr() {
        StringBuilder sb = new StringBuilder();
        if (nodeSet == null) {
            sb.append(getId());
        } else {
            sb.append("{");
            sb.append(this.nodeSet.stream().map(Node::toRepr).collect(Collectors.joining(", ")));
            sb.append("}");
        }
        return sb.toString();
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
