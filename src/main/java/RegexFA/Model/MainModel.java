package RegexFA.Model;

import RegexFA.Alphabet;
import RegexFA.Graph.*;
import RegexFA.Parser.ParserException;
import RegexFA.Parser.RegexParser;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

public class MainModel extends Model implements Closeable {
    public enum GraphChoice {
        NFA,
        DFA,
        MinDFA
    }

    private final GraphViz graphViz;

    private Alphabet alphabet;
    private String regex;
    private String testString;
    private boolean regexSuccess;
    private String regexErrMsg;
    private boolean testStringSuccess;
    private String testStringErrorMsg;
    private int testStringPos;
    private final ArrayList<Node> testStringDFANodes;
    private GraphChoice selection;
    private NFAGraph nfa;
    private DFAGraph dfa;
    private DFAGraph min_dfa;

    public MainModel() throws IOException {
        regexSuccess = false;
        selection = GraphChoice.NFA;
        regex = "";
        testString = "";
        testStringPos = -1;
        testStringDFANodes = new ArrayList<>();
        graphViz = new GraphViz();
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
            testStringDFANodes.clear();
            DFANode curr = dfa.getRootNode();
            int i = 0;
            if (curr != null) {
                testStringDFANodes.add(curr);
            }
            while (curr != null && i < testString.length()) {
                curr = dfa.moveFromNode(curr, testString.charAt(i));
                if (curr != null) {
                    testStringDFANodes.add(curr);
                }
                i++;
            }
        } else {
            testStringDFANodes.clear();
        }
    }

    public synchronized String getDotString() {
        return getDotString(selection);
    }

    public synchronized String getDotString(GraphChoice graphChoice) {
        Node dfaNode = null;
        if (testStringPos + 1 < testStringDFANodes.size()) {
            dfaNode = testStringDFANodes.get(testStringPos + 1);
        }
        switch (graphChoice) {
            case DFA:
                return dfa.toDotString_colorDFA(dfaNode);
            case NFA:
                return nfa.toDotString_colorNFA(dfaNode);
            case MinDFA:
                return min_dfa.toDotString_colorMinDFA(dfaNode);
            default:
                throw new IllegalStateException();
        }
    }

    public synchronized Path getImage(GraphChoice graphChoice, String imgName) {
        return Graph.getImage(graphViz, getDotString(graphChoice), imgName);
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

    public synchronized String getRegex() {
        return regex;
    }

    public synchronized void setRegex(String regex) {
        this.regex = regex;
        generateGraph();
    }

    public synchronized String getTestString() {
        return testString;
    }

    public synchronized void setTestString(String testString) {
        this.testString = testString;
        validateTestString();
        generateColorNodeList();
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

    public synchronized int getTestStringPos() {
        return testStringPos;
    }

    public synchronized void setTestStringPos(int testStringPos) {
        this.testStringPos = testStringPos;
    }

    @Override
    public void close() {
        graphViz.close();
    }
}
