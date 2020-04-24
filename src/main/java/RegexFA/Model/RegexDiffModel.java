package RegexFA.Model;

import RegexFA.Alphabet;
import RegexFA.Graph.DFA;
import RegexFA.Graph.DFAGraph;
import RegexFA.Graph.DFANode;
import RegexFA.Graph.GraphViz;
import RegexFA.Parser.ParserException;
import RegexFA.Parser.RegexParser;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Optional;

public class RegexDiffModel extends Model implements Closeable {

    private final GraphViz graphViz = new GraphViz();
    private final ArrayList<DFANode> testStringNodes1 = new ArrayList<>();
    private final ArrayList<DFANode> testStringNodes2 = new ArrayList<>();
    private final ArrayList<DFANode> testStringNodesXor = new ArrayList<>();
    private Alphabet alphabet;
    private String regex1 = "";
    private String regex2 = "";
    private String testString = "";
    private boolean regex1Success = false;
    private String regex1ErrMsg = "";
    private boolean regex2Success = false;
    private String regex2ErrMsg = "";
    private boolean testStringSuccess = false;
    private String testStringErrorMsg = "";
    private int testStringPos = -1;
    private boolean graphSuccess = false;
    private String graphMessage = "";
    private DFAGraph min_dfa1 = null;
    private DFAGraph min_dfa2 = null;
    private DFAGraph min_dfaXor = null;

    public RegexDiffModel() throws IOException {
    }

    private void validateRegex1() {
        try {
            regex1Success = false;
            RegexParser.verify(regex1, alphabet);
            regex1Success = true;
            regex1ErrMsg = "";
            if (regex2Success) {
                generateGraphs();
            }
        } catch (ParserException e) {
            regex1ErrMsg = e.getMessage();
        }
    }

    private void validateRegex2() {
        try {
            regex2Success = false;
            RegexParser.verify(regex2, alphabet);
            regex2Success = true;
            regex2ErrMsg = "";
            if (regex1Success) {
                generateGraphs();
            }
        } catch (ParserException e) {
            regex2ErrMsg = e.getMessage();
        }
    }

    private void validateTestString() {
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

    public void generateGraphs() {
        try {
            graphSuccess = false;
            min_dfa1 = RegexParser.toGraph(regex1, alphabet).toDFA().minimize().clearNodeSet();
            min_dfa2 = RegexParser.toGraph(regex2, alphabet).toDFA().minimize().clearNodeSet();
            min_dfaXor = DFA.xor(min_dfa1, min_dfa2).toDFA().minimize().clearNodeSet();
            graphSuccess = true;
            Optional<String> discrepancy = DFA.getFirstAcceptString(min_dfaXor);
            if (discrepancy.isEmpty()) {
                graphMessage = String.format("Patterns %s and %s are equivalent.%n", regex1, regex2);
            } else {
                graphMessage = String.format("Patterns %s and %s are not equivalent:%n", regex1, regex2) +
                        String.format("|- String    : %s%n", discrepancy.get().equals("") ? Alphabet.Empty : discrepancy.get()) +
                        String.format("|- Pattern 1 : %s%n", min_dfa1.acceptsString(discrepancy.get()) ? "accepts" : "rejects") +
                        String.format("|- Pattern 2 : %s", min_dfa2.acceptsString(discrepancy.get()) ? "accepts" : "rejects");
            }
        } catch (ParserException e) {
            graphMessage = e.getMessage();
        }
    }

    private void generateTestStringLists() {
        if (graphSuccess && testStringSuccess) {
            testStringNodes1.clear();
            DFANode curr = min_dfa1.getRootNode();
            for (int i = 0; i < testString.length() && curr != null; i++) {
                testStringNodes1.add(curr);
                curr = min_dfa1.moveFromNode(curr, testString.charAt(i));
            }
            if (curr != null) {
                testStringNodes1.add(curr);
            }
            testStringNodes2.clear();
            curr = min_dfa2.getRootNode();
            for (int i = 0; i < testString.length() && curr != null; i++) {
                testStringNodes2.add(curr);
                curr = min_dfa2.moveFromNode(curr, testString.charAt(i));
            }
            if (curr != null) {
                testStringNodes2.add(curr);
            }
            testStringNodesXor.clear();
            curr = min_dfaXor.getRootNode();
            for (int i = 0; i < testString.length() && curr != null; i++) {
                testStringNodesXor.add(curr);
                curr = min_dfaXor.moveFromNode(curr, testString.charAt(i));
            }
            if (curr != null) {
                testStringNodesXor.add(curr);
            }
        } else {
            testStringNodes1.clear();
            testStringNodes2.clear();
            testStringNodesXor.clear();
        }
    }

    private String getDotString(GraphPanelModel.GraphChoice graphChoice) {
        DFANode dfaNode = null;
        switch (graphChoice) {
            case Graph1:
                if (testStringPos + 1 < testStringNodes1.size()) {
                    dfaNode = testStringNodes1.get(testStringPos + 1);
                }
                return min_dfa1.toDotString_colorDFA(dfaNode, true);
            case Graph2:
                if (testStringPos + 1 < testStringNodes2.size()) {
                    dfaNode = testStringNodes2.get(testStringPos + 1);
                }
                return min_dfa2.toDotString_colorDFA(dfaNode, true);
            case Graph3:
                if (testStringPos + 1 < testStringNodesXor.size()) {
                    dfaNode = testStringNodesXor.get(testStringPos + 1);
                }
                return min_dfaXor.toDotString_colorDFA(dfaNode, true);
            default:
                throw new IllegalStateException();
        }
    }

    public Path getImage(GraphPanelModel.GraphChoice graphChoice, String imgName) {
        return graphViz.toImage(getDotString(graphChoice), imgName);
    }


    public Alphabet getAlphabet() {
        return alphabet;
    }

    public void setAlphabet(Alphabet alphabet) {
        this.alphabet = alphabet;
    }

    public String getRegex1() {
        return regex1;
    }

    public void setRegex1(String regex1) {
        this.regex1 = regex1;
        validateRegex1();
    }

    public String getRegex2() {
        return regex2;
    }

    public void setRegex2(String regex2) {
        this.regex2 = regex2;
        validateRegex2();
    }

    public String getTestString() {
        return testString;
    }

    public void setTestString(String testString) {
        this.testString = testString;
        validateTestString();
        generateTestStringLists();
    }

    public boolean isRegex1Success() {
        return regex1Success;
    }

    public String getRegex1ErrMsg() {
        return regex1ErrMsg;
    }

    public boolean isRegex2Success() {
        return regex2Success;
    }

    public String getRegex2ErrMsg() {
        return regex2ErrMsg;
    }

    public boolean isTestStringSuccess() {
        return testStringSuccess;
    }

    public String getTestStringErrorMsg() {
        return testStringErrorMsg;
    }

    public int getTestStringPos() {
        return testStringPos;
    }

    public void setTestStringPos(int testStringPos) {
        this.testStringPos = testStringPos;
    }

    public boolean isGraphSuccess() {
        return graphSuccess;
    }

    public String getGraphMessage() {
        return graphMessage;
    }

    @Override
    public void close() {
        graphViz.close();
    }
}
