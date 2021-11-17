package epidemick;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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

public class Controller implements Initializable {
    public ArrayList<EpidemicObject> infectedArray = new ArrayList<>();
    public ArrayList<EpidemicObject> arrayToInfect = new ArrayList<>();
    public EpidemicObject[][] epidemicArray;
    public int numRows = 10;
    Scanner scanner = new Scanner(System.in);

    public int ticks = 0;

    // 1 - d% = (1 - p)^n
    // p = 1 - (1 - d%)^(1/n)

    public int recoveryTime;
    public double pValue;
    public double dValue;

    public int deaths = 0;

    public Random randomizer = new Random();

    public Canvas canvas = Main.canvas;
    public GraphicsContext gc = Main.gc;
    public double buffer;

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
                removedArray.add(infected);
            }
            else if (ticksSinceInfected >= recoveryTime) {
                infected.recover();
                removedArray.add(infected);
            } else {
                if (i >= 1) {
                    epidemicArray[i-1][j].attemptInfect();
                } if (i <= epidemicArray.length - 2) {
                    epidemicArray[i+1][j].attemptInfect();
                } if (j >= 1) {
                    epidemicArray[i][j-1].attemptInfect();
                } if (j <= epidemicArray.length - 2) {
                    epidemicArray[i][j+1].attemptInfect();
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
            o.infect();
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
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.1), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                update();
                render();
            }
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
            stage.setScene(new Scene(root, 450, 450));
            stage.show();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("How many rows, master?");
        createMap(Integer.parseInt(scanner.next()));
        setInfected();
        setPValue();
        setRecoveryTime();
        setDValue();
        scanner.close();
    }
}
