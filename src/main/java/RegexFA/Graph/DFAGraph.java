package RegexFA.Graph;

import RegexFA.Alphabet;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DFAGraph extends Graph<DFANode> {
    private DFANode rootNode;
    private StringBuilder edgeDotStringMemo;
    private final Map<DFANode, Map<Character, DFANode>> edgeMapForward;
    private final Map<DFANode, Set<Edge<DFANode>>> edgeMapBackward;

    public DFAGraph(Alphabet alphabet) {
        super(alphabet);
        edgeDotStringMemo = null;
        edgeMapForward = new HashMap<>();
        edgeMapBackward = new HashMap<>();
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
        this.nodes.add(node);
        this.edgeMapForward.put(node, new HashMap<>());
        this.edgeMapBackward.put(node, new HashSet<>());
        return node;
    }

    public DFANode addNode(Set<? extends Node<?>> nodeSet) {
        String idString = getIDString(getNextID());
        DFANode node = new DFANode(this, idString, new HashSet<>(nodeSet));
        this.nodes.add(node);
        this.edgeMapForward.put(node, new HashMap<>());
        this.edgeMapBackward.put(node, new HashSet<>());
        return node;
    }

    @Override
    public Set<Edge<DFANode>> removeNode(DFANode node) {
        this.nodes.remove(node);
        Set<Edge<DFANode>> removedEdgesForward = edgeMapForward.remove(node).entrySet().stream().map(e -> new Edge<>(node, e.getValue(), e.getKey())).collect(Collectors.toSet());
        Set<Edge<DFANode>> removedEdgesBackward = edgeMapBackward.remove(node);
        if (!removedEdgesForward.isEmpty()) {
            edgeDotStringMemo = null;
            edges.removeAll(removedEdgesForward);
            for (Edge<DFANode> edge : removedEdgesForward) {
                if (edgeMapBackward.containsKey(edge.toNode)) {
                    edgeMapBackward.get(edge.toNode).remove(edge);
                }
            }
        }
        if (!removedEdgesBackward.isEmpty()) {
            edgeDotStringMemo = null;
            edges.removeAll(removedEdgesBackward);
            for (Edge<DFANode> edge : removedEdgesBackward) {
                if (edgeMapForward.containsKey(edge.fromNode)) {
                    edgeMapForward.get(edge.fromNode).remove(edge.label);
                }
            }
        }
        removedEdgesForward.addAll(removedEdgesBackward);
        return removedEdgesForward;
    }

    @Override
    public Edge<DFANode> addEdge(DFANode fromNode, DFANode toNode, char ch) {
        Edge<DFANode> edge = super.addEdge(fromNode, toNode, ch);
        if (edge != null) {
            edgeMapForward.get(fromNode).put(ch, toNode);
            edgeMapBackward.get(toNode).add(edge);
            edgeDotStringMemo = null;
        }
        return edge;
    }

    @Override
    public void removeEdge(Edge<DFANode> edge) {
        super.removeEdge(edge);
        if (edgeMapForward.containsKey(edge.fromNode)) {
            edgeMapForward.get(edge.fromNode).remove(edge.label);
        }
        if (edgeMapBackward.containsKey(edge.toNode)) {
            edgeMapBackward.get(edge.toNode).remove(edge);
        }
        edgeDotStringMemo = null;
    }

    public DFAGraph clearNodeSet() {
        for (DFANode node : nodes) {
            node.clearNodeSet();
        }
        return this;
    }

    public Map<Character, DFANode> getEdgesFrom(DFANode fromNode) {
        return Collections.unmodifiableMap(edgeMapForward.get(fromNode));
    }

    public Set<Edge<DFANode>> getEdgesTo(DFANode toNode) {
        return Collections.unmodifiableSet(edgeMapBackward.get(toNode));
    }

    public String toDotString() {
        return toDotString(false);
    }

    public String toDotString(boolean bgTransparent) {
        return toDotString((node) -> false, bgTransparent);
    }

    public String toDotString_colorDFA(DFANode dfaNode, boolean bgTransparent) {
        return toDotString((node) -> dfaNode != null && node == dfaNode, bgTransparent);
    }

    public String toDotString_colorMinDFA(DFANode dfaNode, boolean bgTransparent) {
        return toDotString((node) -> dfaNode != null && node.getNodeSet().contains(dfaNode), bgTransparent);
    }

    public String toDotString(Function<DFANode, Boolean> colorPredicate, boolean bgTransparent) {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph {\n");
        if (bgTransparent) {
            sb.append("  bgcolor=transparent;\n");
        }
        sb.append("\n");
        for (DFANode node : nodes) {
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
            for (Edge<DFANode> edge : edges) {
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
            if (!alphabet.alphabetSet.contains(ch)) {
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
        return edgeMapForward.get(curr).getOrDefault(ch, null);
    }

    public DFAGraph minimize() {
        return DFA.minimize(this);
    }

    public DFAGraph negate() {
        return DFA.not(this);
    }

    public NFAGraph xor(DFAGraph other) {
        return DFA.xor(this, other);
    }

    public NFAGraph toNFA() {
        return DFA.toNFA(this);
    }

    public Set<DFANode> getAliveNodes() {
        Set<DFANode> aliveSet = new HashSet<>(nodes);
        aliveSet.removeIf(x -> !x.isAccept());
        Set<DFANode> workingSet = aliveSet.stream().flatMap(x -> edgeMapBackward.get(x).stream().map(y -> y.fromNode)).filter(z -> !aliveSet.contains(z)).collect(Collectors.toSet());
        while (!workingSet.isEmpty()) {
            aliveSet.addAll(workingSet);
            workingSet = workingSet.stream().flatMap(x -> edgeMapBackward.get(x).stream().map(y -> y.fromNode)).filter(z -> !aliveSet.contains(z)).collect(Collectors.toSet());
        }
        return aliveSet;
    }

    public Set<DFANode> getReachableNodes() {
        Set<DFANode> reachableSet = new HashSet<>();
        reachableSet.add(rootNode);
        Set<DFANode> workingSet = reachableSet.stream().flatMap(x -> edgeMapForward.get(x).values().stream()).filter(z -> !reachableSet.contains(z)).collect(Collectors.toSet());
        while (!workingSet.isEmpty()) {
            reachableSet.addAll(workingSet);
            workingSet = workingSet.stream().flatMap(x -> edgeMapForward.get(x).values().stream()).filter(z -> !reachableSet.contains(z)).collect(Collectors.toSet());
        }
        return reachableSet;
    }

    public void pruneAbsorbingNodes() {
        Set<DFANode> absorbingSet = new HashSet<>(nodes);
        absorbingSet.removeAll(getAliveNodes());
        for (DFANode dfaNode : absorbingSet) {
            if (dfaNode != rootNode) {
                removeNode(dfaNode);
            } else {
                // Root node was about to be removed. Instead, keep root node add edges connecting to itself.
                for (char ch : alphabet.alphabetSet) {
                    addEdge(dfaNode, dfaNode, ch);
                }
            }
        }
    }
}
