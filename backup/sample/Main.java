package epidemick;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main extends Application {

    /*
    * Dev Plan:
    * Form an nxn matrix, every n ticks update the matrix.
    * To update the matrix, loop through every infected object and check its adjacent indices on the matrix probabilistically infect.
    * After some time since the object has been infected, the object changes to recovered and cannot be infected anymore, remove object from infected array.
    * Hence for each object we need:
    * 1. State
    * 2. Index
    * 3. TicksSinceInfected
    *
    * For the class we need:
    * 1. An nxn array containing all objects
    * 2. An ArrayList containing all infected objects.
    * 3. A tick refresh rate.
    * 4. A number that controls how probable it is to get infected every tick.
    *
    * For this project, time should be controlled by a TimeLine, since we don't want to be updating every 1/60 seconds*/

    public static int canvasWidth = 1000;
    public static int canvasHeight = 1000;
    public static Canvas canvas = new Canvas(canvasWidth,canvasHeight);
    public static GraphicsContext gc = canvas.getGraphicsContext2D();
    public static Scanner scanner = new Scanner(System.in);
    public static ArrayList<String> keyboardInput = new ArrayList<>();
    public static int[] mouseInput = new int[2];
    public static int stageNumber = 0;

    public static void setVaccinated() {
        System.out.println("Please click on the squares you wish to vaccinate");
        System.out.println("When you are done, please press enter");
    }

    public static void setInfected() {
        System.out.println("Who should be the one, master?");
        int i = Integer.parseInt(scanner.next());
        int j = Integer.parseInt(scanner.next());
        try {
            EpidemicObject o = EpidemicObject.epidemicArray[i][j];
            o.infect();
            EpidemicObject.infectedArray.add(o);

        } catch (IndexOutOfBoundsException e) {
            System.err.println("Invalid index, try again!");
            setInfected();
        }
    }

    public static void setPValue() {
        System.out.println("What p-value?");
        float p = Float.parseFloat(scanner.next());
        if (p >= 0 && p <= 1) {
            EpidemicObject.pValue = p;
        } else {
            System.err.println("Invalid p-value, p-value must be in the interval [0,1]. Please try again:");
            setPValue();
        }
    }

    public static void setDValue() {
        System.out.println("What % of people should die?");
        float d = Float.parseFloat(scanner.next());
        if (d >= 0 && d <= 1) {
            EpidemicObject.dValue = 1 - Math.pow(1-d, 1.0/EpidemicObject.recoveryTime);
        } else {
            System.err.println("Invalid death percentage, d-value must be in the interval [0,1]. Please try again:");
            setDValue();
        }
    }

    public static void setRecoveryTime() {
        System.out.println("How many ticks should it take to recover?");
        int ticks = scanner.nextInt();
        if (ticks >= 1) {
            EpidemicObject.recoveryTime = ticks;
        } else {
            System.err.println("Invalid recovery time, recovery time must be in the interval [1,inf]. Please try again:");
            setRecoveryTime();
        }
    }

    public static void setUp() {
        System.out.println("How many rows, master?");
        EpidemicObject.createMap(Integer.parseInt(scanner.next()));
        setInfected();
        setPValue();
        setRecoveryTime();
        setDValue();
        scanner.close();
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        Group root = new Group();
        root.getChildren().add(canvas);

        primaryStage.setTitle("Epidemick");
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();

        scene.setOnKeyPressed((e) -> {
            String code = e.getCode().toString();
            System.out.println(code);

            if (code.equals("ENTER") && stageNumber <= 1) {
                launchStage(stageNumber);
                stageNumber++;
            }
        });

        scene.setOnKeyReleased((e) -> {
            String code = e.getCode().toString();

            keyboardInput.remove(code);
        });

        scene.setOnMouseClicked((e) -> {
            int x = (int) e.getSceneX();
            int y = (int) e.getSceneY();

            int squareX = x / (canvasWidth / EpidemicObject.numRows);
            int squareY = y / (canvasHeight / EpidemicObject.numRows);

            if (stageNumber == 0) {
                vaccinateSquare(squareX, squareY);
            } else if (stageNumber == 1) {
                createWall(squareX, squareY);
            }
        });

        EpidemicObject.render();

        setVaccinated();
    }

    public static void createWall(int squareX, int squareY) {
        EpidemicObject.epidemicArray[squareX][squareY].makeWall();
        EpidemicObject.render();
    }

    public static void setQuarantineZone() {
        System.out.println("Please click on the squares you wish to wall");
        System.out.println("When you are done, please press enter");
    }

    public static void launchStage(int stageNumber) {
        if (stageNumber == 0) {
            setQuarantineZone();
        } else if (stageNumber == 1) {
            launchVisual();
        }
    }

    public static void vaccinateSquare(int squareX, int squareY) {
        EpidemicObject.epidemicArray[squareX][squareY].vaccinate();
        EpidemicObject.render();
    }

    public static void launchVisual() {
        openGraph();
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.1), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                EpidemicObject.update();
                EpidemicObject.render();
            }
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.playFromStart();
    }

    public static void openGraph() {
        Parent root;
        try {
            root = FXMLLoader.load(Main.class.getResource("GraphWindow.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Graph");
            stage.setScene(new Scene(root, 450, 450));
            stage.show();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void main(String[] args) {
        setUp();
        launch(args);
    }
}
