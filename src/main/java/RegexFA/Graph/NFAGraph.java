package RegexFA.Graph;

import RegexFA.Alphabet;

import java.util.*;
import java.util.function.Function;

public class NFAGraph extends Graph<NFANode> {
    private NFANode rootNode;
    private NFANode terminalNode;
    private StringBuilder edgeDotStringMemo;

    public NFAGraph(Alphabet alphabet) {
        super(alphabet);
        edgeDotStringMemo = null;
    }

    public NFAGraph(DFAGraph acceptanceGraph) {
        this(acceptanceGraph.alphabet);
        HashMap<DFANode, NFANode> dfaNfaNodeMap = new HashMap<>();
        Set<NFANode> acceptNodeSet = new HashSet<>();
        for (DFANode dfaNode : acceptanceGraph.getNodeList()) {
            NFANode nfaNode = addNode();
            if (dfaNode.isAccept()) {
                acceptNodeSet.add(nfaNode);
            }
            dfaNfaNodeMap.put(dfaNode, nfaNode);
        }
        for (Edge<DFANode> dfaEdge : acceptanceGraph.getEdgeList()) {
            addEdge(dfaNfaNodeMap.get(dfaEdge.fromNode), dfaNfaNodeMap.get(dfaEdge.toNode), dfaEdge.label);
        }
        setRootNode(dfaNfaNodeMap.get(acceptanceGraph.getRootNode()));
        NFANode termNode = addNode();
        setTerminalNode(termNode);
        for (NFANode nfaNode : acceptNodeSet) {
            addEdge(nfaNode, termNode, Alphabet.Empty);
        }
    }

    public NFANode getRootNode() {
        return rootNode;
    }

    public void setRootNode(NFANode rootNode) {
        this.rootNode = rootNode;
    }

    public NFANode getTerminalNode() {
        return terminalNode;
    }

    public void setTerminalNode(NFANode terminalNode) {
        this.terminalNode = terminalNode;
    }

    @Override
    public NFANode addNode() {
        String idString = getIDString(getNextID());
        NFANode node = new NFANode(this, idString);
        this.nodeList.add(node);
        return node;
    }

    @Override
    public Edge<NFANode> addEdge(NFANode fromNode, NFANode toNode, char ch) {
        edgeDotStringMemo = null;
        return super.addEdge(fromNode, toNode, ch);
    }

    @Override
    public void removeEdge(Edge<NFANode> edge) {
        edgeDotStringMemo = null;
        super.removeEdge(edge);
    }

    public String toDotString() {
        return toDotString((node) -> false, false);
    }

    public String toDotString(boolean bgTransparent) {
        return toDotString((node) -> false, bgTransparent);
    }

    public String toDotString_colorNFA(DFANode dfaNodes, boolean bgTransparent) {
        return toDotString((node) -> dfaNodes != null && dfaNodes.getNodeSet() != null && dfaNodes.getNodeSet().contains(node), bgTransparent);
    }

    public String toDotString(Function<NFANode, Boolean> colorPredicate, boolean bgTransparent) {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph {\n");
        if (bgTransparent) {
            sb.append("  bgcolor=transparent;\n");
        }
        sb.append("\n");
        for (NFANode node : nodeList) {
            sb.append(String.format("  %s [width=1 height=1", node.getId()));
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
            for (Edge<NFANode> edge : edgeList) {
                edgeDotStringMemo.append(String.format("  %s->%s[label=\"%s\"];\n", edge.fromNode.getId(), edge.toNode.getId(), edge.label));
            }
        }
        sb.append(edgeDotStringMemo);
        sb.append("\n");
        sb.append("}\n");
        return sb.toString();
    }

    public NFANode attachNFAGraph(NFANode toNode, NFAGraph g1) {
        HashMap<NFANode, NFANode> map = new HashMap<>();
        for (NFANode node1 : g1.getNodeList()) {
            NFANode node = addNode();
            map.put(node1, node);
        }
        for (Edge<NFANode> edge : g1.getEdgeList()) {
            addEdge(map.get(edge.fromNode), map.get(edge.toNode), edge.label);
        }
        addEdge(toNode, map.get(g1.getRootNode()), Alphabet.Empty);
        addEdge(terminalNode, map.get(g1.getRootNode()), Alphabet.Empty);
        return map.get(g1.getTerminalNode());
    }

    private static Set<NFANode> getNodesReachableByEmpty(Map<NFANode, Map<Character, List<NFANode>>> edgesMap, NFANode node) {
        return getNodesReachableByEmpty(edgesMap, Set.of(node));
    }

    private static Set<NFANode> getNodesReachableByEmpty(Map<NFANode, Map<Character, List<NFANode>>> edgesMap, Set<NFANode> nodeSet) {
        Set<NFANode> currSet = new HashSet<>();
        Queue<NFANode> nodeQueue = new ArrayDeque<>(nodeSet);
        while (!nodeQueue.isEmpty()) {
            NFANode currNode = nodeQueue.poll();
            if (!currSet.contains(currNode)) {
                currSet.add(currNode);
                nodeQueue.addAll(edgesMap.get(currNode).getOrDefault(Alphabet.Empty, List.of()));
            }
        }
        return currSet;
    }

    public DFAGraph toDFA() {
        return toDFA(this);
    }

    public NFAGraph or(NFAGraph other) {
        return or(this, other);
    }

    public static DFAGraph toDFA(NFAGraph nfaGraph) {
        DFAGraph dfa = new DFAGraph(nfaGraph.getAlphabet());
        Map<NFANode, Map<Character, List<NFANode>>> listMap = new HashMap<>();
        for (NFANode node : nfaGraph.getNodeList()) {
            listMap.put(node, new HashMap<>());
        }

        for (Edge<NFANode> edge : nfaGraph.getEdgeList()) {
            if (listMap.get(edge.fromNode).containsKey(edge.label)) {
                listMap.get(edge.fromNode).get(edge.label).add(edge.toNode);
            } else {
                List<NFANode> nodeList = new ArrayList<>();
                nodeList.add(edge.toNode);
                listMap.get(edge.fromNode).put(edge.label, nodeList);
            }
        }

        Map<Set<NFANode>, DFANode> nodeMap = new HashMap<>();
        Queue<Set<NFANode>> setQueue = new ArrayDeque<>();

        Set<NFANode> rootPowerSet = getNodesReachableByEmpty(listMap, nfaGraph.getRootNode());

        DFANode rootPowerNode = dfa.addNode(rootPowerSet);
        if (rootPowerSet.stream().anyMatch(NFANode::isAccept)) {
            rootPowerNode.setAccept(true);
        }
        nodeMap.put(rootPowerSet, rootPowerNode);
        dfa.setRootNode(rootPowerNode);

        setQueue.add(rootPowerSet);
        while (!setQueue.isEmpty()) {
            Set<NFANode> prevSet = setQueue.poll();
            Set<Character> charSet = new HashSet<>();
            for (NFANode node : prevSet) {
                charSet.addAll(listMap.get(node).keySet());
            }
            for (Character ch : charSet) {
                if (ch != Alphabet.Empty) {
                    Set<NFANode> currNodeSet = new HashSet<>();
                    for (NFANode node : prevSet) {
                        currNodeSet.addAll(listMap.get(node).getOrDefault(ch, List.of()));
                    }
                    Set<NFANode> currPowerSet = getNodesReachableByEmpty(listMap, currNodeSet);

                    if (!nodeMap.containsKey(currPowerSet)) {
                        DFANode currPowerNode = dfa.addNode(currPowerSet);
                        if (currPowerSet.stream().anyMatch(NFANode::isAccept)) {
                            currPowerNode.setAccept(true);
                        }
                        nodeMap.put(currPowerSet, currPowerNode);
                        dfa.addEdge(nodeMap.get(prevSet), currPowerNode, ch);
                        setQueue.add(currPowerSet);
                    } else {
                        dfa.addEdge(nodeMap.get(prevSet), nodeMap.get(currPowerSet), ch);
                    }
                }
            }
        }
        return dfa;
    }

    public static NFAGraph or(NFAGraph g1, NFAGraph g2) {
        assert g1.alphabet == g2.alphabet;
        Alphabet alphabet = g1.alphabet;
        NFAGraph g = new NFAGraph(alphabet);
        NFANode rootNode = g.addNode();
        g.setRootNode(rootNode);

        HashMap<NFANode, NFANode> map = new HashMap<>();
        for (NFANode node1 : g1.getNodeList()) {
            NFANode node = g.addNode();
            map.put(node1, node);
        }
        for (Edge<NFANode> edge : g1.getEdgeList()) {
            g.addEdge(map.get(edge.fromNode), map.get(edge.toNode), edge.label);
        }
        g.addEdge(rootNode, map.get(g1.getRootNode()), Alphabet.Empty);
        NFANode g1TermOnG = map.get(g1.getTerminalNode());

        map = new HashMap<>();
        for (NFANode node2 : g2.getNodeList()) {
            NFANode node = g.addNode();
            map.put(node2, node);
        }
        for (Edge<NFANode> edge : g2.getEdgeList()) {
            g.addEdge(map.get(edge.fromNode), map.get(edge.toNode), edge.label);
        }
        g.addEdge(rootNode, map.get(g2.getRootNode()), Alphabet.Empty);

        NFANode termNode = g.addNode();

        g.addEdge(g1TermOnG, termNode, Alphabet.Empty);
        g.addEdge(map.get(g2.getTerminalNode()), termNode, Alphabet.Empty);

        g.setTerminalNode(termNode);
        return g;
    }
}
