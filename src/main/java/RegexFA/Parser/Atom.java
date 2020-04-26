package RegexFA.Parser;

import RegexFA.Alphabet;
import RegexFA.Graph.NFAGraph;
import RegexFA.Graph.NFANode;

import java.io.PrintStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class Atom {
    public final Alphabet alphabet;
    public final boolean isExpression;
    public final Set<Character> characterSet;
    public final Expression expression;

    private Atom(Alphabet alphabet, Set<Character> characterSet) {
        this.alphabet = alphabet;
        this.isExpression = false;
        this.characterSet = Collections.unmodifiableSet(characterSet);
        this.expression = null;
    }

    private Atom(Alphabet alphabet, Expression expression) {
        this.alphabet = alphabet;
        this.isExpression = true;
        this.characterSet = null;
        this.expression = expression;
    }

    public static Atom seek(Queue<RegexTokenizer.Token> queue, Alphabet alphabet) throws ParserException {
        if (queue.isEmpty() || !queue.peek().tokenEnum.isAtomBegin) return null;
        RegexTokenizer.Token token = queue.poll();
        switch (token.tokenEnum) {
            case DOT:
                return new Atom(alphabet, alphabet.alphabetSet);
            case CHAR:
                return new Atom(alphabet, Collections.singleton(token.string.charAt(0)));
            case L_SQUARE_BRACKET:
                boolean negate = false;
                token = queue.poll();
                Set<Character> characterSet = new HashSet<>();
                if (token == null || token.tokenEnum == RegexTokenizer.TokenEnum.EOF) throw new ParserException("Unexpected EOF.");
                if (token.tokenEnum == RegexTokenizer.TokenEnum.NEG) {
                    negate = true;
                    token = queue.poll();
                    if (token == null || token.tokenEnum == RegexTokenizer.TokenEnum.EOF) throw new ParserException("Unexpected EOF.");
                }
                Integer min = null;
                Integer prev = null;
                if (token.tokenEnum == RegexTokenizer.TokenEnum.R_SQUARE_BRACKET) throw new ParserException(token.string.charAt(0), token.pos, "Unexpected closing square bracket.");
                while (token.tokenEnum != RegexTokenizer.TokenEnum.R_SQUARE_BRACKET) {
                    switch (token.tokenEnum) {
                        case DASH:
                            if (prev == null) throw new ParserException(token.string.charAt(0), token.pos);
                            min = prev;
                            break;
                        case CHAR:
                            if (min == null) {
                                prev = alphabet.invertMap.get(token.string.charAt(0));
                                characterSet.add(token.string.charAt(0));
                            } else {
                                int max = alphabet.invertMap.get(token.string.charAt(0));
                                if (max < min) throw new ParserException(token.string.charAt(0), token.pos, "Character range out of order.");
                                for (int i = min; i <= max; i++) {
                                    characterSet.add(alphabet.alphabetList.get(i));
                                }
                                min = null;
                                prev = null;
                            }
                            break;
                        default:
                            throw new ParserException(token.string.charAt(0), token.pos);
                    }
                    token = queue.poll();
                    if (token == null || token.tokenEnum == RegexTokenizer.TokenEnum.EOF) throw new ParserException("Unexpected EOF. Closing square bracket expected.");
                }
                if (!negate) {
                    return new Atom(alphabet, characterSet);
                } else {
                    Set<Character> resultSet = new HashSet<>(alphabet.alphabetSet);
                    resultSet.removeAll(characterSet);
                    return new Atom(alphabet, Collections.unmodifiableSet(resultSet));
                }
            case L_BRACKET:
                Expression expression = Expression.seek(queue, alphabet);
                token = queue.poll();
                if (token == null || token.tokenEnum == RegexTokenizer.TokenEnum.EOF) throw new ParserException("Unexpected EOF. Closing bracket expected.");
                if (token.tokenEnum != RegexTokenizer.TokenEnum.R_BRACKET) throw new ParserException(token.string.charAt(0), token.pos);
                return new Atom(alphabet, expression);
            default:
                throw new IllegalStateException();
        }
    }

    public void print(PrintStream stream, int depth, int width) {
        stream.println(" ".repeat(width * depth) + "|---" + "Atom");
        if (isExpression) {
            expression.print(stream, depth + 1, width);
        } else {
            stream.println(" ".repeat(width * (depth + 1)) + "|---" + characterSet);
        }
    }

    public NFANode toGraph(NFAGraph graph, NFANode prevNode) {
        if (isExpression){
            return expression.toGraph(graph, prevNode);
        } else {
            NFANode currNode = graph.addNode();
            for (char ch: characterSet){
                graph.addEdge(prevNode, currNode, ch);
            }
            return currNode;
        }
    }
}
