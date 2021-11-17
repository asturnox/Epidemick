package epidemick;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

        ScheduledExecutorService scheduledExecutorService;
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        scheduledExecutorService.scheduleAtFixedRate(() -> Platform.runLater(this::updateGraph), 0, 100, TimeUnit.MILLISECONDS);

    }
}
