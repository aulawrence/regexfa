package RegexFA;

import RegexFA.Controller.DiffViewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class DiffApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/diffView.fxml"));
        Parent root = loader.load();
        DiffViewController controller = loader.getController();
        primaryStage.setTitle("Main");
        primaryStage.setOnHidden(event -> controller.shutdown());
        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/application.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
