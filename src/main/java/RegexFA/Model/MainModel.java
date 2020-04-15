package RegexFA.Model;

import RegexFA.Alphabet;
import RegexFA.Graph.FAGraph;
import RegexFA.Graph.Graph;
import RegexFA.Graph.Node;
import RegexFA.Graph.SimpleNode;
import RegexFA.Parser.ParserException;
import RegexFA.Parser.RegexParser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Set;

public class MainModel extends Model {
    public enum GraphChoice {
        NFA,
        DFA,
        MinDFA
    }

    private Alphabet alphabet;
    private String regex;
    private String testString;
    private boolean regexSuccess;
    private String regexErrMsg;
    private boolean testStringSuccess;
    private String testStringErrorMsg;
    private int testStringPos;
    private final ArrayList<Set<Node>> colorNodeList;
    private GraphChoice selection;

    private FAGraph nfa;
    private FAGraph dfa;
    private FAGraph min_dfa;


    public MainModel() {
        regexSuccess = false;
        selection = GraphChoice.NFA;
        regex = "";
        testString = "";
        testStringPos = -1;
        colorNodeList = new ArrayList<>();
    }

    private synchronized void generateGraph() {
        try {
            regexSuccess = false;
            regexErrMsg = "";
            nfa = RegexParser.toGraph(regex, alphabet);
            dfa = FAGraph.toDFA(nfa);
            min_dfa = FAGraph.minimize(dfa);
            regexSuccess = true;
        } catch (ParserException e) {
            regexErrMsg = e.getMessage();
        }
    }

    private synchronized void validateTestString() {
        for (int i = 0; i < testString.length(); i++) {
            if (!alphabet.invertMap.containsKey(testString.charAt(i))) {
                testStringSuccess = false;
                testStringErrorMsg = String.format("'%c' at position %d is not a valid character.", testString.charAt(i), i);
                return;
            }
        }
        testStringSuccess = true;
        testStringErrorMsg = "";
    }

    private synchronized void generateColorNodeList() {
        if (regexSuccess && testStringSuccess) {
            colorNodeList.clear();
            SimpleNode curr = dfa.getRootNode();
            int i = 0;
            if (curr != null) {
                System.out.printf("%s, %s%n", curr, curr.getNodeSet());
                colorNodeList.add(curr.getNodeSet());
            }
            while (curr != null && i < testString.length()) {
                curr = (SimpleNode) curr.getEdges()[alphabet.invertMap.get(testString.charAt(i))];
                if (curr != null) {
                    System.out.printf("%s, %s%n", curr, curr.getNodeSet());
                    colorNodeList.add(curr.getNodeSet());
                }
                i++;
            }
        } else {
            colorNodeList.clear();
        }
    }

    public synchronized String getDotString() {
        return getDotString(selection);
    }

    public synchronized String getDotString(GraphChoice graphChoice) {
        Set<Node> nodeSet = null;
        if (testStringPos + 1 < colorNodeList.size()) {
            nodeSet = colorNodeList.get(testStringPos + 1);
        }
        switch (graphChoice) {
            case DFA:
                return dfa.toDotString(nodeSet);
            case NFA:
                return nfa.toDotString(nodeSet);
            case MinDFA:
                return min_dfa.toDotString(nodeSet);
            default:
                throw new IllegalStateException();
        }
    }

    public synchronized InputStream getImageStream(GraphChoice graphChoice) {
        return Graph.getImageStream(getDotString(graphChoice));
    }

    public synchronized GraphChoice getSelection() {
        return selection;
    }

    public synchronized void setSelection(GraphChoice selection) {
        this.selection = selection;
    }

    public synchronized boolean isRegexSuccess() {
        return regexSuccess;
    }

    public synchronized void setAlphabet(Alphabet alphabet) {
        this.alphabet = alphabet;
    }

    public synchronized void setRegex(String regex) {
        this.regex = regex;
        generateGraph();
    }

    public synchronized void setTestString(String testString) {
        this.testString = testString;
        validateTestString();
        generateColorNodeList();
    }

    public synchronized String getRegex() {
        return regex;
    }

    public synchronized String getTestString() {
        return testString;
    }

    public synchronized String getRegexErrMsg() {
        return regexErrMsg;
    }

    public synchronized boolean isTestStringSuccess() {
        return testStringSuccess;
    }

    public synchronized String getTestStringErrorMsg() {
        return testStringErrorMsg;
    }

    public synchronized void setTestStringPos(int testStringPos) {
        this.testStringPos = testStringPos;
    }

    public synchronized int getTestStringPos() {
        return testStringPos;
    }
}
