package RegexFA.Graph;

import RegexFA.Alphabet;

import java.util.*;
import java.util.function.Function;

public class DFAGraph extends Graph<DFANode> {
    private DFANode rootNode;
    private StringBuilder edgeDotStringMemo;

    public DFAGraph(Alphabet alphabet) {
        super(alphabet);
        edgeDotStringMemo = null;
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
        edgeDotStringMemo = null;
        return edge;
    }

    @Override
    public void removeEdge(Edge<DFANode> edge) {
        super.removeEdge(edge);
        edgeDotStringMemo = null;
    }

    public String toDotString() {
        return toDotString((node) -> false);
    }

    public String toDotString_colorDFA(Node dfaNode) {
        return toDotString((node) -> dfaNode != null && node == dfaNode);
    }

    public String toDotString_colorMinDFA(Node dfaNode) {
        return toDotString((node) -> dfaNode != null && node.getNodeSet().contains(dfaNode));
    }

    public String toDotString(Function<DFANode, Boolean> colorPredicate) {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph {\n");
        sb.append("\n");
        for (DFANode node : nodeList) {
            sb.append(String.format("  %s [width=1 height=1", node.getId()));
            if (node.getNodeSet() != null) {
                String s = node.toRepr();
                sb.append(" label=\"");
                int lineLen = (int) Math.sqrt(s.length()) * 2 + 3;
                for (int i = 0; i < s.length(); i += lineLen) {
                    if (i + lineLen < s.length()) {
                        sb.append(s, i, i + lineLen);
                        sb.append("\\n");
                    } else {
                        sb.append(s, i, s.length());
                    }
                }
                sb.append("\"");
            }
            if (colorPredicate.apply(node)) {
                sb.append(" color=red");
            }
            if (node.isAccept()) {
                sb.append(" peripheries=2");
            }
            sb.append("];\n");
        }
        sb.append("  0 [width=0 height=0 label=\"\"];\n");
        sb.append("\n");
        if (rootNode != null) {
            sb.append(String.format("  0->%s;\n", rootNode.getId()));
        }
        if (edgeDotStringMemo == null) {
            edgeDotStringMemo = new StringBuilder();
            for (Edge<DFANode> edge : edgeList) {
                edgeDotStringMemo.append(String.format("  %s->%s[label=\"%s\"];\n", edge.fromNode.getId(), edge.toNode.getId(), edge.label));
            }
        }
        sb.append(edgeDotStringMemo);
        sb.append("\n");
        sb.append("}\n");
        return sb.toString();
    }

    public DFAGraph minimize() {
        Map<Node, Integer> prevPartitions;
        Map<Node, Integer> currPartitions = new HashMap<>();
        for (Node node : nodeList) {
            if (node.isAccept()) {
                currPartitions.put(node, 0);
            } else {
                currPartitions.put(node, 1);
            }
        }
        int prevN = -1;
        int currN = 2;
        while (prevN != currN) {
            prevN = currN;
            prevPartitions = currPartitions;
            currPartitions = new HashMap<>(prevPartitions);
            for (int p = 0; p < prevN; p++) {
                List<DFANode> partitionNodeList = new ArrayList<>();
                for (DFANode node : nodeList) {
                    if (prevPartitions.get(node) == p) {
                        partitionNodeList.add(node);
                    }
                }
                boolean modified = false;
                // Singleton
                if (partitionNodeList.size() < 2) continue;
                for (int i = 0; i < partitionNodeList.size(); i++) {
                    DFANode a = partitionNodeList.get(i);
                    // a has already been moved to the new partition
                    if (currPartitions.get(a) != p) continue;
                    for (int j = i + 1; j < partitionNodeList.size(); j++) {
                        DFANode b = partitionNodeList.get(j);
                        // b has already been moved to the new partition
                        if (currPartitions.get(b) != p) continue;
                        for (int k = 0; k < alphabet.n; k++) {
                            int aPartNum = a.getEdges()[k] == null ? -1 : prevPartitions.get(a.getEdges()[k]);
                            int bPartNum = b.getEdges()[k] == null ? -1 : prevPartitions.get(b.getEdges()[k]);
                            if (aPartNum != bPartNum) {
                                currPartitions.put(b, currN);
                                modified = true;
                                break;
                            }
                        }
                    }
                }
                if (modified) {
                    currN++;
                }
            }
        }

        DFAGraph graph = new DFAGraph(alphabet);
        Map<Integer, DFANode> newNodes = new HashMap<>();
        for (int p = 0; p < currN; p++) {
            Set<Node> nodeSet = new HashSet<>();
            boolean isAccept = false;
            boolean isRoot = false;
            for (Node node : nodeList) {
                if (currPartitions.get(node) == p) {
                    nodeSet.add(node);
                    if (node.isAccept()) {
                        isAccept = true;
                    }
                    if (node == rootNode) {
                        isRoot = true;
                    }
                }
            }
            if (!nodeSet.isEmpty()) {
                DFANode newNode = graph.addNode(nodeSet);
                if (isAccept) {
                    newNode.setAccept(true);
                }
                if (isRoot) {
                    graph.setRootNode(newNode);
                }
                newNodes.put(p, newNode);
            }
        }

        for (Edge<DFANode> edge : edgeList) {
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
