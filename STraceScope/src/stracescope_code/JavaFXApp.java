package stracescope_code;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import java.io.File;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.FileChooser;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.*;
import javafx.concurrent.*;
import javafx.beans.value.*;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javafx.scene.effect.DropShadow;

import javafx.util.Duration;
import stracescope_code.cam.Frames;
import javafx.application.Application;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;



class P_move
{
    int x, y;

    public P_move()
    {
        x = 10;
        y = 10;
    }
}

class G_task extends Task<P_move>
{
    P_move p_move;

    public G_task()
    {
        this.p_move = new P_move();
    }

    @Override
    protected P_move call() throws Exception
    {
        int i = 0;

        while(true)
        {
            System.out.println("Task's call method");

            p_move.x = 10 + i;
            p_move.y = 10 + i;

            updateValue(null);
            updateValue(p_move);

            System.out.println("i=" + i);
            i++;

            System.out.println("x = " +  p_move.x + "y = " +  p_move.y);

            if(i == 10)
            {
                updateValue(null);
                break;
            }

            try { Thread.sleep(1000);  System.out.println("sleep method");    }
            catch (InterruptedException ex)
            {
                System.out.println("catch method");
                break;
            }
        }

        return p_move;
    }
}

class Game_service extends Service<P_move>
{

    Task t;

    public Game_service()
    {

    }

    protected Task createTask()
    {
        t = new G_task();
        return t;
    }

}

public class JavaFXApp extends Application implements ChangeListener<P_move>
{
    Game_service g_s;
    GraphicsContext gc;
    Canvas canvas;
    boolean real_time_image = false;
    Timeline timeline;
    Frames frames;
    Label coord_x;
    BufferedImage curr_BI;
    BufferedImage edited_BI;
    Label coord_y;
    int result;
    String program_path = "/home/maja/Studia/5 Semestr/JAVA/Mikroskop/STraceScope/STraceScope/STraceScope/";
    String selected_file = "saved_images/mufasa.png";

    FileChooser fileChooser = new FileChooser();
    /*fileChooser.setTitle("Save");
    fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All Files", "*.*"));
*/
    boolean[] editing_settings = {false, false, false, false};

    private static final int FRAME_WIDTH  = 640;
    private static final int FRAME_HEIGHT = 480;

    byte buffer[];
    PixelWriter pixelWriter;
    PixelFormat<ByteBuffer> pixelFormat;

    public static void main(String[] args) {
        launch(args);
    }

    private MenuBar setUpMenu(Stage primaryStage)
    {
        Menu menu1 = new Menu("File");
        MenuItem menuItem1 = new MenuItem("Item 1");
        MenuItem menuItem2 = new MenuItem("Exit");
        menuItem2.setOnAction(e -> {
            System.out.println("Exit Selected");
            exit_dialog();
        });

        menu1.getItems().add(menuItem1);
        menu1.getItems().add(menuItem2);

        MenuBar menuBar = new MenuBar(); //utworzenie menu bar z menu
        menuBar.getMenus().add(menu1);

        primaryStage.setOnCloseRequest(e -> {
            e.consume();
            exit_dialog();
        });
        return menuBar;
    }

    // Tworzenie HBox z labelem do slidera + sliderem
    private HBox createLabeledSlider(String label) {
        Label lbl = new Label(label);
        Slider slider = new Slider();
        slider.setMin(0);
        slider.setMax(100);
        slider.setValue(50);
        HBox box = new HBox(lbl, slider);
        box.setSpacing(10);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    @Override
    public void start(Stage primaryStage) {
        //=====================MENU BAR=================================
        MenuBar menuBar = setUpMenu(primaryStage);

        // =======================CENTER BOX=============================
        BorderPane sub_root = new BorderPane();
        sub_root.setPadding(new Insets(10));

        //Rectangle canvas = new Rectangle(600, 400, Color.LIGHTGRAY);
        canvas     = new Canvas(FRAME_WIDTH, FRAME_HEIGHT);
        gc         = canvas.getGraphicsContext2D();

        Button preview_button = new Button("Show preview (RAW)");

        //---actions on Preview (RAW)
        // dodanie reagowania przycisku na zdefiniowaną akcję
        preview_button.setOnMousePressed(e -> {
            preview_button.setText("Showing RAW");
            editing_settings[3] = true;
            Frames.update_edit_settings(editing_settings);
            update_edited_image();
            plot_edited_image();
        });
        preview_button.setOnMouseEntered(e -> preview_button.setEffect(new DropShadow()));
        preview_button.setOnMouseExited(e -> preview_button.setEffect(null));
        preview_button.setOnMouseReleased(e -> {
            preview_button.setText("Show preview (RAW)");
            editing_settings[3] = false;
            Frames.update_edit_settings(editing_settings);
            update_edited_image();
            plot_edited_image();
        });

        // Center coordinates
        Label text_center_coords = new Label("Center coordinates [x] [y]: ");
        coord_x = new Label(Frames.get_x_c() + "");
        coord_y = new Label(Frames.get_y_c() + "");
        HBox coords_box = new HBox(text_center_coords, coord_x, coord_y);

        HBox under_canva = new HBox(preview_button, coords_box);
        under_canva.setSpacing(10);

        VBox centerBox = new VBox(canvas, under_canva);
        centerBox.setAlignment(Pos.TOP_CENTER);
        centerBox.setSpacing(10);
        sub_root.setCenter(centerBox);

        // =======================RIGHT BOX===================================
        VBox rightBox = new VBox();
        rightBox.setSpacing(10);
        rightBox.setPadding(new Insets(10));

        //------------------BUTTONS RIGHT BOX----------------------------
        Button save_view = new Button("Save View");
        save_view.setOnMousePressed(e -> {
            save_current_view(primaryStage);
        });

        Button load_view_settings = new Button("Load view settings");
        Button export_view_settings = new Button("Export view settings");
        Button open_image = new Button("Open image");
        open_image.setOnMousePressed(e -> {
            select_and_load_image(primaryStage);
        });

        RadioButton denoice = new RadioButton("Denoice");
        RadioButton upscale_ai = new RadioButton("Upscale (AI)");
        CheckBox follow_object = new CheckBox("Follow object");
        Button measure_distance = new Button("Measure distance");
        Label obliczono = new Label("Obliczono [mm]: ");
        Label obliczono_res = new Label("none");

        HBox obliczono_box = new HBox();
        obliczono_box.getChildren().addAll(obliczono, obliczono_res);

        rightBox.getChildren().addAll(
                save_view,
                load_view_settings,
                export_view_settings,
                open_image,
                denoice,
                upscale_ai,
                follow_object,
                measure_distance,
                obliczono_box );
        sub_root.setRight(rightBox);

        // ------------Sterowanie (GridPane na strzałki)-----------------------------------
        GridPane controlGrid = new GridPane();
        controlGrid.setHgap(5);
        controlGrid.setVgap(5);
        controlGrid.setPadding(new Insets(10));

        Button up_button = new Button("^");
        up_button.setOnMousePressed(e -> { Frames.decrement_y_c(); refresh(); });
        Button left_button = new Button("<");
        left_button.setOnMousePressed(e -> { Frames.decrement_x_c(); refresh(); });
        Button right_button = new Button(">");
        right_button.setOnMousePressed(e -> { Frames.increment_x_c(); refresh(); });
        Button down_button = new Button("v");
        down_button.setOnMousePressed(e -> { Frames.increment_y_c(); refresh(); });

        controlGrid.add(up_button, 1, 0);
        controlGrid.add(left_button, 0, 1);
        controlGrid.add(right_button, 2, 1);
        controlGrid.add(down_button, 1, 2);
        rightBox.getChildren().add(new Label("Sterowanie"));
        rightBox.getChildren().add(controlGrid);

        //=======================BOTTOM BOX=============================
        HBox bottomBox = new HBox();
        bottomBox.setSpacing(20);
        bottomBox.setPadding(new Insets(10));

        // Suwaki (Jasność, Kontrast)
        VBox slidersBox = new VBox();
        slidersBox.setSpacing(10);

        //---Tworzenie HBoxów z suwakami---
        // light
        Label lbl_light = new Label("Jasność");
        Slider slider_light = new Slider();
        slider_light.setMin(0);
        slider_light.setMax(100);
        slider_light.setValue(50);
        HBox slider_box_light = new HBox(lbl_light, slider_light);
        slider_box_light.setSpacing(10);
        slider_box_light.setAlignment(Pos.CENTER_LEFT);
        slider_light.valueProperty().addListener(
                new ChangeListener<Number>() {
                    public void changed(ObservableValue <? extends Number >
                                                observable, Number oldValue, Number newValue)
                    {
                        Frames.set_brightness((double) newValue/100);
                        System.out.println("Brightness changed to: " + (double)newValue/100);
                        refresh();
                    }
                });

        //contrast
        Label lbl_contrast = new Label("Kontrast");
        Slider slider_contrast = new Slider();
        slider_contrast.setMin(0);
        slider_contrast.setMax(100);
        slider_contrast.setValue(50);
        HBox slider_box_contrast = new HBox(lbl_contrast, slider_contrast);
        slider_box_contrast.setSpacing(10);
        slider_box_contrast.setAlignment(Pos.CENTER_LEFT);
        slider_contrast.valueProperty().addListener(
                new ChangeListener<Number>() {
                    public void changed(ObservableValue <? extends Number >
                                                observable, Number oldValue, Number newValue)
                    {
                        Frames.set_contrast((double) newValue/100);
                        System.out.println("Contrast changed to: " + (double)newValue/100);
                        refresh();
                    }

                });

        slidersBox.getChildren().addAll(
                slider_box_light,
                slider_box_contrast );

        //-------------------Scale Box--------------------------
        VBox scaleBox = new VBox();
        scaleBox.setSpacing(10);
        TextField scaleField = new TextField("1.00");
        scaleField.setPrefWidth(50);
        EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e)
            {
                String value = scaleField.getText();
                Frames.set_scale(Double.parseDouble(value) );
                refresh();
            }
        };
        scaleField.setOnAction(event);

        Button plus_scale = new Button("+");
        plus_scale.setOnMousePressed(e -> {
            Frames.increment_scale();
            scaleField.setText(Frames.get_scale() + "" );
            refresh();
        });

        Button minus_scale = new Button("-");
        minus_scale.setOnMousePressed(e -> {
            Frames.decrement_scale();
            scaleField.setText(Frames.get_scale() + "" );
            refresh();
        });

        HBox scaleButtons = new HBox(plus_scale, minus_scale);
        scaleButtons.setSpacing(5);
        scaleBox.getChildren().addAll(new Label("Scale"), scaleField, scaleButtons);

        //-----------------Options Box-----------------------
        VBox optionsBox = new VBox();
        optionsBox.setSpacing(10);

        //----choice_box_mode----
        ChoiceBox choice_box_mode = new ChoiceBox();
        choice_box_mode.getItems().add("RAW");
        choice_box_mode.getItems().add("One-channel");
        choice_box_mode.getItems().add("Negative");
        choice_box_mode.setValue("RAW");
        //--Obsługa--
        choice_box_mode.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue ov, Number value, Number new_value)
            {
                for(int i=0; i<2; i++)
                {
                    if( new_value.intValue()-1 == i ) editing_settings[i]=true;
                    else editing_settings[i]=false;
                }

                Frames.update_edit_settings(editing_settings);
                refresh();
            }
        });

        CheckBox show_grid = new CheckBox("Show grid");
        show_grid.selectedProperty().addListener(
                (ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) -> {
                    //text1.setText("Welcome to Tutorilaspoint");
                    editing_settings[2] = show_grid.isSelected();
                    Frames.update_edit_settings(editing_settings);
                    refresh();
                });

        optionsBox.getChildren().addAll(
                choice_box_mode,
                show_grid );

        // Dodanie sekcji dolnej
        bottomBox.getChildren().addAll(slidersBox, scaleBox, optionsBox);
        sub_root.setBottom(bottomBox);

        VBox root = new VBox(menuBar, sub_root);
        // Ustawienie sceny i wyświetlenie
        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setTitle("STraceScope");
        primaryStage.setScene(scene);
        primaryStage.show();

        if( real_time_image ) {
            frames = new Frames();
            //result = frames.open_shm(program_path + "frames");

            timeline = new Timeline(new KeyFrame(Duration.millis(130), e->disp_frame()));
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();
        } else {
            show_file_image(program_path + selected_file);
        }

        //---REST---
        g_s = new Game_service();
        g_s.valueProperty().addListener(this::changed);
        g_s.start();
        primaryStage.show();
    }

    public void changed(ObservableValue<? extends P_move> observable,
                        P_move oldValue,
                        P_move newValue)
    {
        if(newValue != null) System.out.println("changed method called, x = " + newValue.x + "y = " + newValue.y);
    }

    public void refresh()
    {
        update_edited_image();
        plot_edited_image();
        coord_x.setText(Frames.get_x_c() + " ");
        coord_y.setText(" " + Frames.get_y_c());
    }

    public void item_1()
    {
        System.out.println("item 1");
    }

    private void show_file_image(String path)
    {
        load_image_from_file(path);
        update_edited_image();
        plot_edited_image();
    }

    private void load_image_from_file(String path)
    {
        curr_BI = Frames.load_from_file(path);
    }

    private void select_and_load_image(Stage primaryStage)
    {
        File file = fileChooser.showOpenDialog(primaryStage);
        load_image_from_file(file.getAbsolutePath());
        update_edited_image();
        plot_edited_image();
    }

    private void update_edited_image()
    {
        edited_BI = Frames.edit_BI(curr_BI);
    }

    private void plot_edited_image()
    {
        byte[] buffer_temp = Frames.convert_BI_to_bytes(edited_BI);
        pixelWriter = gc.getPixelWriter();
        pixelFormat = PixelFormat.getByteRgbInstance();
        pixelWriter.setPixels(0, 0, FRAME_WIDTH, FRAME_HEIGHT, pixelFormat, buffer_temp, 0, FRAME_WIDTH * 3);
    }

    private void disp_frame()
    {
        pixelWriter = gc.getPixelWriter();
        pixelFormat = PixelFormat.getByteRgbInstance();
        buffer = frames.get_frame();
        curr_BI = Frames.convert_to_BI(buffer);
        update_edited_image();
        plot_edited_image();
    }

    private void save_current_view(Stage primaryStage)
    {
        File file = fileChooser.showSaveDialog(primaryStage);
        FileChooserSavingFile.save_current(edited_BI, file);
    }

    public void exit_dialog()
    {
        System.out.println("exit dialog");

        Alert alert = new Alert(AlertType.CONFIRMATION,
                "Do you really want to exit the program?.",
                ButtonType.YES, ButtonType.NO);

        alert.setResizable(true);
        alert.onShownProperty().addListener(e -> {
            Platform.runLater(() -> alert.setResizable(false));
        });

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.YES)
        {
            Platform.exit();
        }
        else
        {
        }

    }
}