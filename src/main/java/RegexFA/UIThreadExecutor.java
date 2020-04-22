package RegexFA;

import javafx.application.Platform;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;

public class UIThreadExecutor implements Executor {
    @Override
    public void execute(@NotNull Runnable command) {
        Platform.runLater(command);
    }
}
