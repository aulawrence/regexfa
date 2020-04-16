package RegexFA.Graph;

import java.util.Set;
import java.util.stream.Collectors;

public class Node {
    private final Graph<?> graph;
    private final String id;
    private final Set<Node> nodeSet;
    private boolean accept;

    public Node(Graph<?> graph, String id) {
        this(graph, id, null);
    }

    public Node(Graph<?> graph, String id, Set<Node> nodeSet) {
        this.graph = graph;
        this.id = id;
        this.nodeSet = nodeSet;
        this.accept = false;
    }

    public Graph<?> getGraph() {
        return graph;
    }

    public String getId() {
        return id;
    }

    public Set<Node> getNodeSet() {
        return nodeSet;
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
            sb.append(id);
        } else {
            sb.append("{");
            sb.append(this.nodeSet.stream().map(Node::toRepr).collect(Collectors.joining(", ")));
            sb.append("}");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Node(" + toRepr() + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != Node.class) {
            return false;
        }
        Node other = (Node) obj;
        return this.getGraph().equals(other.getGraph()) && this.getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return this.graph.hashCode() ^ this.id.hashCode();
    }
}
