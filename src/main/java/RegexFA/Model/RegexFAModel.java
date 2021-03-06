package RegexFA.Model;

import RegexFA.Alphabet;
import RegexFA.Graph.DFAGraph;
import RegexFA.Graph.DFANode;
import RegexFA.Graph.GraphViz;
import RegexFA.Graph.NFAGraph;
import RegexFA.Parser.Expression;
import RegexFA.Parser.ParserException;
import RegexFA.Parser.RegexParser;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

public class RegexFAModel extends Model implements Closeable {

    private final GraphViz graphViz;

    private Alphabet alphabet;
    private String regex;
    private String testString;
    private boolean regexSuccess;
    private String regexErrMsg;
    private boolean testStringSuccess;
    private String testStringErrorMsg;
    private int testStringPos;
    private final ArrayList<DFANode> testStringDFANodes;
    private NFAGraph nfa;
    private DFAGraph dfa;
    private DFAGraph min_dfa;

    public RegexFAModel() throws IOException {
        regexSuccess = false;
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
            Expression expression = RegexParser.parse(regex, alphabet);
            nfa = RegexParser.toGraph(expression, alphabet);
            dfa = nfa.toDFA();
            min_dfa = dfa.minimize();
            regexSuccess = true;
        } catch (ParserException e) {
            regexErrMsg = e.getMessage();
        }
    }

    private synchronized void validateTestString() {
        for (int i = 0; i < testString.length(); i++) {
            char ch = testString.charAt(i);
            if (!alphabet.alphabetSet.contains(ch)) {
                testStringSuccess = false;
                testStringErrorMsg = String.format("'%c' at position %d is not valid. This character is not in the alphabet.", ch, i);
                return;
            }
        }
        testStringSuccess = true;
        testStringErrorMsg = "";
    }

    private synchronized void generateTestStringLists() {
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

    public synchronized String getDotString(GraphPanelModel.GraphChoice graphChoice) {
        DFANode dfaNode = null;
        if (testStringPos + 1 < testStringDFANodes.size()) {
            dfaNode = testStringDFANodes.get(testStringPos + 1);
        }
        switch (graphChoice) {
            case Graph1:
                return nfa.toDotString_colorNFA(dfaNode, true);
            case Graph2:
                return dfa.toDotString_colorDFA(dfaNode, true);
            case Graph3:
                return min_dfa.toDotString_colorMinDFA(dfaNode, true);
            default:
                throw new IllegalStateException();
        }
    }

    public synchronized Path getImage(GraphPanelModel.GraphChoice graphChoice, String imgName) {
        return graphViz.toImage(getDotString(graphChoice), imgName);
    }

    public synchronized boolean getTestStringAcceptance(int i) {
        if (i < 0 || i > testString.length()) {
            throw new IllegalArgumentException();
        }
        if (i + 1 < testStringDFANodes.size()) {
            return testStringDFANodes.get(i + 1).isAccept();
        }
        return false;
    }

    public synchronized boolean isRegexSuccess() {
        return regexSuccess;
    }

    public synchronized void setAlphabet(Alphabet alphabet) {
        this.alphabet = alphabet;
    }

    public Alphabet getAlphabet() {
        return alphabet;
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
        generateTestStringLists();
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
