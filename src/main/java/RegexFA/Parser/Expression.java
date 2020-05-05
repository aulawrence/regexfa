package RegexFA.Parser;

import RegexFA.Alphabet;
import RegexFA.Graph.NFAGraph;
import RegexFA.Graph.NFANode;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

public class Expression {
    public final Alphabet alphabet;
    public final List<List<Factor>> termList;

    private Expression(Alphabet alphabet, List<List<Factor>> termList) {
        this.alphabet = alphabet;
        this.termList = Collections.unmodifiableList(termList);
    }

    public static Expression seek(Queue<RegexTokenizer.Token> queue, Alphabet alphabet) throws ParserException {
        List<List<Factor>> termList = new ArrayList<>();
        while (true) {
            List<Factor> term = new ArrayList<>();
            Factor factor = Factor.seek(queue, alphabet);
            while (factor != null) {
                term.add(factor);
                factor = Factor.seek(queue, alphabet);
            }
            if (!term.isEmpty()) {
                termList.add(Collections.unmodifiableList(term));
                if (queue.isEmpty()) throw new IllegalStateException();
                if (queue.peek().tokenEnum == RegexTokenizer.TokenEnum.OR) {
                    queue.poll();
                    continue;
                }
            }
            break;
        }
        return new Expression(alphabet, termList);
    }

    public void print(PrintStream stream, int depth, int width) {
        stream.println(" ".repeat(width * depth) + "|---" + "Expression");
        for (List<Factor> term : termList) {
            stream.println(" ".repeat(width * (depth + 1)) + "|---" + "Term");
            for (Factor factor : term) {
                factor.print(stream, depth + 2, width);
            }
        }
    }

    public NFANode toGraph(NFANode prevNode) {
        NFAGraph graph = prevNode.getGraph();
        if (termList.isEmpty()) {
            return prevNode;
        }
        List<NFANode> nodeList = new ArrayList<>();
        for (List<Factor> term : termList) {
            NFANode attachNode = graph.addNode();
            graph.addEdge(prevNode, attachNode, Alphabet.Empty);
            NFANode currNode = attachNode;
            for (Factor factor : term) {
                currNode = factor.toGraph(currNode);
            }
            nodeList.add(currNode);
        }
        NFANode termNode = graph.addNode();
        for (NFANode node2 : nodeList) {
            graph.addEdge(node2, termNode, Alphabet.Empty);
        }
        return termNode;
    }
}
