package RegexFA.Parser;

import RegexFA.Alphabet;
import RegexFA.Graph.DFAGraph;
import RegexFA.Graph.GraphViz;
import RegexFA.Graph.NFAGraph;
import RegexFA.Graph.NFANode;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public class RegexParser {

    public static void verify(String pattern, Alphabet alphabet) throws ParserException {
        List<RegexTokenizer.Token> tokenList = RegexTokenizer.tokenize(pattern, alphabet);
        parse(tokenList, alphabet);
    }

    private static Expression parse(List<RegexTokenizer.Token> tokenList, Alphabet alphabet) throws ParserException {
        Queue<RegexTokenizer.Token> queue = new ArrayDeque<>(tokenList);
        Expression expression = Expression.seek(queue, alphabet);
        if (queue.isEmpty()) throw new IllegalStateException();
        RegexTokenizer.Token token = queue.poll();
        if (token.tokenEnum != RegexTokenizer.TokenEnum.EOF)
            throw new ParserException(token.string.charAt(0), token.pos);
        return expression;
    }

    public static NFAGraph toGraph(String pattern, Alphabet alphabet) throws ParserException {
        List<RegexTokenizer.Token> tokenList = RegexTokenizer.tokenize(pattern, alphabet);
        Expression expression = parse(tokenList, alphabet);
        NFAGraph graph = new NFAGraph(alphabet);
        NFANode rootNode = graph.addNode();
        graph.setRootNode(rootNode);
        if (expression != null) {
            graph.setTerminalNode(expression.toGraph(graph, rootNode));
        } else {
            graph.setTerminalNode(rootNode);
        }
        return graph;
    }

    public static void main(String[] args) {
        String pattern1 = "(0|1(01*0)*1){2,5}";
        Alphabet alphabet = Alphabet.Binary;
        try {
            GraphViz graphViz = new GraphViz(Paths.get("parser_img"));
            NFAGraph g1 = toGraph(pattern1, alphabet);
            graphViz.toImage(g1.toDotString(), "g1");
            DFAGraph h1 = g1.toDFA();
            DFAGraph i1 = h1.minimize();
            graphViz.toImage(h1.toDotString(), "h1");
            graphViz.toImage(i1.toDotString(), "i1");
        } catch (ParserException | IOException e) {
            e.printStackTrace();
        }
    }
}
