package RegexFA.Model;

import RegexFA.Alphabet;
import RegexFA.Graph.FAGraph;
import RegexFA.Graph.Graph;
import RegexFA.Graph.Node;
import RegexFA.Graph.SimpleNode;
import RegexFA.Parser.ParserException;
import RegexFA.Parser.RegexParser;

import java.awt.image.BufferedImage;
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

    private void generateGraph() {
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

    private void validateTestString() {
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

    private void generateColorNodeList() {
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

    public String getDotString() {
        return getDotString(selection);
    }

    public String getDotString(GraphChoice graphChoice) {
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

    public BufferedImage getImage(GraphChoice graphChoice) {
        return Graph.getImage(getDotString(graphChoice));
    }

    public GraphChoice getSelection() {
        return selection;
    }

    public void setSelection(GraphChoice selection) {
        this.selection = selection;
    }

    public boolean isRegexSuccess() {
        return regexSuccess;
    }

    public void setAlphabet(Alphabet alphabet) {
        this.alphabet = alphabet;
    }

    public void setRegex(String regex) {
        this.regex = regex;
        generateGraph();
    }

    public void setTestString(String testString) {
        this.testString = testString;
        validateTestString();
        generateColorNodeList();
    }

    public String getRegex() {
        return regex;
    }

    public String getTestString() {
        return testString;
    }

    public String getRegexErrMsg() {
        return regexErrMsg;
    }

    public boolean isTestStringSuccess() {
        return testStringSuccess;
    }

    public String getTestStringErrorMsg() {
        return testStringErrorMsg;
    }

    public void setTestStringPos(int testStringPos) {
        this.testStringPos = testStringPos;
    }

    public int getTestStringPos() {
        return testStringPos;
    }
}
