package RegexFA.Graph;

import RegexFA.Alphabet;

import java.util.*;

public class DFA {
    private DFA() {
    }

    public static DFAGraph minimize(DFAGraph dfaGraph) {
        Alphabet alphabet = dfaGraph.getAlphabet();
        Map<DFANode, Integer> prevPartitions;
        Map<DFANode, Integer> currPartitions = new HashMap<>();
        for (DFANode node : dfaGraph.getNodes()) {
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
                for (DFANode node : dfaGraph.getNodes()) {
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
                        Map<Character, DFANode> aEdges = dfaGraph.getEdgesFrom(a);
                        Map<Character, DFANode> bEdges = dfaGraph.getEdgesFrom(b);
                        if (!(aEdges.keySet().equals(bEdges.keySet()))) {
                            currPartitions.put(b, currN);
                            modified = true;
                        } else {
                            for (char ch : aEdges.keySet()) {
                                if (!prevPartitions.get(aEdges.get(ch)).equals(prevPartitions.get(bEdges.get(ch)))) {
                                    currPartitions.put(b, currN);
                                    modified = true;
                                    break;
                                }
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
            Set<DFANode> nodeSet = new HashSet<>();
            boolean isAccept = false;
            boolean isRoot = false;
            for (DFANode node : dfaGraph.getNodes()) {
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

        for (Edge<DFANode> edge : dfaGraph.getEdges()) {
            DFANode newFromNode = newNodes.get(currPartitions.get(edge.fromNode));
            DFANode newToNode = newNodes.get(currPartitions.get(edge.toNode));
            graph.addEdge(newFromNode, newToNode, edge.label);
        }

        graph.pruneAbsorbingNodes();

        return graph;
    }


    public static DFAGraph not(DFAGraph dfaGraph) {
        Alphabet alphabet = dfaGraph.getAlphabet();
        DFAGraph negDFA = new DFAGraph(alphabet);
        HashMap<DFANode, DFANode> nodeMap = new HashMap<>();
        DFANode negNullNode = null;
        for (DFANode node : dfaGraph.getNodes()) {
            DFANode negNode = negDFA.addNode();
            negNode.setAccept(!node.isAccept());
            nodeMap.put(node, negNode);
            Set<Character> edgeCharSet = dfaGraph.getEdgesFrom(node).keySet();
            for (char ch : alphabet.alphabetSet) {
                if (!edgeCharSet.contains(ch)) {
                    if (negNullNode == null) {
                        negNullNode = negDFA.addNode();
                        negNullNode.setAccept(true);
                        for (char ch1 : alphabet.alphabetSet) {
                            negDFA.addEdge(negNullNode, negNullNode, ch1);
                        }
                    }
                    negDFA.addEdge(nodeMap.get(node), negNullNode, ch);
                }
            }
        }
        negDFA.setRootNode(nodeMap.get(dfaGraph.getRootNode()));
        for (Edge<DFANode> edge : dfaGraph.getEdges()) {
            negDFA.addEdge(nodeMap.get(edge.fromNode), nodeMap.get(edge.toNode), edge.label);
        }
        negDFA.pruneAbsorbingNodes();
        return negDFA;
    }

    public static NFAGraph xor(DFAGraph g1, DFAGraph g2) {
        assert g1.alphabet == g2.alphabet;
        DFAGraph g1n = g1.negate();
        DFAGraph g2n = g2.negate();
        DFAGraph alpha = NFA.or(g1.toNFA(), g2n.toNFA()).toDFA().negate();
        DFAGraph beta = NFA.or(g1n.toNFA(), g2.toNFA()).toDFA().negate();
        return NFA.or(alpha.toNFA(), beta.toNFA());
    }

    public static NFAGraph toNFA(DFAGraph g) {
        return new NFAGraph(g);
    }

    // TODO Maybe add stream of discrepancies

    public static Optional<String> getFirstAcceptString(DFAGraph g) {
        Alphabet alphabet = g.getAlphabet();
        HashMap<DFANode, String> stringMap = new HashMap<>();
        Queue<DFANode> nodeQueue = new ArrayDeque<>();
        DFANode rootNode = g.getRootNode();
        stringMap.put(rootNode, "");
        nodeQueue.add(rootNode);
        while (!nodeQueue.isEmpty()) {
            DFANode currNode = nodeQueue.poll();
            String currS = stringMap.get(currNode);
            if (currNode.isAccept()) {
                return Optional.of(currS);
            }
            for (char ch : alphabet.alphabetSet) {
                DFANode nextNode = g.moveFromNode(currNode, ch);
                if (nextNode != null && !stringMap.containsKey(nextNode)) {
                    stringMap.put(nextNode, currS + ch);
                    nodeQueue.add(nextNode);
                }
            }
        }
        return Optional.empty();
    }

    public static Optional<String> getFirstDiscrepancyMin(DFAGraph g1, DFAGraph g2) {
        return getFirstAcceptString(DFA.xor(g1, g2).toDFA().minimize());
    }

    public static boolean isEquivalentMin(DFAGraph g1, DFAGraph g2) {
        assert g1.alphabet == g2.alphabet;
        if (g1 == g2) {
            return true;
        } else {
            if ((g1.nodes.size() != g2.nodes.size()) || (g1.edges.size() != g2.edges.size())) {
                return false;
            }
            HashMap<DFANode, DFANode> nodeMap = new HashMap<>();
            HashMap<DFANode, DFANode> invNodeMap = new HashMap<>();
            HashMap<DFANode, String> stringMap = new HashMap<>();
            DFANode root1 = g1.getRootNode();
            DFANode root2 = g2.getRootNode();
            nodeMap.put(root1, root2);
            invNodeMap.put(root2, root1);
            stringMap.put(root1, "");
            Queue<DFANode> nodeQueue = new ArrayDeque<>();
            nodeQueue.add(root1);
            while (!nodeQueue.isEmpty()) {
                DFANode curr1 = nodeQueue.poll();
                DFANode curr2 = nodeMap.get(curr1);
                String s1 = stringMap.get(curr1);
                Map<Character, DFANode> edgeMap1 = g1.getEdgesFrom(curr1);
                Map<Character, DFANode> edgeMap2 = g2.getEdgesFrom(curr2);
                if (!edgeMap1.keySet().equals(edgeMap2.keySet())) {
                    // Some edges in one set are mapped to null in the other set
                    return false;
                }
                for (char ch : edgeMap1.keySet()) {
                    DFANode next1 = edgeMap1.get(ch);
                    DFANode next2 = edgeMap2.get(ch);
                    String nextS = s1 + ch;
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
        return true;
    }

    public static Optional<String> getFirstDiscrepancy(DFAGraph g1, DFAGraph g2) {
        return getFirstDiscrepancyMin(minimize(g1), minimize(g2));
    }

    public static boolean isEquivalent(DFAGraph g1, DFAGraph g2) {
        return isEquivalentMin(minimize(g1), minimize(g2));
    }
}
