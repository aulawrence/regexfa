package RegexFA.Parser;

import RegexFA.Alphabet;
import RegexFA.Graph.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Optional;
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
        s.add('.');
        s.add('{');
        s.add('}');
        s.add(',');
        for (char ch = '0'; ch <= '9'; ch++) {
            s.add(ch);
        }
        int bracketCount = 0;
        boolean prevAtom = false;
        boolean inQuantifier = false;
        String minQuantifier = null;
        String maxQuantifier = null;
        char[] chArray = string.toCharArray();
        for (int i = 0; i < chArray.length; i++) {
            char ch = chArray[i];
            if (!s.contains(ch)) {
                throw new ParserException(ch, i, "It is not a valid character.");
            }
            if (!inQuantifier) {
                switch (ch) {
                    case '(':
                        bracketCount++;
                        prevAtom = false;
                        break;
                    case ')':
                        bracketCount--;
                        if (bracketCount < 0) {
                            throw new ParserException(')', i, "Cannot match opening bracket.");
                        }
                        prevAtom = true;
                        break;
                    case '+':
                    case '*':
                    case '?':
                        if (!prevAtom) {
                            throw new ParserException(ch, i, "The preceding character must be in the alphabet or a closing bracket.");
                        }
                        prevAtom = false;
                        break;
                    case '|':
                        prevAtom = false;
                        break;
                    case '.':
                        prevAtom = true;
                        break;
                    case '{':
                        if (!prevAtom) {
                            throw new ParserException(ch, i, "The preceding character must be in the alphabet or a closing bracket.");
                        }
                        inQuantifier = true;
                        minQuantifier = "";
                        maxQuantifier = null;
                        prevAtom = false;
                        break;
                    default:
                        if (ch == Alphabet.Empty || !alphabet.alphabetSet.contains(ch)) {
                            throw new ParserException(ch, i, "This character is not in the alphabet.");
                        }
                        prevAtom = true;
                        break;
                }
            } else {
                switch (ch) {
                    case '}':
                        if (minQuantifier.equals("")) {
                            throw new ParserException(ch, i, "A minimum value must be set.");
                        }
                        if (maxQuantifier != null && !maxQuantifier.equals("") && Integer.parseInt(minQuantifier) > Integer.parseInt(maxQuantifier)) {
                            throw new ParserException(ch, i, "The minimum value cannot be greater than the maximum value.");
                        }

                        inQuantifier = false;
                        minQuantifier = null;
                        maxQuantifier = null;
                        break;
                    case ',':
                        if (minQuantifier.equals("")) {
                            throw new ParserException(ch, i, "A minimum value must be set.");
                        }
                        if (maxQuantifier != null) {
                            throw new ParserException(ch, i, "Only one comma is allowed inside curly brackets.");
                        }
                        maxQuantifier = "";
                        break;
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                        if (maxQuantifier == null) {
                            minQuantifier += ch;
                        } else {
                            maxQuantifier += ch;
                        }
                        break;
                    default:
                        throw new ParserException(ch, i, "This character is not allowed inside curly brackets.");
                }
            }
        }
        if (bracketCount != 0) {
            throw new ParserException(String.format("Missing %d closing brackets.", bracketCount));
        }
        if (inQuantifier) {
            throw new ParserException("Missing closing curly braces.");
        }
    }

    private static String preprocessQuantifier(String string, Alphabet alphabet) throws ParserException {
        verify(string, alphabet);
        StringBuilder sb = new StringBuilder();
        Stack<StringBuilder> atomStack = new Stack<>();
        String currAtom = "";
        boolean inQuantifier = false;
        String minQuantifier = null;
        String maxQuantifier = null;
        char[] chArray = string.toCharArray();
        for (char ch : chArray) {
            StringBuilder target = sb;
            if (!atomStack.empty()) {
                target = atomStack.peek();
            }
            if (!inQuantifier) {
                switch (ch) {
                    case '(':
                        target.append(currAtom);
                        atomStack.push(new StringBuilder());
                        currAtom = "(";
                        break;
                    case ')':
                        target.append(currAtom);
                        currAtom = atomStack.pop().append(")").toString();
                        break;
                    case '{':
                        inQuantifier = true;
                        minQuantifier = "";
                        maxQuantifier = null;
                        break;
                    case '*':
                    case '?':
                    case '+':
                        target.append(currAtom);
                        target.append(ch);
                        currAtom = "";
                        break;
                    default:
                        target.append(currAtom);
                        currAtom = Character.toString(ch);
                }
            } else {
                switch (ch) {
                    case '}':
                        if (minQuantifier.equals("")) {
                            throw new IllegalStateException();
                        }
                        if (maxQuantifier != null && !maxQuantifier.equals("") && Integer.parseInt(minQuantifier) > Integer.parseInt(maxQuantifier)) {
                            throw new IllegalStateException();
                        }

                        Integer min = Integer.parseInt(minQuantifier);
                        Integer max;
                        if (maxQuantifier == null) {
                            max = min;
                        } else if (maxQuantifier.equals("")) {
                            max = null;
                        } else {
                            max = Integer.parseInt(maxQuantifier);
                        }

                        System.out.printf("Min: %s, Max: %s.%n", min.toString(), max == null ? "Inf" : max.toString());

                        if (min == 0) {
                            if (max == null) {
                                target.append(currAtom);
                                target.append("*");
                            } else if (max == 0) {
                                // Empty
                            } else if (max == 1) {
                                target.append(currAtom);
                                target.append("?");
                            } else {
                                target.append("(");
                                target.append(currAtom);
                                target.append("?");
                                for (int m = 2; m <= max; m++) {
                                    target.append("|");
                                    target.append(currAtom.repeat(m));
                                }
                                target.append(")");
                            }
                        } else {
                            target.append(currAtom.repeat(min - 1));
                            if (max == null) {
                                target.append(currAtom);
                                target.append("+");
                            } else if (max.equals(min)) {
                                target.append(currAtom);
                            } else {
                                target.append("(");
                                target.append(currAtom);
                                for (int m = min + 1; m <= max; m++) {
                                    target.append("|");
                                    target.append(currAtom.repeat(m - min + 1));
                                }
                                target.append(")");
                            }
                        }

                        currAtom = "";

                        inQuantifier = false;
                        minQuantifier = null;
                        maxQuantifier = null;
                        break;
                    case ',':
                        if (minQuantifier.equals("")) {
                            throw new IllegalStateException();
                        }
                        if (maxQuantifier != null) {
                            throw new IllegalStateException();
                        }
                        maxQuantifier = "";
                        break;
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                        if (maxQuantifier == null) {
                            minQuantifier += ch;
                        } else {
                            maxQuantifier += ch;
                        }
                        break;
                    default:
                        throw new IllegalStateException();
                }
            }
        }
        sb.append(currAtom);
        return sb.toString();
    }

    private static boolean addModifier(NFAGraph graph, NFANode prev, NFANode atomBegin, Character ch) {
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
        String prep = preprocessQuantifier(string, alphabet);
        NFAGraph graph = new NFAGraph(alphabet);
        Stack<NFANode> groupStack = new Stack<>();
        Stack<NFANode> orStack = new Stack<>();
        NFANode startNode = graph.addNode();
        graph.setRootNode(startNode);
        groupStack.push(startNode);
        NFANode startNode2 = graph.addNode();
        graph.addEdge(startNode, startNode2, Alphabet.Empty);
        NFANode curr = startNode2;
        NFANode prev = startNode2;
        int i = 0;
        while (i < prep.length()) {
            char chCurr = prep.charAt(i);
            Character chNext = i == prep.length() - 1 ? null : prep.charAt(i + 1);
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
                case '.':
                    curr = graph.addNode();
                    for (char ch : alphabet.alphabetList) {
                        if (ch != Alphabet.Empty) {
                            graph.addEdge(prev, curr, ch);
                        }
                    }
                    if (addModifier(graph, curr, prev, chNext)) {
                        prev = curr;
                        curr = graph.addNode();
                        graph.addEdge(prev, curr, Alphabet.Empty);
                        i++;
                    }
                    prev = curr;
                    break;
                default:
                    curr = graph.addNode();
                    graph.addEdge(prev, curr, chCurr);
                    if (addModifier(graph, curr, prev, chNext)) {
                        prev = curr;
                        curr = graph.addNode();
                        graph.addEdge(prev, curr, Alphabet.Empty);
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
        graph.setTerminalNode(curr);
        if (groupStack.pop() != startNode) throw new AssertionError();
        if (!orStack.isEmpty()) throw new AssertionError();
        if (!groupStack.isEmpty()) throw new AssertionError();
        return graph;
    }

    public static void main(String[] args) {
//        String pattern = "1{5,7}(0{0,2}){2}1{1,}(01){2}(10){2,2}";
//        String pattern = "(0|1(01*0)*1){2,5}";
//        String pattern1 = "(0|1)*1001";
//        String pattern2 = "(0|1)*(10)+(01)+";
        String pattern1 = "(0|1(01*0)*1)*";
        String pattern2 = "(0|1(01*0)*1){0,5}";
        Alphabet alphabet = Alphabet.Binary;

        try {
            GraphViz graphViz = new GraphViz(Paths.get("parser_img"));
            verify(pattern1, alphabet);
            System.out.println(preprocessQuantifier(pattern1, alphabet));
            NFAGraph g1 = toGraph(pattern1, alphabet);
            DFAGraph h1 = g1.toDFA().minimize();

            verify(pattern2, alphabet);
            System.out.println(preprocessQuantifier(pattern2, alphabet));
            NFAGraph g2 = toGraph(pattern2, alphabet);
            DFAGraph h2 = g2.toDFA().minimize();
            DFAGraph h3 = DFAGraph.xor(h1, h2).toDFA().minimize();

            h1.clearNodeSet();
            h2.clearNodeSet();
            h3.clearNodeSet();

            Graph.getImage(graphViz, g1.toDotString(), "g1");
            Graph.getImage(graphViz, g2.toDotString(), "g2");

            Graph.getImage(graphViz, h1.toDotString(), "h1");
            Graph.getImage(graphViz, h2.toDotString(), "h2");
            Graph.getImage(graphViz, h3.toDotString(), "h3");

            Optional<String> discrepancy = DFAGraph.getFirstDiscrepancyMin(h1, h2);
            if (discrepancy.isEmpty()) {
                System.out.printf("Patterns %s and %s are equivalent.%n", pattern1, pattern2);
            } else {
                System.out.printf("Patterns %s and %s are not equivalent:%n", pattern1, pattern2);
                System.out.printf("|- Test String   : %s%n", discrepancy.get());
                System.out.printf("|- First pattern : %s%n", h1.acceptsString(discrepancy.get()) ? "accepts" : "rejects");
                System.out.printf("|- Second pattern: %s%n", h2.acceptsString(discrepancy.get()) ? "accepts" : "rejects");
                System.out.println();
            }
        } catch (ParserException | IOException e) {
            e.printStackTrace();
        }
    }
}
