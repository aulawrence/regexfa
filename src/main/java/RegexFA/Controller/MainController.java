package RegexFA.Controller;

import RegexFA.Model.MainModel;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController extends Controller<MainModel> {
    @FXML
    private MenuItem menuItem_quit;
    @FXML
    private MenuItem menuItem_regexFA;
    @FXML
    private MenuItem menuItem_regexDiff;
    @FXML
    private MenuItem menuItem_example;
    @FXML
    private AnchorPane anchorPane;

    private RegexFAController regexFAController;
    private RegexDiffController regexDiffController;

    public MainController() {
        super(new MainModel());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadView();
    }

    @FXML
    private void onAction_menuItem(ActionEvent actionEvent) {
        if (actionEvent.getSource() instanceof MenuItem) {
            MenuItem source = (MenuItem) actionEvent.getSource();
            if (source == menuItem_quit) {
                Platform.exit();
            } else if (source == menuItem_regexFA) {
                MainModel.ViewChoice orig = model.getViewChoice();
                if (orig != MainModel.ViewChoice.RegexFA) {
                    model.setViewChoice(MainModel.ViewChoice.RegexFA);
                    loadView();
                }
            } else if (source == menuItem_regexDiff) {
                MainModel.ViewChoice orig = model.getViewChoice();
                if (orig != MainModel.ViewChoice.RegexDiff) {
                    model.setViewChoice(MainModel.ViewChoice.RegexDiff);
                    loadView();
                }
            } else if (source == menuItem_example) {
                loadExample();
            } else {
                throw new IllegalArgumentException();
            }
        }
    }

    private void loadView() {
        shutdownChildControllers();
        anchorPane.getChildren().clear();
        FXMLLoader loader;
        Node node;
        try {
            switch (model.getViewChoice()) {
                case RegexFA:
                    loader = new FXMLLoader(getClass().getResource("/regexFAView.fxml"));
                    node = loader.load();
                    regexFAController = loader.getController();
                    break;
                case RegexDiff:
                    loader = new FXMLLoader(getClass().getResource("/regexDiffView.fxml"));
                    node = loader.load();
                    regexDiffController = loader.getController();
                    break;
                default:
                    throw new IllegalStateException();
            }
        } catch (IOException e) {
            node = new Text(String.format("An error occured: %s", e.getMessage()));
        }
        AnchorPane.setTopAnchor(node, 0.);
        AnchorPane.setLeftAnchor(node, 0.);
        AnchorPane.setRightAnchor(node, 0.);
        AnchorPane.setBottomAnchor(node, 0.);
        anchorPane.getChildren().add(node);
    }

    private void loadExample() {
        switch (model.getViewChoice()) {
            case RegexFA:
                if (regexFAController != null) {
                    regexFAController.loadExample();
                }
                break;
            case RegexDiff:
                if (regexDiffController != null) {
                    regexDiffController.loadExample();
                }
                break;
            default:
                throw new IllegalStateException();
        }
    }

    private void shutdownChildControllers() {
        if (regexFAController != null) {
            regexFAController.shutdown();
            regexFAController = null;
        }
        if (regexDiffController != null) {
            regexDiffController.shutdown();
            regexDiffController = null;
        }
    }

    public void shutdown() {
        shutdownChildControllers();
    }
}