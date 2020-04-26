package RegexFA.Parser;

import java.io.PrintStream;
import java.util.Queue;

public class Quantifier {
    public final int min;
    public final Integer max;
    public final Type type;

    public Quantifier(int min, Integer max, Type type) {
        this.min = min;
        this.max = max;
        this.type = type;
    }

    public static Quantifier seek(Queue<RegexTokenizer.Token> queue) throws ParserException {
        if (queue.isEmpty() || !queue.peek().tokenEnum.isQuantifierBegin) return null;
        switch (queue.poll().tokenEnum) {
            case PLUS:
                return new Quantifier(1, null, Type.Greedy);
            case STAR:
                return new Quantifier(0, null, Type.Greedy);
            case QMARK:
                return new Quantifier(0, 1, Type.Greedy);
//            case PPLUS:
//                return new Quantifier(1, null, Type.Possessive);
//            case PSTAR:
//                return new Quantifier(0, null, Type.Possessive);
//            case PQMARK:
//                return new Quantifier(0, 1, Type.Possessive);
//            case LPLUS:
//                return new Quantifier(1, null, Type.Lazy);
//            case LSTAR:
//                return new Quantifier(0, null, Type.Lazy);
//            case LQMARK:
//                return new Quantifier(0, 1, Type.Lazy);
            case L_CURLY_BRACKET:
                RegexTokenizer.Token token = queue.poll();
                if (token == null || token.tokenEnum == RegexTokenizer.TokenEnum.EOF)
                    throw new ParserException("Unexpected EOF. Number expected.");
                if (token.tokenEnum != RegexTokenizer.TokenEnum.NUMBER)
                    throw new ParserException(token.string.charAt(0), token.pos, "Number expected.");
                int min = Integer.parseInt(token.string);
                token = queue.poll();
                if (token == null || token.tokenEnum == RegexTokenizer.TokenEnum.EOF)
                    throw new ParserException("Unexpected EOF. Comma or closing curly bracket expected.");
                if (token.tokenEnum != RegexTokenizer.TokenEnum.COMMA) {
                    if (token.tokenEnum != RegexTokenizer.TokenEnum.R_CURLY_BRACKET)
                        throw new ParserException(token.string.charAt(0), token.pos, "Comma or closing curly bracket expected.");
                    return new Quantifier(min, min, Type.Greedy);
                }
                token = queue.poll();
                if (token == null || token.tokenEnum == RegexTokenizer.TokenEnum.EOF)
                    throw new ParserException("Unexpected EOF.");
                if (token.tokenEnum == RegexTokenizer.TokenEnum.R_CURLY_BRACKET) {
                    return new Quantifier(min, null, Type.Greedy);
                }
                if (token.tokenEnum != RegexTokenizer.TokenEnum.NUMBER)
                    throw new ParserException(token.string.charAt(0), token.pos, "Number expected.");
                int max = Integer.parseInt(token.string);
                if (max < min)
                    throw new ParserException(token.string.charAt(0), token.pos, "Quantifier range out of order.");
                token = queue.poll();
                if (token == null || token.tokenEnum == RegexTokenizer.TokenEnum.EOF)
                    throw new ParserException("Unexpected EOF. Closing curly bracket expected.");
                if (token.tokenEnum != RegexTokenizer.TokenEnum.R_CURLY_BRACKET)
                    throw new ParserException(token.string.charAt(0), token.pos, "Closing curly bracket expected.");
                return new Quantifier(min, max, Type.Greedy);
            default:
                return null;
        }
    }

    @Override
    public String toString() {
        return String.format("Quantifier(%d, %d, %s)", min, max, type);
    }

    public enum Type {
        Greedy,
//            Lazy,
//            Possessive;
    }

    public void print(PrintStream stream, int depth, int width) {
        stream.println(" ".repeat(width * depth) + "|---" + this);
    }
}
