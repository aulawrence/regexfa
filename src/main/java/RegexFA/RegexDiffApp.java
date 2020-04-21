package RegexFA;

import RegexFA.Controller.RegexDiffController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class RegexDiffApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/regexDiffView.fxml"));
        Parent root = loader.load();
        RegexDiffController controller = loader.getController();
        primaryStage.setTitle("RegexDiff");
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
