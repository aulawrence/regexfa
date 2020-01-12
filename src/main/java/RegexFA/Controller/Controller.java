package RegexFA.Controller;

import RegexFA.Model.Model;
import javafx.fxml.Initializable;

public abstract class Controller<M extends Model> implements Initializable {
    protected final M model;

    protected Controller(M model) {
        this.model = model;
    }
}
