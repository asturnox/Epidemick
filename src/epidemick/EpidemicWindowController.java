package epidemick;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Main controller for the epidemic window.
 * Updates/renders epidemic state and starts main application loop.
 */
public class EpidemicWindowController implements Initializable {
    private final ArrayList<EpidemicObject> infectedArray = new ArrayList<>();
    private final ArrayList<EpidemicObject> arrayToInfect = new ArrayList<>();
    private EpidemicObject[][] epidemicArray;
    private int numRows;
    private static final Scanner scanner = new Scanner(System.in);
    private static final Random randomizer = new Random();
    /**
     * Delay between each frame, in ms
     */
    public static int delay = 100;

    /**
     * Number of ticks that have passed since start of epidemic: needed for determining recoveries
     */
    public static int ticks = 0;

    /**
     * Epidemic parameters set by user
     */
    private int recoveryTime;
    private double pValue;
    private double dValue;

    /**
     * Epidemic statistics
     */
    public static int infections = 0;
    public static int deaths = 0;
    public static int recoveries = 0;

    private GraphicsContext gc;

    /**
     * Buffer between squares
     */
    private double buffer;

    /**
     * Stage of application
     */
    private int stageNumber = 0;

    /**
     * Initializes the epidemicArray map, a 2D-square array made up of epidemicObject squares.
     *
     * @param n length/width of map
     */
    private void createMap(int n) {
        numRows = n;
        buffer = (canvas.getWidth() / numRows) * 0.1;
        epidemicArray = new EpidemicObject[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                epidemicArray[i][j] = new EpidemicObject(i, j);
            }
        }
    }

    /**
     * Updates the state of all epidemicObject squares as necessary.
     * Using epidemic parameters: infects new squares, kills infected squares and recovers infected squares.
     */
    private void update() {
        arrayToInfect.clear();
        ArrayList<EpidemicObject> removedArray = new ArrayList<>();

        for (EpidemicObject infected : infectedArray) {
            int ticksSinceInfected = ticks - infected.tickWhenInfected; // Used for determining recovery
            int i = infected.position[0];
            int j = infected.position[1];

            if (randomizer.nextDouble() <= dValue) {
                infected.kill();
                deaths++;
                removedArray.add(infected);
            } else if (ticksSinceInfected >= recoveryTime) {
                infected.recover();
                recoveries++;
                infections--;
                removedArray.add(infected);
            } else {    // check all four directions from square, chance to infect
                if (i >= 1) {
                    attemptInfect(epidemicArray[i - 1][j]);
                }
                if (i <= epidemicArray.length - 2) {
                    attemptInfect(epidemicArray[i + 1][j]);
                }
                if (j >= 1) {
                    attemptInfect(epidemicArray[i][j - 1]);
                }
                if (j <= epidemicArray.length - 2) {
                    attemptInfect(epidemicArray[i][j + 1]);
                }
            }
        }

        for (EpidemicObject removed : removedArray) {
            infectedArray.remove(removed);
        }

        infectedArray.addAll(arrayToInfect);

        //Increase ticks for tracking
        ticks++;
    }

    /**
     * Infects a given epidemicObject square probabilistically.
     *
     * @param epidemicObject square to be infected
     */
    private void attemptInfect(EpidemicObject epidemicObject) {
        float x = randomizer.nextFloat();

        if (x <= pValue && epidemicObject.state.equals("normal")) {
            epidemicObject.infect(ticks);
            arrayToInfect.add(epidemicObject);
            infections++;
        }
    }

    /**
     * Renders the grid with the appropriate colour of each square.
     */
    private void render() {
        gc.setFill(Color.GRAY);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
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
                    default -> throw new IllegalStateException();
                };

                gc.setFill(color);
                gc.fillRect(i * (canvas.getHeight() / numRows), j * (canvas.getWidth() / numRows), (canvas.getWidth() / numRows) - buffer, (canvas.getHeight() / numRows) - buffer);
            }
        }
    }

    private void setVaccinated() {
        System.out.println("Please click on the squares you wish to vaccinate");
        System.out.println("When you are done, please press enter");
    }

    private void setInfected() {
        System.out.println("Who should be the one?");
        int i = Integer.parseInt(scanner.next());
        int j = Integer.parseInt(scanner.next());
        try {
            EpidemicObject o = epidemicArray[i][j];
            o.infect(ticks);
            infectedArray.add(o);

        } catch (IndexOutOfBoundsException e) {
            System.err.println("Invalid index, index must be in the interval [0," + (numRows - 1) + "], please try again!");
            setInfected();
        }
    }

    private void setPValue() {
        System.out.println("What % of people should be infected?");
        double p = Double.parseDouble(scanner.next());
        if (p >= 0 && p <= 100) {
            pValue = p / 100.0;
        } else {
            System.err.println("Invalid p-value, p-value must be in the interval [0,100]. Please try again:");
            setPValue();
        }
    }

    private void setDValue() {
        System.out.println("What % of people should die?");
        double d = Double.parseDouble(scanner.next()) / 100.0;
        if (d >= 0 && d <= 1) {
            dValue = 1 - Math.pow(1 - d, 1.0 / recoveryTime);
        } else {
            System.err.println("Invalid death percentage, d-value must be in the interval [0,100]. Please try again:");
            setDValue();
        }
    }

    private void setRecoveryTime() {
        System.out.println("How many ticks should it take to recover?");
        int ticks = scanner.nextInt();
        if (ticks >= 1) {
            recoveryTime = ticks;
        } else {
            System.err.println("Invalid recovery time, recovery time must be in the interval [1,inf]. Please try again:");
            setRecoveryTime();
        }
    }

    /**
     * Sets epidemic parameters from user (square infected, infection rate, recovery time, death rate)
     */
    private void setUp() {
        System.out.println("How many rows, master?");
        createMap(Integer.parseInt(scanner.next()));
        setInfected();
        setPValue();
        setRecoveryTime();
        setDValue();
        scanner.close();
    }

    private void setQuarantineZone() {
        System.out.println("Please click on the squares you wish to wall");
        System.out.println("When you are done, please press enter");
    }

    /**
     * Determines which of the two stages(quarantining or main loop) should be launched
     *
     * @param stageNumber which stage the game is in
     */
    private void launchStage(int stageNumber) {
        if (stageNumber == 0) {
            setQuarantineZone();
        } else if (stageNumber == 1) {
            launchVisual();
        }
    }

    private void vaccinateSquare(int squareX, int squareY) {
        epidemicArray[squareX][squareY].vaccinate();
        render();
    }

    private void createWall(int squareX, int squareY) {
        epidemicArray[squareX][squareY].makeWall();
        render();
    }

    /**
     * Launches the visual components of the application: Graph and Epidemic windows.
     * Sets the refresh rate for the Epidemic Window.
     */
    private void launchVisual() {
        openGraph();

        ScheduledExecutorService updateServiceExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });

        ScheduledExecutorService renderServiceExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });

        updateServiceExecutor.scheduleAtFixedRate(this::update, 0, delay, TimeUnit.MILLISECONDS);
        renderServiceExecutor.scheduleAtFixedRate(() -> Platform.runLater(this::render), 0, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * Opens the epidemic statistics graph window and initializes it.
     */
    private void openGraph() {
        Parent root;
        try {
            root = FXMLLoader.load(Main.class.getResource("GraphWindow.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Graph");
            stage.setScene(new Scene(root, 600, 500));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Sets the controls of canvas for setting vaccinated and walled squares.
     */
    private void setControls() {
        canvas.setFocusTraversable(true);

        canvas.setOnKeyPressed((e) -> {
            String code = e.getCode().toString();

            if (code.equals("ENTER") && stageNumber <= 1) { // If user types ENTER, we advance onto the wall stage
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

    /**
     * Sets up epidemic parameters, squares infected, vaccinated and walled. Leads to the start of main game loop.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        gc = canvas.getGraphicsContext2D();

        setControls();
        setUp();
        scanner.close();

        render();
        setVaccinated();
    }
}
