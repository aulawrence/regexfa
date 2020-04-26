package RegexFA.Parser;

import RegexFA.Alphabet;

import java.util.*;
import java.util.stream.Collectors;

public class RegexTokenizer {
    public static List<Token> tokenize(String string, Alphabet alphabet) throws ParserException {
        List<Token> tokenList = new ArrayList<>();
        TokenEnum.Scope scope = TokenEnum.Scope.Normal;
        Set<Character> digitSet = new HashSet<>();
        for (char ch = '0'; ch <= '9'; ch++) {
            digitSet.add(ch);
        }
        int i = 0;
        while (i < string.length()) {
            int j = i + 2;
            if (j <= string.length()) {
                String tokenCandidate = string.substring(i, j);
                if (TokenEnum.charSetMap.get(scope).contains(tokenCandidate)) {
                    TokenEnum tokenEnum = TokenEnum.fromString(tokenCandidate);
                    tokenList.add(new Token(tokenEnum, i, tokenCandidate));
                    i = j;
                    continue;
                }
            }
            j = i + 1;
            String tokenCandidate = string.substring(i, j);
            if (TokenEnum.charSetMap.get(scope).contains(tokenCandidate)) {
                TokenEnum tokenEnum = TokenEnum.fromString(tokenCandidate);
                tokenList.add(new Token(tokenEnum, i, tokenCandidate));
                if (tokenEnum == TokenEnum.L_CURLY_BRACKET) {
                    scope = TokenEnum.Scope.Quantifier;
                } else if (tokenEnum == TokenEnum.R_CURLY_BRACKET) {
                    scope = TokenEnum.Scope.Normal;
                } else if (tokenEnum == TokenEnum.L_SQUARE_BRACKET) {
                    scope = TokenEnum.Scope.Charset;
                } else if (tokenEnum == TokenEnum.R_SQUARE_BRACKET) {
                    scope = TokenEnum.Scope.Normal;
                }
            } else if (scope == TokenEnum.Scope.Normal) {
                char charCandidate = tokenCandidate.charAt(0);
                if (alphabet.alphabetSet.contains(charCandidate)) {
                    tokenList.add(new Token(TokenEnum.CHAR, i, tokenCandidate));
                } else {
                    throw new ParserException(charCandidate, i);
                }
            } else if (scope == TokenEnum.Scope.Quantifier) {
                char charCandidate = tokenCandidate.charAt(0);
                if (digitSet.contains(charCandidate)) {
                    while (j < string.length() && digitSet.contains(string.charAt(j))) {
                        j += 1;
                    }
                    tokenList.add(new Token(TokenEnum.NUMBER, i, string.substring(i, j)));
                } else {
                    throw new ParserException(charCandidate, i);
                }
            } else {
                char charCandidate = tokenCandidate.charAt(0);
                if (alphabet.alphabetSet.contains(charCandidate)) {
                    tokenList.add(new Token(TokenEnum.CHAR, i, tokenCandidate));
                } else {
                    throw new ParserException(charCandidate, i);
                }
            }
            i = j;
        }
        tokenList.add(new Token(TokenEnum.EOF, i, ""));
        return tokenList;
    }

    public enum TokenEnum {
        CHAR(null, Scope.Normal, true, false),
        DOT(".", Scope.Normal, true, false),
        L_BRACKET("(", Scope.Normal, true, false),
        R_BRACKET(")", Scope.Normal, false, false),
        L_CURLY_BRACKET("{", Scope.Normal, false, true),
        PLUS("+", Scope.Normal, false, true),
        STAR("*", Scope.Normal, false, true),
        QMARK("?", Scope.Normal, false, true),
        OR("|", Scope.Normal, false, false),

        NUMBER(null, Scope.Quantifier, false, false),
        R_CURLY_BRACKET("}", Scope.Quantifier, false, false),
        COMMA(",", Scope.Quantifier, false, false),

        EOF(null, Scope.Normal, false, false),

        L_SQUARE_BRACKET("[", Scope.Normal, true, false),
        R_SQUARE_BRACKET("]", Scope.Charset, false, false),
        NEG("^", Scope.Charset, false, false),
        DASH("-", Scope.Charset, false, false),

//        PPLUS("++", Scope.Normal, false, true),
//        PSTAR("*+", Scope.Normal, false, true),
//        PQMARK("?+", Scope.Normal, false, true),
//
//        LPLUS("+?", Scope.Normal, false, true ),
//        LSTAR("*?", Scope.Normal, false, true ),
//        LQMARK("??", Scope.Normal, false, true ),
        ;
        public static final Map<Scope, Set<String>> charSetMap = Arrays.stream(Scope.values())
                .collect(Collectors.toUnmodifiableMap(
                        scope -> scope,
                        scope -> Arrays.stream(TokenEnum.values())
                                .filter(x -> x.scope == scope && x.string != null)
                                .map(x -> x.string)
                                .collect(Collectors.toUnmodifiableSet())
                ));
        public final String string;
        public final Scope scope;
        public final boolean isAtomBegin;
        public final boolean isQuantifierBegin;

        TokenEnum(String string, Scope scope, boolean isAtomBegin, boolean isQuantifierBegin) {
            this.string = string;
            this.scope = scope;
            this.isAtomBegin = isAtomBegin;
            this.isQuantifierBegin = isQuantifierBegin;
        }

        public static TokenEnum fromString(String s) {
            for (TokenEnum tokenEnum : TokenEnum.values()) {
                if (tokenEnum.string != null && tokenEnum.string.equals(s)) {
                    return tokenEnum;
                }
            }
            return null;
        }

        public enum Scope {
            Normal,
            Quantifier,
            Charset;
        }
    }

    public static class Token {
        public final TokenEnum tokenEnum;
        public final int pos;
        public final String string;

        public Token(TokenEnum tokenEnum, int pos, String string) {
            this.tokenEnum = tokenEnum;
            this.pos = pos;
            this.string = string;
        }

        @Override
        public String toString() {
            return String.format("%s(%s)", tokenEnum, string);
        }
    }
}
