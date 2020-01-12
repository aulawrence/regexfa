package Test4.Model;

import Test4.Alphabet;
import Test4.Graph.FAGraph;
import Test4.Parser.ParserException;
import Test4.Parser.RegexParser;

public class MainModel extends Model {
    public FAGraph getGraph(String string, Alphabet alphabet) throws ParserException {
        return RegexParser.toGraph(string, alphabet);
    }
}
