package mod;

import javafx.application.Application;
import javafx.stage.Stage;
import mod.core.Model;
import mod.core.View;

public class App extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {
        Model model = new Model();
        new View(model, stage);
    }
}