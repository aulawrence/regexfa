package RegexFA.Model;

public class GraphViewModel extends Model {
    private GraphPaneChoice graphPaneChoice;
    private GraphChoice dotStringChoice;

    public GraphViewModel() {
        graphPaneChoice = GraphPaneChoice.ALL;
        dotStringChoice = GraphChoice.Graph1;
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
        Pane1,
        Pane2,
        Pane3;

        public static GraphPaneChoice fromGraphChoice(GraphChoice graphChoice) {
            switch (graphChoice) {
                case Graph1:
                    return Pane1;
                case Graph2:
                    return Pane2;
                case Graph3:
                    return Pane3;
            }
            throw new IllegalStateException();
        }
    }

    public enum GraphChoice {
        Graph1,
        Graph2,
        Graph3
    }
}
