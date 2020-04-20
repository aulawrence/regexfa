package RegexFA.Model;

public class TextInputModel extends Model {
    private boolean editing;
    private boolean hideResult;
    private String result;
    private String err;

    public TextInputModel() {
        this.editing = true;
        this.hideResult = false;
        this.result = "";
        this.err = "";
    }

    public boolean isEditing() {
        return editing;
    }

    public void setEditing(boolean editing) {
        this.editing = editing;
    }

    public boolean isHideResult() {
        return hideResult;
    }

    public void setHideResult(boolean hideResult) {
        this.hideResult = hideResult;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getErr() {
        return err;
    }

    public void setErr(String err) {
        this.err = err;
    }
}
