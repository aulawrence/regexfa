package RegexFA.Model;

public class GraphViewModel extends Model {
    private GraphPaneChoice graphPaneChoice;
    private GraphChoice dotStringChoice;

    public GraphViewModel() {
        graphPaneChoice = GraphPaneChoice.ALL;
        dotStringChoice = GraphChoice.NFA;
    }

    public GraphPaneChoice getGraphPaneChoice() {
        return graphPaneChoice;
    }

    public void setGraphPaneChoice(GraphPaneChoice graphPaneChoice) {
        this.graphPaneChoice = graphPaneChoice;
    }

    public GraphChoice getDotStringChoice() {
        return dotStringChoice;
    }

    public void setDotStringChoice(GraphChoice dotStringChoice) {
        this.dotStringChoice = dotStringChoice;
    }

    public enum GraphPaneChoice {
        ALL,
        NFA,
        DFA,
        MinDFA
    }

    public enum GraphChoice {
        NFA,
        DFA,
        MinDFA
    }
}
