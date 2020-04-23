package RegexFA.Graph;

public abstract class Node<G extends Graph<?>> {
    protected final G graph;
    protected final String id;

    public Node(G graph, String id) {
        this.graph = graph;
        this.id = id;
    }

    public G getGraph() {
        return graph;
    }

    public String getId() {
        return id;
    }

    public abstract boolean isAccept();

    public abstract String toRepr();

    @Override
    public String toString() {
        return "Node(" + toRepr() + ")";
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return this.graph.hashCode() ^ this.id.hashCode();
    }
}
