package RegexFA;

import RegexFA.Controller.RegexFAController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class RegexFAApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/regexFAView.fxml"));
        Parent root = loader.load();
        RegexFAController controller = loader.getController();
        primaryStage.setTitle("RegexFA");
        primaryStage.setOnHidden(event -> controller.shutdown());
        Scene scene = new Scene(root, 1024, 768);
        scene.getStylesheets().add(getClass().getResource("/application.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
