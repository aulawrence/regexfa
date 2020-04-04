package RegexFA.Model;

import RegexFA.Alphabet;
import RegexFA.Graph.FAGraph;
import RegexFA.Parser.ParserException;
import RegexFA.Parser.RegexParser;

import java.awt.image.BufferedImage;

public class MainModel extends Model {
    public enum GraphChoice {
        NFA,
        DFA,
        MinDFA
    }

    private FAGraph nfa;
    private FAGraph dfa;
    private FAGraph min_dfa;
    private boolean success;
    private GraphChoice selection;

    public MainModel() {
        success = false;
        selection = GraphChoice.NFA;
    }

    public void generate_graph(String string, Alphabet alphabet) throws ParserException {
        success = false;
        nfa = RegexParser.toGraph(string, alphabet);
        dfa = FAGraph.toDFA(nfa);
        min_dfa = FAGraph.minimize(dfa);
        success = true;
    }

    public String getDotString() {
        switch (selection) {
            case DFA:
                return dfa.toDotString();
            case NFA:
                return nfa.toDotString();
            case MinDFA:
                return min_dfa.toDotString();
            default:
                throw new IllegalStateException();
        }
    }

    public BufferedImage getImage(GraphChoice graphChoice) {
        switch (graphChoice) {
            case DFA:
                return dfa.getImage();
            case NFA:
                return nfa.getImage();
            case MinDFA:
                return min_dfa.getImage();
            default:
                throw new IllegalStateException();
        }
    }

    public GraphChoice getSelection() {
        return selection;
    }

    public void setSelection(GraphChoice selection) {
        this.selection = selection;
    }

    public boolean isSuccess() {
        return success;
    }
}
