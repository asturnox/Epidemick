package epidemick;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Scanner;

public class EpidemicWindowController implements Initializable {
    private ArrayList<EpidemicObject> infectedArray = new ArrayList<>();
    private ArrayList<EpidemicObject> arrayToInfect = new ArrayList<>();
    private EpidemicObject[][] epidemicArray;
    private int numRows = 10;
    private static final Scanner scanner = new Scanner(System.in);
    private static final Random randomizer = new Random();

    public static int ticks = 0;

    private int recoveryTime;
    private double pValue;
    private double dValue;

    public static int infections = 0;
    public static int deaths = 0;
    public static int recoveries = 0;

    private GraphicsContext gc;
    private double buffer;

    private int stageNumber = 0;

    public static Scene scene;

    public void createMap(int n) {
        numRows = n;
        buffer = (canvas.getWidth()/numRows) * 0.1;
        epidemicArray = new EpidemicObject[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                epidemicArray[i][j] = new EpidemicObject(i,j);
            }
        }
    }

    public void update() {
        arrayToInfect = new ArrayList<>();
        ArrayList<EpidemicObject> removedArray = new ArrayList<>();

        for (EpidemicObject infected : infectedArray) {
            int ticksSinceInfected = ticks - infected.tickWhenInfected;
            int i = infected.position[0];
            int j = infected.position[1];

            if (randomizer.nextDouble() <= dValue) {
                infected.kill();
                deaths++;
                removedArray.add(infected);
            }
            else if (ticksSinceInfected >= recoveryTime) {
                infected.recover();
                recoveries++;
                infections--;
                removedArray.add(infected);
            } else {
                if (i >= 1) {
                    attemptInfect(epidemicArray[i-1][j]);
                } if (i <= epidemicArray.length - 2) {
                    attemptInfect(epidemicArray[i+1][j]);
                } if (j >= 1) {
                    attemptInfect(epidemicArray[i][j-1]);
                } if (j <= epidemicArray.length - 2) {
                    attemptInfect(epidemicArray[i][j+1]);
                }
            }
        }

        for (EpidemicObject recovered : removedArray) {
            infectedArray.remove(recovered);
        }

        infectedArray.addAll(arrayToInfect);

        //Increase ticks for tracking
        ticks++;
    }

    public void attemptInfect(EpidemicObject epidemicObject) {
        float x = randomizer.nextFloat();

        if (x <= pValue && epidemicObject.state.equals("normal")) {
            epidemicObject.infect(ticks);
            arrayToInfect.add(epidemicObject);
            infections++;
        }
    }

    public void render() {
        gc.setFill(Color.GRAY);
        gc.fillRect(0,0, canvas.getWidth(), canvas.getHeight());
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numRows; j++) {
                EpidemicObject o = epidemicArray[i][j];
                String objectState = o.state;
                Color color = switch (objectState) {
                    case "infected" -> Color.RED;
                    case "recovered" -> Color.GREEN;
                    case "deceased" -> Color.BLACK;
                    case "normal" -> Color.WHITE;
                    case "vaccinated" -> Color.CYAN;
                    case "wall" -> Color.GRAY;
                    default -> throw new IllegalArgumentException();
                };

                gc.setFill(color);
                gc.fillRect(i * (canvas.getHeight()/numRows), j * (canvas.getWidth()/numRows), (canvas.getWidth()/numRows) - buffer, (canvas.getHeight()/numRows) - buffer);
            }
        }
    }

    public void setVaccinated() {
        System.out.println("Please click on the squares you wish to vaccinate");
        System.out.println("When you are done, please press enter");
    }

    public void setInfected() {
        System.out.println("Who should be the one, master?");
        int i = Integer.parseInt(scanner.next());
        int j = Integer.parseInt(scanner.next());
        try {
            EpidemicObject o = epidemicArray[i][j];
            o.infect(ticks);
            infectedArray.add(o);

        } catch (IndexOutOfBoundsException e) {
            System.err.println("Invalid index, try again!");
            setInfected();
        }
    }

    public void setPValue() {
        System.out.println("What p-value?");
        float p = Float.parseFloat(scanner.next());
        if (p >= 0 && p <= 1) {
            pValue = p;
        } else {
            System.err.println("Invalid p-value, p-value must be in the interval [0,1]. Please try again:");
            setPValue();
        }
    }

    public void setDValue() {
        System.out.println("What % of people should die?");
        float d = Float.parseFloat(scanner.next());
        if (d >= 0 && d <= 1) {
            dValue = 1 - Math.pow(1-d, 1.0/recoveryTime);
        } else {
            System.err.println("Invalid death percentage, d-value must be in the interval [0,1]. Please try again:");
            setDValue();
        }
    }

    public void setRecoveryTime() {
        System.out.println("How many ticks should it take to recover?");
        int ticks = scanner.nextInt();
        if (ticks >= 1) {
            recoveryTime = ticks;
        } else {
            System.err.println("Invalid recovery time, recovery time must be in the interval [1,inf]. Please try again:");
            setRecoveryTime();
        }
    }

    public void setUp() {
        System.out.println("How many rows, master?");
        createMap(Integer.parseInt(scanner.next()));
        setInfected();
        setPValue();
        setRecoveryTime();
        setDValue();
        scanner.close();
    }

    public void createWall(int squareX, int squareY) {
        epidemicArray[squareX][squareY].makeWall();
        render();
    }

    public void setQuarantineZone() {
        System.out.println("Please click on the squares you wish to wall");
        System.out.println("When you are done, please press enter");
    }

    public void launchStage(int stageNumber) {
        if (stageNumber == 0) {
            setQuarantineZone();
        } else if (stageNumber == 1) {
            launchVisual();
        }
    }

    public void vaccinateSquare(int squareX, int squareY) {
        epidemicArray[squareX][squareY].vaccinate();
        render();
    }

    public void launchVisual() {
        openGraph();
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.1), actionEvent -> {
            update();
            render();
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.playFromStart();
    }

    public void openGraph() {
        Parent root;
        try {
            root = FXMLLoader.load(Main.class.getResource("GraphWindow.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Graph");
            stage.setScene(new Scene(root, 600, 500));
            stage.show();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void setControls() {
        canvas.setFocusTraversable(true);

        canvas.setOnKeyPressed((e) -> {
            String code = e.getCode().toString();
            System.out.println(code);
            System.out.println("ha");

            if (code.equals("ENTER") && stageNumber <= 1) {
                launchStage(stageNumber);
                stageNumber++;
            }
        });

        canvas.setOnMouseClicked((e) -> {
            int x = (int) e.getSceneX();
            int y = (int) e.getSceneY();

            int squareX = (int) (x / (canvas.getWidth() / numRows));
            int squareY = (int) (y / (canvas.getHeight() / numRows));

            if (stageNumber == 0) {
                vaccinateSquare(squareX, squareY);
            } else if (stageNumber == 1) {
                createWall(squareX, squareY);
            }
        });
    }

    @FXML
    Canvas canvas;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        gc = canvas.getGraphicsContext2D();

        setControls();

        System.out.println("How many rows, master?");
        createMap(Integer.parseInt(scanner.next()));
        setInfected();
        setPValue();
        setRecoveryTime();
        setDValue();
        scanner.close();

        render();
        setVaccinated();
    }
}
