package RegexFA.Model;

import java.util.HashMap;

public class GraphViewModel extends Model {
    private GraphPaneChoice graphPaneChoice;
    private GraphChoice dotStringChoice;
    private final HashMap<GraphChoice, Double> zoomHashMap;

    public GraphViewModel() {
        graphPaneChoice = GraphPaneChoice.ALL;
        dotStringChoice = GraphChoice.NFA;
        zoomHashMap = new HashMap<>();
        for (GraphChoice graphChoice : GraphChoice.values()) {
            zoomHashMap.put(graphChoice, 1.0);
        }
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

    public double getZoom(GraphChoice graphChoice) {
        return zoomHashMap.get(graphChoice);
    }

    public void setZoom(GraphChoice graphChoice, double zoom) {
        zoomHashMap.put(graphChoice, zoom);
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
