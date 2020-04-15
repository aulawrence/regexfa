package RegexFA.Graph;

import RegexFA.Alphabet;

import java.util.*;
import java.util.stream.Collectors;

public class DFAGraph extends Graph<DFANode>{
    private DFANode rootNode;

    public DFAGraph(Alphabet alphabet) {
        super(alphabet);
    }

    public DFANode getRootNode() {
        return rootNode;
    }

    public void setRootNode(DFANode rootNode) {
        this.rootNode = rootNode;
    }

    @Override
    public DFANode addNode() {
        String idString = getIDString(getNextID());
        DFANode node = new DFANode(this, idString);
        this.nodeList.add(node);
        return node;
    }

    public DFANode addNode(Set<Node> nodeSet) {
        String idString = getIDString(getNextID());
        DFANode node = new DFANode(this, idString, nodeSet);
        this.nodeList.add(node);
        return node;
    }

    @Override
    public Edge<DFANode> addEdge(DFANode fromNode, DFANode toNode, char ch) {
        Edge<DFANode> edge = super.addEdge(fromNode, toNode, ch);
        fromNode.setEdge(ch, toNode);
        return edge;
    }

    public String toDotString() {
        return toDotString(null);
    }

    public String toDotString(Set<Node> colorNodeSet) {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph {\n");
        sb.append("\n");
        for (Node node : nodeList) {
            sb.append(String.format("  %s [width=1, height=1", node.getId()));
            if (node.getNodeSet() != null){
                String s = node.toRepr();
                sb.append(" label=\"");
                int k = (int) Math.sqrt(s.length())*2+1;
                for (int i = 0; i < s.length(); i++){
                    sb.append(s.charAt(i));
                    if ((i+1) % k == 0){
                        sb.append("\\n");
                    }
                }
                sb.append("\"");

                if (colorNodeSet != null &&  (node.getNodeSet() == colorNodeSet || node.getNodeSet().stream().anyMatch((x) -> x.getNodeSet() == colorNodeSet))) {
                    sb.append(", color=red");
                }
            } else {
                if (colorNodeSet != null && colorNodeSet.contains(node)) {
                    sb.append(", color=red");
                }
            }
            if (node.isAccept()){
                sb.append(" peripheries=2");
            }
            sb.append("];\n");
        }
        sb.append("  0 [width=0, height=0, label=\"\"];\n");
        sb.append("\n");
        if (rootNode != null) {
            sb.append(String.format("  0->%s;\n", rootNode.getId()));
        }
        for (Edge<DFANode> edge : edgeList) {
            sb.append(String.format("  %s->%s[label=\"%s\"];\n", edge.fromNode.getId(), edge.toNode.getId(), edge.label));
        }
        sb.append("\n");
        sb.append("}\n");
        return sb.toString();
    }

    public DFAGraph minimize(){
        Map<Node, Integer> prevPartitions;
        Map<Node, Integer> currPartitions = new HashMap<>();
        for (Node node: this.getNodeList()){
            if (node.isAccept()){
                currPartitions.put(node, 0);
            } else {
                currPartitions.put(node, 1);
            }
        }
        int prevN = -1;
        int currN = 2;
        while (prevN != currN){
            prevN = currN;
            prevPartitions = currPartitions;
            currPartitions = new HashMap<>(prevPartitions);
            for (int p = 0; p < prevN; p ++){
                List<DFANode> partitionNodeList = new ArrayList<>();
                for (DFANode node: this.getNodeList()) {
                    if (prevPartitions.get(node) == p){
                        partitionNodeList.add(node);
                    }
                }
                boolean modified = false;
                if (partitionNodeList.size() >= 2){
                    for (int i = 0; i < partitionNodeList.size(); i++){
                        DFANode a = partitionNodeList.get(i);
                        if (currPartitions.get(a) == p) {
                            for (int j = i + 1; j < partitionNodeList.size(); j++) {
                                DFANode b = partitionNodeList.get(j);
                                if (currPartitions.get(b) == p) {
                                    for (int k = 0; k < this.getAlphabet().n; k++) {
                                        if (a.getEdges()[k] != null || b.getEdges()[k] != null){
                                            if (a.getEdges()[k] == null || b.getEdges()[k] == null || !prevPartitions.get(a.getEdges()[k]).equals(prevPartitions.get(b.getEdges()[k]))){
                                                currPartitions.put(b, currN);
                                                modified = true;
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (modified){
                    currN ++;
                }
            }
        }

        DFAGraph graph = new DFAGraph(this.getAlphabet());
        Map<Integer, DFANode> newNodes = new HashMap<>();
        for (int p = 0; p < currN; p ++){
            Set<Node> nodeSet = new HashSet<>(){
                @Override
                public String toString() {
                    return "{" + this.stream().map(Node::getId).collect(Collectors.joining(", ")) + "}";
                }
            };
            boolean isAccept = false;
            boolean isRoot = false;
            for (Node node: this.getNodeList()) {
                if (currPartitions.get(node) == p){
                    nodeSet.add(node);
                    if (node.isAccept()){
                        isAccept = true;
                    }
                    if (node == this.getRootNode()){
                        isRoot = true;
                    }
                }
            }
            if (!nodeSet.isEmpty()){
                DFANode newNode = graph.addNode(nodeSet);
                if (isAccept){
                    newNode.setAccept(true);
                }
                if (isRoot){
                    graph.setRootNode(newNode);
                }
                newNodes.put(p, newNode);
            }
        }

        for (Edge<DFANode> edge: this.getEdgeList()){
            DFANode newFromNode = newNodes.get(currPartitions.get(edge.fromNode));
            DFANode newToNode = newNodes.get(currPartitions.get(edge.toNode));
            graph.addEdge(newFromNode, newToNode, edge.label);
        }

        return graph;
    }

    public DFANode moveFromNode(DFANode curr, char ch) {
        return curr.getEdges()[alphabet.invertMap.get(ch)];
    }
}
