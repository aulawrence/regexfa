package RegexFA.Parser;

public class ParserException extends Exception {
    public ParserException(char ch, int i, String message) {
        super(String.format("'%s' at position %d is not valid. %s", ch, i, message));
    }

    public ParserException(char ch, int i) {
        super(String.format("'%s' at position %d is not valid.", ch, i));
    }

    public ParserException(String message) {
        super(message);
    }
}
