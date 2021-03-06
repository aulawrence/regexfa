package RegexFA.Graph;

import RegexFA.Alphabet;

import java.util.*;

public class NFA {
    private NFA() {
    }

    public static DFAGraph toDFA(NFAGraph nfaGraph) {
        DFAGraph dfa = new DFAGraph(nfaGraph.getAlphabet());
        Map<NFANode, Map<Character, List<NFANode>>> listMap = new HashMap<>();
        for (NFANode node : nfaGraph.getNodes()) {
            listMap.put(node, new HashMap<>());
        }

        for (Edge<NFANode> edge : nfaGraph.getEdges()) {
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
        NFANode attachNode1 = g.addNode();
        NFANode attachNode2 = g.addNode();
        g.addEdge(rootNode, attachNode1, Alphabet.Empty);
        g.addEdge(rootNode, attachNode2, Alphabet.Empty);
        NFANode term1 = g.attachNFAGraph(attachNode1, g1);
        NFANode term2 = g.attachNFAGraph(attachNode2, g2);
        NFANode termNode = g.addNode();
        g.addEdge(term1, termNode, Alphabet.Empty);
        g.addEdge(term2, termNode, Alphabet.Empty);
        g.setTerminalNode(termNode);
        return g;
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
}
