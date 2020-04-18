package RegexFA.Graph;

import RegexFA.Alphabet;

import java.util.*;
import java.util.function.Function;

public class NFAGraph extends Graph<Node> {
    private Node rootNode;
    private StringBuilder edgeDotStringMemo;

    public NFAGraph(Alphabet alphabet) {
        super(alphabet);
        edgeDotStringMemo = null;
    }

    public Node getRootNode() {
        return rootNode;
    }

    public void setRootNode(Node rootNode) {
        this.rootNode = rootNode;
    }

    @Override
    public Node addNode() {
        String idString = getIDString(getNextID());
        Node node = new Node(this, idString);
        this.nodeList.add(node);
        return node;
    }

    @Override
    public Edge<Node> addEdge(Node fromNode, Node toNode, char ch) {
        edgeDotStringMemo = null;
        return super.addEdge(fromNode, toNode, ch);
    }

    @Override
    public void removeEdge(Edge<Node> edge) {
        edgeDotStringMemo = null;
        super.removeEdge(edge);
    }

    public String toDotString() {
        return toDotString((node) -> false);
    }

    public String toDotString_colorNFA(Node dfaNodes) {
        return toDotString((node) -> dfaNodes != null && dfaNodes.getNodeSet() != null && dfaNodes.getNodeSet().contains(node));
    }

    public String toDotString(Function<Node, Boolean> colorPredicate) {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph {\n");
        sb.append("\n");
        for (Node node : nodeList) {
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
            for (Edge<Node> edge : edgeList) {
                edgeDotStringMemo.append(String.format("  %s->%s[label=\"%s\"];\n", edge.fromNode.getId(), edge.toNode.getId(), edge.label));
            }
        }
        sb.append(edgeDotStringMemo);
        sb.append("\n");
        sb.append("}\n");
        return sb.toString();
    }

    private static Set<Node> getNodesReachableByEmpty(Map<Node, Map<Character, List<Node>>> edgesMap, Node node) {
        Set<Node> currSet = new HashSet<>();
        Queue<Node> nodeQueue = new ArrayDeque<>();
        nodeQueue.add(node);
        while (!nodeQueue.isEmpty()) {
            Node currNode = nodeQueue.poll();
            if (!currSet.contains(currNode)) {
                currSet.add(currNode);
                nodeQueue.addAll(edgesMap.get(currNode).getOrDefault(Alphabet.Empty, List.of()));
            }
        }
        return currSet;
    }

    private static Set<Node> getNodesReachableByEmpty(Map<Node, Map<Character, List<Node>>> edgesMap, Set<Node> nodeSet) {
        Set<Node> currSet = new HashSet<>();
        Queue<Node> nodeQueue = new ArrayDeque<>(nodeSet);
        while (!nodeQueue.isEmpty()) {
            Node currNode = nodeQueue.poll();
            if (!currSet.contains(currNode)) {
                currSet.add(currNode);
                nodeQueue.addAll(edgesMap.get(currNode).getOrDefault(Alphabet.Empty, List.of()));
            }
        }
        return currSet;
    }

    public DFAGraph toDFA() {
        DFAGraph dfa = new DFAGraph(alphabet);
        Map<Node, Map<Character, List<Node>>> listMap = new HashMap<>();
        for (Node node : nodeList) {
            listMap.put(node, new HashMap<>());
        }

        for (Edge<Node> edge : edgeList) {
            if (listMap.get(edge.fromNode).containsKey(edge.label)) {
                listMap.get(edge.fromNode).get(edge.label).add(edge.toNode);
            } else {
                List<Node> nodeList = new ArrayList<>();
                nodeList.add(edge.toNode);
                listMap.get(edge.fromNode).put(edge.label, nodeList);
            }
        }

        Map<Set<Node>, DFANode> nodeMap = new HashMap<>();
        Queue<Set<Node>> setQueue = new ArrayDeque<>();

        Set<Node> rootPowerSet = getNodesReachableByEmpty(listMap, rootNode);

        DFANode rootPowerNode = dfa.addNode(rootPowerSet);
        if (rootPowerSet.stream().anyMatch(Node::isAccept)) {
            rootPowerNode.setAccept(true);
        }
        nodeMap.put(rootPowerSet, rootPowerNode);
        dfa.setRootNode(rootPowerNode);

        setQueue.add(rootPowerSet);
        while (!setQueue.isEmpty()) {
            Set<Node> prevSet = setQueue.poll();
            Set<Character> charSet = new HashSet<>();
            for (Node node : prevSet) {
                charSet.addAll(listMap.get(node).keySet());
            }
            for (Character ch : charSet) {
                if (ch != Alphabet.Empty) {
                    Set<Node> currNodeSet = new HashSet<>();
                    for (Node node : prevSet) {
                        currNodeSet.addAll(listMap.get(node).getOrDefault(ch, List.of()));
                    }
                    Set<Node> currPowerSet = getNodesReachableByEmpty(listMap, currNodeSet);

                    if (!nodeMap.containsKey(currPowerSet)) {
                        DFANode currPowerNode = dfa.addNode(currPowerSet);
                        if (currPowerSet.stream().anyMatch(Node::isAccept)) {
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

}
