package mod.core;

import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

import java.io.IOException;

public class View {
    public View(Model model, Stage stage) {
        try {
            FXMLLoader mainViewLoader = new FXMLLoader(View.class.getResource("/mod/view/view.fxml"));

            stage.setScene(mainViewLoader.load());
            Controller controller = mainViewLoader.getController();
            controller.init(model);

            // Resize the window dynamically.
            stage.widthProperty().addListener(e -> controller.onResize(stage));
            stage.heightProperty().addListener(e -> controller.onResize(stage));

            stage.setTitle("Map Of Denmark!");
            stage.show();
        } catch (IOException err) {
            throw new RuntimeException(err);
        }
    }
}