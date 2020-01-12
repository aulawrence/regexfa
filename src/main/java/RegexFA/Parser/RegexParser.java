package RegexFA.Parser;
import RegexFA.Alphabet;
import RegexFA.Graph.FAGraph;
import RegexFA.Graph.Graph;
import RegexFA.Graph.Node;
import java.util.*;

public class RegexParser {
//    private final String pattern;
//    private final Alphabet alphabet;

    //  <expression> ::= <term> '|' <expression>
    //                 | <term>
    //
    //  <term>       ::= <factor> <term>
    //                 | <factor>
    //
    //  <factor>     ::= <atom> '*'
    //                 | <atom>
    //
    //  <atom>       ::= <char>
    //                 | '(' <expression> ')'

    private RegexParser() {
    }

    public static void verify(String string, Alphabet alphabet) throws ParserException{
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
                    if (bracketCount < 0){
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
        if (bracketCount != 0){
            throw new ParserException(String.format("Unmatched brackets: Missing %d closing brackets.", bracketCount));
        }
    }

//    public static Expression parse(String string, Alphabet alphabet){
//
//    }

    private static boolean addModifier(Graph graph, Node prev, Node atomBegin, Character ch) {
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

    public static FAGraph toGraph(String string, Alphabet alphabet) throws ParserException {
        verify(string, alphabet);
        FAGraph graph = new FAGraph(alphabet);
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
//        String pattern = "(0|1(01*0)*1)*";
//        String pattern = "0*1*";
//        String pattern = "1|1+|0|0+";
        String pattern = "010|101";
        try {
            verify(pattern, Alphabet.Binary);
            FAGraph g = toGraph(pattern, Alphabet.Binary);
            System.out.println(g.toDotString());
            FAGraph h = FAGraph.toDFA(g);
            System.out.println(h.toDotString());
            FAGraph i = FAGraph.minimize(h);
            System.out.println(i.toDotString());
        } catch (ParserException e) {
            e.printStackTrace();
        }
    }
}
