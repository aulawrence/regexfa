package RegexFA.Model;

public class GraphViewModel extends Model {
    private GraphPaneChoice graphPaneChoice;
    private GraphChoice graphFocus;

    public GraphViewModel() {
        graphPaneChoice = GraphPaneChoice.ALL;
        graphFocus = GraphChoice.Graph1;
    }

    public GraphPaneChoice getGraphPaneChoice() {
        return graphPaneChoice;
    }

    public void setGraphPaneChoice(GraphPaneChoice graphPaneChoice) {
        this.graphPaneChoice = graphPaneChoice;
    }

    public GraphChoice getGraphFocus() {
        return graphFocus;
    }

    public void setGraphFocus(GraphChoice graphFocus) {
        this.graphFocus = graphFocus;
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
