package RegexFA.Graph;

public class Edge<N extends Node> {
    public final N fromNode;
    public final N toNode;
    public final Character label;

    public Edge(N fromNode, N toNode, char label) {
        this.fromNode = fromNode;
        this.toNode = toNode;
        this.label = label;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Edge)) {
            return false;
        }
        Edge<?> other = (Edge<?>) obj;
        return this.fromNode.equals(other.fromNode) &&
                this.toNode.equals(other.toNode) &&
                this.label.equals(other.label);
    }

    @Override
    public int hashCode() {
        return this.fromNode.hashCode() ^ this.toNode.hashCode() ^ this.label.hashCode();
    }
}
