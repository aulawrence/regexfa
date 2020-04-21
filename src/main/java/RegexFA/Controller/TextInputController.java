package RegexFA.Controller;

import RegexFA.Model.TextInputModel;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.net.URL;
import java.util.ResourceBundle;

public class TextInputController extends Controller<TextInputModel> {

    @FXML
    private Label label;
    @FXML
    private TextFlow textFlow;
    @FXML
    private Text text_result;
    @FXML
    private TextField textField;
    @FXML
    private Text text_err;
    @FXML
    private Button button;

    private final PublishSubject<Message.EmitBase> observable;
    private final Observer<Message.RecvBase> observer;

    public TextInputController() {
        super(new TextInputModel());
        observable = PublishSubject.create();
        observer = new Observer<Message.RecvBase>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(Message.@NonNull RecvBase recvBase) {
                handle(recvBase);
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        };
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    private void onKeyReleased_textFlow(KeyEvent keyEvent) {
        switch (keyEvent.getCode()) {
            case ENTER:
                toggleEditing(1);
                break;
        }
    }


    @FXML
    private void onKeyReleased_textField(KeyEvent keyEvent) {
        switch (keyEvent.getCode()) {
            case ENTER:
                toggleEditing(1);
                break;
        }
    }

    @FXML
    private void onKeyReleased_button(KeyEvent keyEvent) {
        switch (keyEvent.getCode()) {
            case SPACE:
            case ENTER:
                toggleEditing(1);
                break;
        }
    }

    @FXML
    private void onClick_button(MouseEvent mouseEvent) {
        toggleEditing(1);
    }

    private void toggleEditing(int count) {
        if (count > 0) {
            if (!model.isEditing()) {
                model.setEditing(true);
                observable.onNext(new Message.EmitStartEditing(count - 1));
                updateView();
                textField.requestFocus();
            } else {
                observable.onNext(new Message.EmitSubmit(textField.getText(), count - 1));
            }
        }
    }

    private void handle(Message.RecvBase recvBase) {
        if (recvBase instanceof Message.RecvResult) {
            handle((Message.RecvResult) recvBase);
        } else if (recvBase instanceof Message.RecvLabel) {
            handle((Message.RecvLabel) recvBase);
        } else if (recvBase instanceof Message.RecvHideResult) {
            handle((Message.RecvHideResult) recvBase);
        } else if (recvBase instanceof Message.RecvToggle) {
            handle((Message.RecvToggle) recvBase);
        } else if (recvBase instanceof Message.RecvEnabled) {
            handle((Message.RecvEnabled) recvBase);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private void handle(Message.RecvResult msg) {
        if (msg.isSuccess) {
            model.setEditing(false);
            model.setResult(msg.resultOrError);
            model.setErr("");
        } else {
            model.setEditing(true);
            model.setErr(msg.resultOrError);
        }
        updateView();
    }

    private void handle(Message.RecvToggle msg) {
        toggleEditing(msg.count);
    }

    private void handle(Message.RecvLabel msg) {
        label.setText(msg.label);
    }

    private void handle(Message.RecvHideResult msg) {
        model.setHideResult(msg.hideResult);
        updateView();
    }

    private void handle(Message.RecvEnabled msg) {
        button.setDisable(!msg.isEnabled);
        textField.setDisable(!msg.isEnabled);
        textFlow.setDisable(!msg.isEnabled);
    }

    private void updateView() {
        if (model.isEditing()) {
            button.setText("Ok");
            text_err.setText(model.getErr());
            textField.setVisible(true);
            textFlow.setVisible(false);
        } else {
            button.setText("Edit");
            text_err.setText(model.getErr());
            textField.setText(model.getResult());
            textField.setVisible(false);
            if (model.isHideResult()) {
                textFlow.setVisible(false);
                text_result.setText("");
            } else {
                textFlow.setVisible(true);
                text_result.setText(model.getResult());
            }
        }
    }

    public Observable<Message.EmitBase> getObservable() {
        return observable;
    }

    public Observer<Message.RecvBase> getObserver() {
        return observer;
    }

    public static final class Message {
        public static abstract class EmitBase {
        }

        public static final class EmitSubmit extends EmitBase {
            public final String string;
            public final int count;

            public EmitSubmit(String string, int count) {
                this.string = string;
                this.count = count;
            }
        }

        public static final class EmitStartEditing extends EmitBase {
            public final int count;

            public EmitStartEditing(int count) {
                this.count = count;
            }
        }

        public static abstract class RecvBase {
        }

        public static final class RecvResult extends RecvBase {
            public final boolean isSuccess;
            public final String resultOrError;

            public RecvResult(boolean isSuccess, String resultOrError) {
                this.isSuccess = isSuccess;
                this.resultOrError = resultOrError;
            }
        }

        public static final class RecvLabel extends RecvBase {
            public final String label;

            public RecvLabel(String label) {
                this.label = label;
            }
        }

        public static final class RecvHideResult extends RecvBase {
            public final boolean hideResult;

            public RecvHideResult(boolean hideResult) {
                this.hideResult = hideResult;
            }
        }

        public static final class RecvToggle extends RecvBase {
            private final int count;

            public RecvToggle(int count) {
                this.count = count;
            }
        }

        public static final class RecvEnabled extends RecvBase {
            private final boolean isEnabled;

            public RecvEnabled(boolean isEnabled) {
                this.isEnabled = isEnabled;
            }
        }
    }
}
