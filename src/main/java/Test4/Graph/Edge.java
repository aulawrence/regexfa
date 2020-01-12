package Test4.Graph;

public class Edge {
    public final Node fromNode;
    public final Node toNode;
    public final Character label;

    public Edge(Node fromNode, Node toNode, char label) {
        this.fromNode = fromNode;
        this.toNode = toNode;
        this.label = label;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != Edge.class) {
            return false;
        }
        Edge other = (Edge) obj;
        return this.fromNode.equals(other.fromNode) &&
                this.toNode.equals(other.toNode) &&
                this.label.equals(other.label);
    }

    @Override
    public int hashCode() {
        return this.fromNode.hashCode() ^ this.toNode.hashCode() ^ this.label.hashCode();
    }
}
