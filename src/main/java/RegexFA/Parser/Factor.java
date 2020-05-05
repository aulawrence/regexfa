package RegexFA.Parser;

import RegexFA.Alphabet;
import RegexFA.Graph.NFAGraph;
import RegexFA.Graph.NFANode;

import java.io.PrintStream;
import java.util.Queue;
import java.util.Stack;

public class Factor {
    public final Alphabet alphabet;
    public final Atom atom;
    public final Quantifier quantifier;

    private Factor(Alphabet alphabet, Atom atom, Quantifier quantifier) {
        this.alphabet = alphabet;
        this.atom = atom;
        this.quantifier = quantifier;
    }

    public static Factor seek(Queue<RegexTokenizer.Token> queue, Alphabet alphabet) throws ParserException {
        Atom atom = Atom.seek(queue, alphabet);
        if (atom == null) {
            return null;
        }
        Quantifier quantifier = Quantifier.seek(queue);
        return new Factor(alphabet, atom, quantifier);
    }

    public void print(PrintStream stream, int depth, int width) {
        stream.println(" ".repeat(width * depth) + "|---" + "Factor");
        atom.print(stream, depth + 1, width);
        if (quantifier != null) {
            quantifier.print(stream, depth + 1, width);
        }
    }

    public NFANode toGraph(NFANode prevNode) {
        NFAGraph graph = prevNode.getGraph();
        if (quantifier == null) {
            return atom.toGraph(prevNode);
        }
        if (quantifier.max == null) {
            // max infinity
            NFANode currNode = prevNode;
            for (int i = 0; i < quantifier.min; i++) {
                currNode = atom.toGraph(currNode);
            }
            NFANode newNode = atom.toGraph(currNode);
            graph.addEdge(currNode, newNode, Alphabet.Empty);
            graph.addEdge(newNode, currNode, Alphabet.Empty);
            return newNode;
        } else {
            // add min copies
            NFANode currNode = prevNode;
            for (int i = 0; i < quantifier.min; i++) {
                currNode = atom.toGraph(currNode);
            }
            // add max - min choices
            Stack<NFANode> bypassList = new Stack<>();
            bypassList.add(currNode);
            for (int i = 0; i < quantifier.max - quantifier.min; i++) {
                currNode = atom.toGraph(currNode);
                bypassList.add(currNode);
            }
            NFANode endNode = bypassList.pop();
            for (NFANode bypassNode : bypassList) {
                graph.addEdge(bypassNode, endNode, Alphabet.Empty);
            }
            return endNode;
        }
    }
}
