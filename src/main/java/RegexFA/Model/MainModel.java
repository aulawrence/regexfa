package RegexFA.Model;

public class MainModel extends Model {
    private ViewChoice viewChoice;

    public MainModel() {
        viewChoice = ViewChoice.RegexFA;
    }

    public ViewChoice getViewChoice() {
        return viewChoice;
    }

    public void setViewChoice(ViewChoice viewChoice) {
        this.viewChoice = viewChoice;
    }

    public enum ViewChoice {
        RegexFA,
        RegexDiff;
    }
}
