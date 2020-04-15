package RegexFA.Model;

import RegexFA.Alphabet;
import RegexFA.Graph.*;
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

    private NFAGraph nfa;
    private DFAGraph dfa;
    private DFAGraph min_dfa;


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
            dfa = nfa.toDFA();
            min_dfa = dfa.minimize();
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
            DFANode curr = dfa.getRootNode();
            int i = 0;
            if (curr != null) {
                colorNodeList.add(curr.getNodeSet());
            }
            while (curr != null && i < testString.length()) {
                curr = dfa.moveFromNode(curr, testString.charAt(i));
                if (curr != null) {
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

    public synchronized BufferedImage getImage(GraphChoice graphChoice) {
        return Graph.getImage(getDotString(graphChoice));
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
