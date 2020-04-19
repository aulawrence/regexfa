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
        return toDotString(false);
    }

    public String toDotString(boolean bgTransparent) {
        return toDotString((node) -> false, bgTransparent);
    }

    public String toDotString_colorDFA(Node dfaNode, boolean bgTransparent) {
        return toDotString((node) -> dfaNode != null && node == dfaNode, bgTransparent);
    }

    public String toDotString_colorMinDFA(Node dfaNode, boolean bgTransparent) {
        return toDotString((node) -> dfaNode != null && node.getNodeSet().contains(dfaNode), bgTransparent);
    }

    public String toDotString(Function<DFANode, Boolean> colorPredicate, boolean bgTransparent) {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph {\n");
        if (bgTransparent) {
            sb.append("  bgcolor=transparent;\n");
        }
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
                if (node.isAccept()) {
                    sb.append(" color=green3");
                } else {
                    sb.append(" color=red3");
                }
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

    public boolean acceptsString(String s) {
        for (char ch : s.toCharArray()) {
            if (ch == Alphabet.Empty || !alphabet.alphabetSet.contains(ch)) {
                return false;
            }
        }
        DFANode currNode = getRootNode();
        for (char ch : s.toCharArray()) {
            currNode = moveFromNode(currNode, ch);
            if (currNode == null) {
                return false;
            }
        }
        return currNode.isAccept();
    }

    public DFANode moveFromNode(DFANode curr, char ch) {
        return curr.getEdge(ch);
    }

    public DFAGraph minimize() {
        return minimize(this);
    }

    public DFAGraph negate() {
        return not(this);
    }

    public DFAGraph xor(DFAGraph other) {
        return xor(this, other);
    }

    public NFAGraph toNFA() {
        return toNFA(this);
    }

    public static DFAGraph minimize(DFAGraph dfaGraph) {
        Alphabet alphabet = dfaGraph.getAlphabet();
        Map<Node, Integer> prevPartitions;
        Map<Node, Integer> currPartitions = new HashMap<>();
        for (Node node : dfaGraph.getNodeList()) {
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
                for (DFANode node : dfaGraph.getNodeList()) {
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
                            int aPartNum = a.getEdge(k) == null ? -1 : prevPartitions.get(a.getEdge(k));
                            int bPartNum = b.getEdge(k) == null ? -1 : prevPartitions.get(b.getEdge(k));
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
            for (Node node : dfaGraph.getNodeList()) {
                if (currPartitions.get(node) == p) {
                    nodeSet.add(node);
                    if (node.isAccept()) {
                        isAccept = true;
                    }
                    if (node == dfaGraph.getRootNode()) {
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

        for (Edge<DFANode> edge : dfaGraph.getEdgeList()) {
            DFANode newFromNode = newNodes.get(currPartitions.get(edge.fromNode));
            DFANode newToNode = newNodes.get(currPartitions.get(edge.toNode));
            graph.addEdge(newFromNode, newToNode, edge.label);
        }

        return graph;
    }

    public static DFAGraph not(DFAGraph dfaGraph) {
        Alphabet alphabet = dfaGraph.getAlphabet();
        DFANode rootNode = dfaGraph.getRootNode();
        DFAGraph negDFA = new DFAGraph(alphabet);
        DFANode negRoot = negDFA.addNode();
        negRoot.setAccept(!rootNode.isAccept());
        negDFA.setRootNode(negRoot);
        HashMap<DFANode, DFANode> nodeMap = new HashMap<>();
        Queue<DFANode> nodeQueue = new ArrayDeque<>();
        nodeMap.put(rootNode, negRoot);
        nodeQueue.add(rootNode);
        while (!nodeQueue.isEmpty()) {
            DFANode curr = nodeQueue.poll();
            DFANode negCurr = nodeMap.get(curr);
            Set<Character> nullEdges = new HashSet<>();
//          Init i = 1 to ignore Alphabet.Empty
            for (int i = 1; i < alphabet.n; i++) {
                DFANode node = curr.getEdge(i);
                if (node == null) {
                    nullEdges.add(alphabet.alphabetList.get(i));
                } else {
                    DFANode negNode;
                    if (!nodeMap.containsKey(node)) {
                        negNode = negDFA.addNode();
                        negNode.setAccept(!node.isAccept());
                        nodeMap.put(node, negNode);
                        nodeQueue.add(node);
                    } else {
                        negNode = nodeMap.get(node);
                    }
                    negDFA.addEdge(negCurr, negNode, alphabet.alphabetList.get(i));
                }
            }
            if (!nullEdges.isEmpty()) {
                DFANode negNullNode = negDFA.addNode();
                negNullNode.setAccept(true);
                for (char ch : nullEdges) {
                    negDFA.addEdge(negCurr, negNullNode, ch);
                }
                for (int i = 1; i < alphabet.n; i++) {
                    negDFA.addEdge(negNullNode, negNullNode, alphabet.alphabetList.get(i));
                }
            }
        }
        return negDFA;
    }

    public static DFAGraph xor(DFAGraph g1, DFAGraph g2) {
        assert g1.alphabet == g2.alphabet;
        DFAGraph g1n = g1.negate();
        DFAGraph g2n = g2.negate();
        DFAGraph alpha = NFAGraph.or(g1.toNFA(), g2n.toNFA()).toDFA().negate();
        DFAGraph beta = NFAGraph.or(g1n.toNFA(), g2.toNFA()).toDFA().negate();
        return NFAGraph.or(alpha.toNFA(), beta.toNFA()).toDFA();
    }

    public static NFAGraph toNFA(DFAGraph g) {
        return new NFAGraph(g);
    }

    // TODO Maybe add stream of discrepancies

    public static Optional<String> getFirstDiscrepancyMin(DFAGraph g1, DFAGraph g2) {
        DFAGraph result = xor(g1, g2).minimize();
        Alphabet alphabet = result.getAlphabet();
        HashMap<DFANode, String> stringMap = new HashMap<>();
        Queue<DFANode> nodeQueue = new ArrayDeque<>();
        DFANode rootNode = result.getRootNode();
        stringMap.put(rootNode, "");
        nodeQueue.add(rootNode);
        while (!nodeQueue.isEmpty()) {
            DFANode currNode = nodeQueue.poll();
            String currS = stringMap.get(currNode);
            if (currNode.isAccept()) {
                return Optional.of(currS);
            }
            // Init i = 1 to ignore Alphabet.empty
            for (int i = 1; i < alphabet.n; i++) {
                char ch = alphabet.alphabetList.get(i);
                DFANode nextNode = result.moveFromNode(currNode, ch);
                if (nextNode != null && !stringMap.containsKey(nextNode)) {
                    stringMap.put(nextNode, currS + ch);
                    nodeQueue.add(nextNode);
                }
            }
        }
        return Optional.empty();
    }

    public static boolean isEquivalentMin(DFAGraph g1, DFAGraph g2) {
        assert g1.alphabet == g2.alphabet;
        Alphabet alphabet = g1.alphabet;
        if (g1 == g2) {
            return true;
        } else {
            if ((g1.nodeList.size() != g2.nodeList.size()) || (g1.edgeList.size() != g2.edgeList.size())) {
                return false;
            }
            HashMap<DFANode, DFANode> nodeMap = new HashMap<>();
            HashMap<DFANode, DFANode> invNodeMap = new HashMap<>();
            HashMap<DFANode, String> stringMap = new HashMap<>();
            DFANode root1 = g1.rootNode;
            DFANode root2 = g2.rootNode;
            nodeMap.put(root1, root2);
            nodeMap.put(root2, root1);
            stringMap.put(root1, "");
            Queue<DFANode> nodeQueue = new ArrayDeque<>();
            nodeQueue.add(root1);
            while (!nodeQueue.isEmpty()) {
                DFANode curr1 = nodeQueue.poll();
                DFANode curr2 = nodeMap.get(curr1);
                String s1 = stringMap.get(curr1);

                // Init i = 1 to ignore Alphabet.empty
                for (int i = 1; i < alphabet.n; i++) {
                    char ch = alphabet.alphabetList.get(i);
                    DFANode next1 = g1.moveFromNode(curr1, ch);
                    DFANode next2 = g2.moveFromNode(curr2, ch);
                    String nextS = s1 + ch;

                    if (next1 != null || next2 != null) {
                        if (next1 == null || next2 == null) {
                            // One null, other non-null
                            return false;
                        }
                        if (next1.isAccept() != next2.isAccept()) {
                            // One accept, other reject
                            return false;
                        }
                        if (nodeMap.containsKey(next1)) {
                            // Both seen but inconsistent
                            if (nodeMap.get(next1) != next2 || invNodeMap.get(next2) != next1) {
                                return false;
                            }
                            // Both seen and consistent -> ok
                        } else {
                            if (invNodeMap.containsKey(next2)) {
                                // next1 unseen, next2 seen
                                return false;
                            } else {
                                // both unseen -> add to map
                                nodeMap.put(next1, next2);
                                invNodeMap.put(next2, next1);
                                stringMap.put(next1, nextS);
                                nodeQueue.add(next1);
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    public static Optional<String> getFirstDiscrepancy(DFAGraph g1, DFAGraph g2) {
        return getFirstDiscrepancyMin(minimize(g1), minimize(g2));
    }

    public static boolean isEquivalent(DFAGraph g1, DFAGraph g2) {
        return isEquivalentMin(minimize(g1), minimize(g2));
    }
}
