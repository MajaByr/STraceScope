package stracescope_code;

import java.nio.ByteBuffer;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.util.Duration;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.util.ArrayList;
import javafx.scene.control.ChoiceBox;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.StackPane;
import javafx.scene.input.MouseEvent;

import stracescope_code.cam.Frames;


public class Cam extends Application
{
    private static final int FRAME_WIDTH  = 640;
    private static final int FRAME_HEIGHT = 480;


    GraphicsContext gc;
    Canvas canvas;
    byte buffer[];
    PixelWriter pixelWriter;
    PixelFormat<ByteBuffer> pixelFormat;
    Frames frames;

    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage)
    {
        int result;

        Timeline timeline;

        frames = new Frames();

        result = frames.open_shm("/frames");

        primaryStage.setTitle("Camera");
        Scene scene;

        Group root = new Group();
        canvas     = new Canvas(1080, 720);
        gc         = canvas.getGraphicsContext2D();


        //---------USTAWIANIE GUI----------

        // Push buttons
        // create a button
        Button b = new Button("button");
        EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e)
            {
                b.setText("_Click");
            }
        };

        b.setOnAction(event);
        // add button
        root.getChildren().add(b);

        // choice boxes
        ChoiceBox choiceBox = new ChoiceBox();
        choiceBox.getItems().add("RAW");
        choiceBox.getItems().add("Negatyw");
        choiceBox.getItems().add("Jednokanalowy");
        choiceBox.setValue("RAW");
        HBox hbox = new HBox(choiceBox);
        root.getChildren().add(choiceBox);

        choiceBox.setOnAction((event_choiceBox) -> {  });

        timeline = new Timeline(new KeyFrame(Duration.millis(130), e->disp_frame()));

        timeline.setCycleCount(Timeline.INDEFINITE);

        timeline.play();

        root.getChildren().add(canvas);
        scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void disp_frame()
    {

        pixelWriter = gc.getPixelWriter();
        pixelFormat = PixelFormat.getByteRgbInstance();


        buffer = frames.get_frame();
        pixelWriter.setPixels(25, 25, FRAME_WIDTH, FRAME_HEIGHT, pixelFormat, buffer, 0, FRAME_WIDTH*3);

    }

}