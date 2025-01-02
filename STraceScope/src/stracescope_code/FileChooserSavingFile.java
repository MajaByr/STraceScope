package stracescope_code;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;

public class FileChooserSavingFile extends Application {
    @Override
    public void start(Stage stage) {
        ImageView imgView = new ImageView("UIControls/Save.png");
        imgView.setFitWidth(20);
        imgView.setFitHeight(20);
        Menu file = new Menu("File");
        MenuItem item = new MenuItem("Save", imgView);
        file.getItems().addAll(item);
        //Creating a File chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save");
        fileChooser.getExtensionFilters().addAll(new ExtensionFilter("All Files", "*.*"));
        //Adding action on the menu item
        item.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                //Opening a dialog box
                fileChooser.showSaveDialog(stage);
            }
        });
        //Creating a menu bar and adding menu to it.
        MenuBar menuBar = new MenuBar(file);
        Group root = new Group(menuBar);
        Scene scene = new Scene(root, 595, 355, Color.BEIGE);
        stage.setTitle("File Chooser Example");
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String args[]){
        launch(args);
    }
}