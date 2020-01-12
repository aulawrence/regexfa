package RegexFA.Graph;

import java.util.Set;

public class Node {
    private final Graph graph;
    private final String id;
    private final Set<Node> nodeSet;
    private boolean accept;

    public Node(Graph graph, String id) {
        this(graph, id, null);
    }

    public Node(Graph graph, String id, Set<Node> nodeSet) {
        this.graph = graph;
        this.id = id;
        this.nodeSet = nodeSet;
        this.accept = false;
    }

    public Graph getGraph() {
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

    @Override
    public String toString() {
        return "Node{" + id + '}';
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
