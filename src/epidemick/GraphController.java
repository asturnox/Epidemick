package epidemick;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Main controller for the Graph window.
 * Gets statistics from EpidemicWindowController class and represents these in a graph.
 */
public class GraphController implements Initializable {
    /**
     * Data point series for key epidemic statistics.
     */
    private final XYChart.Series<String, Number> deathSeries = new XYChart.Series<>();
    private final XYChart.Series<String, Number> recoverySeries = new XYChart.Series<>();
    private final XYChart.Series<String, Number> infectionsSeries = new XYChart.Series<>();

    /**
     * Gets epidemic statistics from EpidemicWindowController, updates the graph accordingly.
     */
    private void updateGraph() {
        String ticks = String.valueOf(EpidemicWindowController.ticks);
        int deaths = EpidemicWindowController.deaths;
        int recoveries = EpidemicWindowController.recoveries;
        int infections = EpidemicWindowController.infections;

        deathSeries.getData().add(new XYChart.Data<>(ticks, deaths));
        recoverySeries.getData().add(new XYChart.Data<>(ticks, recoveries));
        infectionsSeries.getData().add(new XYChart.Data<>(ticks, infections));

        if (deathSeries.getData().size() > 600) {   // If data points are larger than width of graph, remove earlier ones
            deathSeries.getData().remove(0);
            recoverySeries.getData().remove(0);
            infectionsSeries.getData().remove(0);

        }

    }

    @FXML
    LineChart<String, Number> lineChart;

    /**
     * Initializes LineChart graph, starts a new Thread scheduled to execute at a determined rate.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        final CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Tick");
        xAxis.setAnimated(false);
        yAxis.setLabel("Amount");
        yAxis.setAnimated(false);

        lineChart.setTitle("Statistics");
        lineChart.setAnimated(true);

        deathSeries.setName("Deaths");
        recoverySeries.setName("Recoveries");
        infectionsSeries.setName("Infected");

        lineChart.getData().add(infectionsSeries);
        lineChart.getData().add(deathSeries);
        lineChart.getData().add(recoverySeries);

        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(); // Create new thread for efficiency
        scheduledExecutorService.scheduleAtFixedRate(() -> Platform.runLater(this::updateGraph), 0, 100, TimeUnit.MILLISECONDS);

    }
}
