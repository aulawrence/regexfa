package Test4.Graph;

import Test4.Alphabet;

import java.util.*;
import java.util.stream.Collectors;

public class FAGraph extends Graph {
    private Node rootNode;

    public FAGraph(Alphabet alphabet) {
        super(alphabet);
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
        SimpleNode node = new SimpleNode(this, idString);
        this.nodeList.add(node);
        return node;
    }

    public Node addNode(Set<Node> nodeSet) {
        String idString = getIDString(getNextID());
        SimpleNode node = new SimpleNode(this, idString, nodeSet);
        this.nodeList.add(node);
        return node;
    }

    @Override
    public Edge addEdge(Node fromNode, Node toNode, char ch) {
        Edge edge = super.addEdge(fromNode, toNode, ch);
        SimpleNode node = (SimpleNode) fromNode;
        node.setEdge(ch, toNode);
        return edge;
    }

    public String toDotString() {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph {\n");
        sb.append("\n");
        for (Node node : nodeList) {
            sb.append(String.format("  %s [width=1, height=1", node.getId()));
            if (node.getNodeSet() != null){
                String s = node.getNodeSet().toString();
                sb.append(" label=\"");
                int k = (int) Math.sqrt(s.length())*2+1;
                for (int i = 0; i < s.length(); i++){
                    sb.append(s.charAt(i));
                    if ((i+1) % k == 0){
                        sb.append("\n");
                    }
                }
                sb.append("\"");
            }
            if (node.isAccept()){
                sb.append(" shape=doublecircle");
            }
            sb.append("];\n");
        }
        sb.append("  0 [width=0, height=0, label=\"\"];\n");
        sb.append("\n");
        if (rootNode != null) {
            sb.append(String.format("  0->%s;\n", rootNode.getId()));
        }
        for (Edge edge : edgeList) {
            sb.append(String.format("  %s->%s[label=\"%s\"];\n", edge.fromNode.getId(), edge.toNode.getId(), edge.label));
        }
        sb.append("\n");
        sb.append("}\n");
        return sb.toString();
    }

    public static FAGraph toDFA(FAGraph nfa){
        Alphabet alphabet = nfa.getAlphabet();
        FAGraph dfa = new FAGraph(alphabet);
        Map<Node, Map<Character, List<Node>>> listMap = new HashMap<>();
        for (Node node: nfa.getNodeList()){
            listMap.put(node, new HashMap<>());
        }
        for (Edge edge: nfa.getEdgeList()){
            if (listMap.get(edge.fromNode).containsKey(edge.label)){
                listMap.get(edge.fromNode).get(edge.label).add(edge.toNode);
            } else {
                List<Node> nodeList = new ArrayList<>();
                nodeList.add(edge.toNode);
                listMap.get(edge.fromNode).put(edge.label, nodeList);
            }
        }
        Map<Set<Node>, Node> nodeMap = new HashMap<>();
        Queue<Set<Node>> setQueue = new ArrayDeque<>();
        Set<Node> currSet = new HashSet<>(){
            @Override
            public String toString() {
                return "{" + this.stream().map(Node::getId).collect(Collectors.joining(", ")) + "}";
            }
        };
        Queue<Node> nodeQueue = new ArrayDeque<>();

        nodeQueue.add(nfa.getRootNode());
        while (!nodeQueue.isEmpty()){
            Node currNode = nodeQueue.poll();
            if (!currSet.contains(currNode)){
                currSet.add(currNode);
                nodeQueue.addAll(listMap.get(currNode).getOrDefault(Alphabet.Empty, List.of()));
            }
        }
        Node rootNode = dfa.addNode(currSet);
        for (Node node: currSet){
            if (node.isAccept()){
                rootNode.setAccept(true);
            }
        }
        nodeMap.put(currSet, rootNode);
        dfa.setRootNode(rootNode);

        setQueue.add(currSet);
        while (!setQueue.isEmpty()){
            Set<Node> prevSet = setQueue.poll();
            Set<Character> charSet = new HashSet<>();
            for (Node node: prevSet){
                charSet.addAll(listMap.get(node).keySet());
            }
            for (Character ch: charSet){
                if (ch != Alphabet.Empty) {
                    currSet = new HashSet<>(){
                        @Override
                        public String toString() {
                            return "{" + this.stream().map(Node::getId).collect(Collectors.joining(", ")) + "}";
                        }
                    };
                    for (Node node : prevSet) {
                        nodeQueue.addAll(listMap.get(node).getOrDefault(ch, List.of()));
                    }
                    while (!nodeQueue.isEmpty()) {
                        Node currNode = nodeQueue.poll();
                        if (!currSet.contains(currNode)) {
                            currSet.add(currNode);
                            nodeQueue.addAll(listMap.get(currNode).getOrDefault(Alphabet.Empty, List.of()));
                        }
                    }
                    if (!nodeMap.containsKey(currSet)) {
                        Node powerNode = dfa.addNode(currSet);
                        for (Node node: currSet){
                            if (node.isAccept()){
                                powerNode.setAccept(true);
                            }
                        }
                        nodeMap.put(currSet, powerNode);
                        dfa.addEdge(nodeMap.get(prevSet), powerNode, ch);
                        setQueue.add(currSet);
                    } else {
                        dfa.addEdge(nodeMap.get(prevSet), nodeMap.get(currSet), ch);
                    }
                }
            }
        }
        return dfa;
    }

    public static FAGraph minimize(FAGraph dfa){
        Map<Node, Integer> prevPartitions;
        Map<Node, Integer> currPartitions = new HashMap<>();
        for (Node node: dfa.getNodeList()){
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
                List<Node> partitionNodeList = new ArrayList<>();
                for (Node node: dfa.getNodeList()) {
                    if (prevPartitions.get(node) == p){
                        partitionNodeList.add(node);
                    }
                }
                boolean modified = false;
                if (partitionNodeList.size() >= 2){
                    for (int i = 0; i < partitionNodeList.size(); i++){
                        SimpleNode a = (SimpleNode) partitionNodeList.get(i);
                        if (currPartitions.get(a) == p) {
                            for (int j = i + 1; j < partitionNodeList.size(); j++) {
                                SimpleNode b = (SimpleNode) partitionNodeList.get(j);
                                if (currPartitions.get(b) == p) {
                                    for (int k = 0; k < dfa.getAlphabet().n; k++) {
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
        System.out.println(currPartitions.toString());
        FAGraph graph = new FAGraph(dfa.getAlphabet());
        Map<Integer, Node> newNodes = new HashMap<>();
        for (int p = 0; p < currN; p ++){
            Set<Node> nodeSet = new HashSet<>(){
                @Override
                public String toString() {
                    return "{" + this.stream().map(Node::getId).collect(Collectors.joining(", ")) + "}";
                }
            };
            boolean isAccept = false;
            boolean isRoot = false;
            for (Node node: dfa.getNodeList()) {
                if (currPartitions.get(node) == p){
                    nodeSet.addAll(node.getNodeSet());
                    if (node.isAccept()){
                        isAccept = true;
                    }
                    if (node == dfa.rootNode){
                        isRoot = true;
                    }
                }
            }
            if (!nodeSet.isEmpty()){
                Node newNode = graph.addNode(nodeSet);
                if (isAccept){
                    newNode.setAccept(true);
                }
                if (isRoot){
                    graph.setRootNode(newNode);
                }
                newNodes.put(p, newNode);
            }
        }

        for (Edge edge: dfa.getEdgeList()){
            Node newFromNode = newNodes.get(currPartitions.get(edge.fromNode));
            Node newToNode = newNodes.get(currPartitions.get(edge.toNode));
            graph.addEdge(newFromNode, newToNode, edge.label);
        }

        return graph;
    }

}
