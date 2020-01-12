package RegexFA.Model;

import RegexFA.Alphabet;
import RegexFA.Graph.FAGraph;
import RegexFA.Parser.ParserException;
import RegexFA.Parser.RegexParser;

public class MainModel extends Model {
    public FAGraph getGraph(String string, Alphabet alphabet) throws ParserException {
        return RegexParser.toGraph(string, alphabet);
    }
}
