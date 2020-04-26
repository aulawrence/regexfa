package RegexFA.Graph;

import RegexFA.Alphabet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
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
        for (DFANode dfaNode : acceptanceGraph.getNodes()) {
            NFANode nfaNode = addNode();
            if (dfaNode.isAccept()) {
                acceptNodeSet.add(nfaNode);
            }
            dfaNfaNodeMap.put(dfaNode, nfaNode);
        }
        for (Edge<DFANode> dfaEdge : acceptanceGraph.getEdges()) {
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
        assert rootNode.getGraph() == this;
        this.rootNode = rootNode;
    }

    public NFANode getTerminalNode() {
        return terminalNode;
    }

    public void setTerminalNode(NFANode terminalNode) {
        assert terminalNode.getGraph() == this;
        this.terminalNode = terminalNode;
    }

    @Override
    public NFANode addNode() {
        String idString = getIDString(getNextID());
        NFANode node = new NFANode(this, idString);
        this.nodes.add(node);
        return node;
    }

    @Override
    public Set<Edge<NFANode>> removeNode(NFANode node) {
        Set<Edge<NFANode>> removedEdges = super.removeNode(node);
        if (!removedEdges.isEmpty()) {
            edgeDotStringMemo = null;
        }
        return removedEdges;
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
        return toDotString(false);
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
        for (NFANode node : nodes) {
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
            for (Edge<NFANode> edge : edges) {
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
        map.put(g1.getRootNode(), toNode);
        for (NFANode node1 : g1.getNodes()) {
            if (node1 != g1.getRootNode()) {
                NFANode node = addNode();
                map.put(node1, node);
            }
        }
        for (Edge<NFANode> edge : g1.getEdges()) {
            addEdge(map.get(edge.fromNode), map.get(edge.toNode), edge.label);
        }
        return map.get(g1.getTerminalNode());
    }

    public DFAGraph toDFA() {
        return NFA.toDFA(this);
    }

    public NFAGraph or(NFAGraph other) {
        return NFA.or(this, other);
    }
}
