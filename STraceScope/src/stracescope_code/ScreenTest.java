package stracescope_code;

import java.util.List;

import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class ScreenTest extends Application {

    @Override
    public void start(Stage primaryStage) {
        final Screen primaryScreen = Screen.getPrimary();
        final List<Screen> allScreens = Screen.getScreens();
        Screen secondaryScreen;
        if (allScreens.size() <= 1) {
            System.out.println("Only one screen");
            secondaryScreen = primaryScreen;
        } else {
            // UPDATED:
            if (allScreens.get(0).equals(primaryScreen)) {
                secondaryScreen = allScreens.get(1);
            } else {
                secondaryScreen = allScreens.get(0);
            }
        }
        configureAndShowStage("Primary", primaryStage, primaryScreen);

        final Stage secondaryStage = new Stage();
        configureAndShowStage("Secondary", secondaryStage, secondaryScreen);

    }

    private void configureAndShowStage(final String name, final Stage stage, final Screen screen) {
        StackPane root = new StackPane();
        root.getChildren().add(new Label(name + " stage"));
        Scene scene = new Scene(root, 300, 200);
        stage.setScene(scene);
        stage.setTitle(name);

        Rectangle2D bounds = screen.getBounds();
        System.out.println(bounds);
        stage.setX(bounds.getMinX() + (bounds.getWidth() - 300) / 2);
        stage.setY(bounds.getMinY() + (bounds.getHeight() - 200) / 2);
        stage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}