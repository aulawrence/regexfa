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
                        for (int k = 0; k < alphabet.n; k++) {
                            // Partition 1 is perpetual reject partition
                            int aPartNum = a.getEdge(k) == null ? 1 : prevPartitions.get(a.getEdge(k));
                            int bPartNum = b.getEdge(k) == null ? 1 : prevPartitions.get(b.getEdge(k));
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
            // Init i = 1 to ignore Alphabet.empty
            for (int i = 1; i < alphabet.n; i++) {
                char ch = alphabet.alphabetList.get(i);
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
        Alphabet alphabet = g1.alphabet;
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
