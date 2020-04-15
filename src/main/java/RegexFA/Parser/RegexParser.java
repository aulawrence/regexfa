package RegexFA.Parser;

import RegexFA.Alphabet;
import RegexFA.Graph.DFAGraph;
import RegexFA.Graph.NFAGraph;
import RegexFA.Graph.Node;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class RegexParser {

    private RegexParser() {
    }

    public static void verify(String string, Alphabet alphabet) throws ParserException {
        HashSet<Character> s = new HashSet<>(alphabet.alphabetSet);
        s.add('(');
        s.add(')');
        s.add('+');
        s.add('*');
        s.add('?');
        s.add('|');
        int bracketCount = 0;
        boolean prevAtom = false;
        char[] chArray = string.toCharArray();
        for (int i = 0; i < chArray.length; i++) {
            char ch = chArray[i];
            if (!s.contains(ch)) {
                throw new ParserException(String.format("'%s' at position %d is not a valid character.", ch, i));
            }
            switch (ch) {
                case '(':
                    bracketCount++;
                    prevAtom = false;
                    break;
                case ')':
                    bracketCount--;
                    if (bracketCount < 0) {
                        throw new ParserException(String.format("')' at position %d is not valid. Cannot match opening bracket.", i));
                    }
                    prevAtom = true;
                    break;
                case '+':
                case '*':
                case '?':
                    if (!prevAtom) {
                        throw new ParserException(String.format("'%s' at position %d is not valid. The preceding character must be in the alphabet or a closing bracket.", ch, i));
                    }
                    prevAtom = false;
                    break;
                case '|':
                    prevAtom = false;
                    break;
                default:
                    prevAtom = true;
                    break;
            }
        }
        if (bracketCount != 0) {
            throw new ParserException(String.format("Unmatched brackets: Missing %d closing brackets.", bracketCount));
        }
    }

    private static boolean addModifier(NFAGraph graph, Node prev, Node atomBegin, Character ch) {
        if (ch != null) {
            switch (ch) {
                case '+':
                    graph.addEdge(prev, atomBegin, Alphabet.Empty);
                    return true;
                case '*':
                    graph.addEdge(prev, atomBegin, Alphabet.Empty);
                    graph.addEdge(atomBegin, prev, Alphabet.Empty);
                    return true;
                case '?':
                    graph.addEdge(atomBegin, prev, Alphabet.Empty);
                    return true;
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

    public static NFAGraph toGraph(String string, Alphabet alphabet) throws ParserException {
        verify(string, alphabet);
        NFAGraph graph = new NFAGraph(alphabet);
        Stack<Node> groupStack = new Stack<>();
        Stack<Node> orStack = new Stack<>();
        Node startNode = graph.addNode();
        graph.setRootNode(startNode);
        groupStack.push(startNode);
        Node startNode2 = graph.addNode();
        graph.addEdge(startNode, startNode2, Alphabet.Empty);
        Node curr = startNode2;
        Node prev = startNode2;
        int i = 0;
        while (i < string.length()) {
            char chCurr = string.charAt(i);
            Character chNext = i == string.length() - 1 ? null : string.charAt(i + 1);
            switch (chCurr) {
                case '(':
                    curr = graph.addNode();
                    graph.addEdge(prev, curr, Alphabet.Empty);
                    groupStack.push(curr);
                    prev = curr;
                    curr = graph.addNode();
                    graph.addEdge(prev, curr, Alphabet.Empty);
                    prev = curr;
                    break;
                case ')':
                    curr = graph.addNode();
                    graph.addEdge(prev, curr, Alphabet.Empty);
                    while (!orStack.empty() && orStack.peek() == groupStack.peek()) {
                        orStack.pop();
                        graph.addEdge(orStack.pop(), curr, Alphabet.Empty);
                    }
                    prev = curr;
                    if (addModifier(graph, curr, groupStack.peek(), chNext)) {
                        curr = graph.addNode();
                        graph.addEdge(prev, curr, Alphabet.Empty);
                        prev = curr;
                        i++;
                    }
                    groupStack.pop();
                    break;
                case '+':
                case '*':
                case '?':
                    throw new IllegalStateException();
                case '|':
                    orStack.push(curr);
                    orStack.push(groupStack.peek());
                    prev = groupStack.peek();
                    curr = graph.addNode();
                    graph.addEdge(prev, curr, Alphabet.Empty);
                    prev = curr;
                    break;
                default:
                    curr = graph.addNode();
                    graph.addEdge(prev, curr, chCurr);
                    if (addModifier(graph, curr, prev, chNext)) {
                        prev = curr;
                        curr = graph.addNode();
                        graph.addEdge(prev, curr, Alphabet.Empty);
                        prev = curr;
                        i++;
                    }
                    prev = curr;
                    break;
            }
            i++;
        }
        if (!orStack.isEmpty()) {
            curr = graph.addNode();
            graph.addEdge(prev, curr, Alphabet.Empty);
            while (!orStack.empty() && orStack.peek() == groupStack.peek()) {
                orStack.pop();
                graph.addEdge(orStack.pop(), curr, Alphabet.Empty);
            }
        }
        curr.setAccept(true);
        if (groupStack.pop() != startNode) throw new AssertionError();
        if (!orStack.isEmpty()) throw new AssertionError();
        if (!groupStack.isEmpty()) throw new AssertionError();
        return graph;
    }

    public static void main(String[] args) {
        String pattern = "(0|1(01*0)*1)*";
        try {
            verify(pattern, Alphabet.Binary);
            NFAGraph g = toGraph(pattern, Alphabet.Binary);
            DFAGraph h = g.toDFA();
            DFAGraph i = h.minimize();
            Set<Node> colorNode = h.getRootNode().getEdges()[Alphabet.Binary.invertMap.get('1')].getNodeSet();
            System.out.println(g.toDotString(colorNode));
            System.out.println(h.toDotString(colorNode));
            System.out.println(i.toDotString(colorNode));
        } catch (ParserException e) {
            e.printStackTrace();
        }
    }
}
