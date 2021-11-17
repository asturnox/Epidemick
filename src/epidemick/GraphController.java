package epidemick;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class GraphController implements Initializable {
    private final XYChart.Series<String, Number> deathSeries = new XYChart.Series<>();
    private final XYChart.Series<String, Number> recoverySeries = new XYChart.Series<>();
    private final XYChart.Series<String, Number> infectionsSeries = new XYChart.Series<>();

    public void updateGraph() {
        String ticks = String.valueOf(EpidemicWindowController.ticks);
        int deaths = EpidemicWindowController.deaths;
        int recoveries = EpidemicWindowController.recoveries;
        int infections = EpidemicWindowController.infections;

        deathSeries.getData().add(new XYChart.Data<>(ticks, deaths));
        recoverySeries.getData().add(new XYChart.Data<>(ticks, recoveries));
        infectionsSeries.getData().add(new XYChart.Data<>(ticks, infections));

        if (deathSeries.getData().size() > 750) {
            deathSeries.getData().remove(0);
            recoverySeries.getData().remove(0);
            infectionsSeries.getData().remove(0);

        }

    }

    @FXML
    Canvas canvas;

    @FXML
    LineChart<String, Number> lineChart;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        final CategoryAxis xAxis = new CategoryAxis();  NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time/s");
        xAxis.setAnimated(false);
        yAxis.setLabel("Value");
        yAxis.setAnimated(false);

        lineChart.setTitle("Statistics");
        lineChart.setAnimated(true);

        deathSeries.setName("Deaths");
        recoverySeries.setName("Recoveries");
        infectionsSeries.setName("Infected");

        lineChart.getData().add(infectionsSeries);
        lineChart.getData().add(deathSeries);
        lineChart.getData().add(recoverySeries);

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.1), actionEvent -> {
            updateGraph();
            System.out.println(EpidemicWindowController.deaths);
        }));

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.playFromStart();
    }
}
