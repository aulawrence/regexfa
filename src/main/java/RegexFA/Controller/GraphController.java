package RegexFA.Controller;

import RegexFA.Model.GraphModel;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.util.StringConverter;

import java.net.URL;
import java.nio.file.Path;
import java.util.ResourceBundle;

public class GraphController extends Controller<GraphModel> {
    @FXML
    private Label label;
    @FXML
    private ImageView imageView;
    @FXML
    private Slider slider;
    private final PublishSubject<Message.EmitBase> observable;
    private final Observer<Message.RecvBase> observer;

    public GraphController() {
        super(new GraphModel());
        observable = PublishSubject.create();
        observer = new Observer<>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(Message.RecvBase recvBase) {
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
        StringConverter<Double> labelConverter = new StringConverter<>() {
            @Override
            public String toString(Double object) {
                return String.format("%.2fx", Math.pow(2, object));
            }

            @Override
            public Double fromString(String string) {
                return Math.log(Double.parseDouble(string)) / Math.log(2);
            }
        };
        slider.setLabelFormatter(labelConverter);
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            model.setZoom(Math.pow(2, newValue.doubleValue()));
            updateImageZoom();
        });
    }


    private void handle(Message.RecvBase recvBase) {
        if (recvBase instanceof Message.RecvImage) {
            handle((Message.RecvImage) recvBase);
        } else if (recvBase instanceof Message.RecvLabel) {
            handle((Message.RecvLabel) recvBase);
        } else if (recvBase instanceof Message.RecvLabelFormat) {
            handle((Message.RecvLabelFormat) recvBase);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private void handle(Message.RecvLabel msg) {
        model.setLabel(msg.label);
        updateLabel();
    }

    private void handle(Message.RecvLabelFormat msg) {
        label.setStyle(msg.style);
        label.setUnderline(msg.underline);
    }

    private void handle(Message.RecvImage msg) {
        model.setImagePath(msg.imagePath);
        updateImage();
    }

    @FXML
    private void onClick_label(MouseEvent mouseEvent) {
        observable.onNext(new Message.EmitClickLabel(mouseEvent.getClickCount()));
    }

    @FXML
    private void onClick_scrollPane(MouseEvent mouseEvent) {
        observable.onNext(new Message.EmitClickImage(mouseEvent.getClickCount()));
    }

    private void updateLabel() {
        label.setText(model.getLabel());
    }

    private void updateImageZoom() {
        Image image = imageView.getImage();
        if (image != null) {
            imageView.setFitHeight(image.getHeight() * model.getZoom());
        }
    }

    private void updateImage() {
        if (model.getImagePath() == null) {
            imageView.setImage(null);
        } else {
            imageView.setImage(new Image(model.getImagePath().toUri().toString()));
            updateImageZoom();
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

        public static final class EmitClickLabel extends EmitBase {
            final int clickCount;

            public EmitClickLabel(int clickCount) {
                this.clickCount = clickCount;
            }
        }

        public static final class EmitClickImage extends EmitBase {
            final int clickCount;

            public EmitClickImage(int clickCount) {
                this.clickCount = clickCount;
            }
        }

        public static abstract class RecvBase {
        }

        public static final class RecvLabel extends RecvBase {
            public final String label;

            public RecvLabel(String label) {
                this.label = label;
            }
        }

        public static final class RecvLabelFormat extends RecvBase {
            public final String style;
            public final boolean underline;

            public RecvLabelFormat(String style, boolean underline) {
                this.style = style;
                this.underline = underline;
            }
        }

        public static final class RecvImage extends RecvBase {
            public final Path imagePath;

            public RecvImage(Path imagePath) {
                this.imagePath = imagePath;
            }
        }
    }
}