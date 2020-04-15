package RegexFA.Graph;

import RegexFA.Alphabet;

import java.util.*;
import java.util.stream.Collectors;

public class NFAGraph extends Graph<Node> {
    private Node rootNode;

    public NFAGraph(Alphabet alphabet) {
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
        Node node = new Node(this, idString);
        this.nodeList.add(node);
        return node;
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
        for (Edge<Node> edge : edgeList) {
            sb.append(String.format("  %s->%s[label=\"%s\"];\n", edge.fromNode.getId(), edge.toNode.getId(), edge.label));
        }
        sb.append("\n");
        sb.append("}\n");
        return sb.toString();
    }

    public DFAGraph toDFA(){
        Alphabet alphabet = this.getAlphabet();
        DFAGraph dfa = new DFAGraph(alphabet);
        Map<Node, Map<Character, List<Node>>> listMap = new HashMap<>();
        for (Node node: this.getNodeList()){
            listMap.put(node, new HashMap<>());
        }
        for (Edge<Node> edge: this.getEdgeList()){
            if (listMap.get(edge.fromNode).containsKey(edge.label)){
                listMap.get(edge.fromNode).get(edge.label).add(edge.toNode);
            } else {
                List<Node> nodeList = new ArrayList<>();
                nodeList.add(edge.toNode);
                listMap.get(edge.fromNode).put(edge.label, nodeList);
            }
        }
        Map<Set<Node>, DFANode> nodeMap = new HashMap<>();
        Queue<Set<Node>> setQueue = new ArrayDeque<>();
        Set<Node> currSet = new HashSet<>(){
            @Override
            public String toString() {
                return "{" + this.stream().map(Node::getId).collect(Collectors.joining(", ")) + "}";
            }
        };
        Queue<Node> nodeQueue = new ArrayDeque<>();

        nodeQueue.add(this.getRootNode());
        while (!nodeQueue.isEmpty()){
            Node currNode = nodeQueue.poll();
            if (!currSet.contains(currNode)){
                currSet.add(currNode);
                nodeQueue.addAll(listMap.get(currNode).getOrDefault(Alphabet.Empty, List.of()));
            }
        }
        DFANode rootNode = dfa.addNode(currSet);
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
                        DFANode powerNode = dfa.addNode(currSet);
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

}
