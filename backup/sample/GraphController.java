package epidemick;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class GraphController implements Initializable {
    GraphicsContext gc;
    Color[] colors = new Color[] {Color.GREEN, Color.BLUE};
    int n = 0;

    public void updateGraph() {
        gc.setFill(colors[n % 2]);
        gc.fillRect(0, 0, 750, 500);
        n++;
    }

    @FXML
    Canvas canvas;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        gc = canvas.getGraphicsContext2D();
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.1), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                updateGraph();
                System.out.println("a");
            }
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.playFromStart();

    }
}
