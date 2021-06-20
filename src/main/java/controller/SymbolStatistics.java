package controller;

import com.sun.javafx.animation.KeyValueType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

// todo: move file paths to config.
public class SymbolStatistics {

    @FXML
    private BarChart symbolsCount;

    @FXML
    private LineChart avgSymbolPrice;

    String dailyAdjustedDataFolder = "src/main/resources/dataset/daily-adjusted/";
    final int WINDOW = 10;
    private final ObservableList<String> symbols = FXCollections.observableArrayList(
            Arrays.stream(new File(dailyAdjustedDataFolder).list())
                    .map(s -> s.substring(0, s.length() - 4))
                    .collect(Collectors.toList())
    );

    public void initialize() throws IOException {
        fillCharts();
    }

    public void fillCharts() throws IOException {
        var priceData = new XYChart.Series<String, Number>();
        var countData = new XYChart.Series<String, Number>();
        Map<String, Double> avgPrice = new HashMap<>();
        Map<String, Integer> count = new HashMap<>();

        for (String selectedSymbol : symbols) {
            System.out.println("Analyzing symbol " + selectedSymbol);
            try (BufferedReader br = new BufferedReader(new FileReader(dailyAdjustedDataFolder + selectedSymbol + ".csv"))) {
                String line;
                if ((line = br.readLine()) == null || line.equals("{")) {
                    System.out.print(" (downloaded with error)");
                    continue;
                }
                String[] tokens;
                while ((line = br.readLine()) != null) {
                    tokens = line.split(",");
                    avgPrice.merge(tokens[0], (Double.parseDouble(tokens[1]) + Double.parseDouble(tokens[2]) +
                            Double.parseDouble(tokens[3]) + Double.parseDouble(tokens[4])) / 4, Double::sum);
                    count.merge(tokens[0], 1, Integer::sum);
                }
            }
        }

        avgPrice.replaceAll((k, v) -> v / count.get(k));

        String[] keys = avgPrice.keySet().stream().sorted().toArray(String[]::new);
        Double[] values = new Double[keys.length];
        for (int i = 0; i < keys.length; i++) { values[i] = avgPrice.get(keys[i]); }

        double sum = 0.0;
        for (int i = 0; i < WINDOW && keys.length > i; i++) {
            sum += values[i];
            avgPrice.put(keys[i], sum / (i+1));
        }
        for (int i = WINDOW; keys.length > i; i++) {
            sum = sum - values[i-WINDOW] + values[i];
            avgPrice.put(keys[i], sum / WINDOW);
        }

        // todo: loading from the cache!
        try {
            File myObj = new File("src/main/resources/dataset/moving-" + WINDOW + "-overall.csv");
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        try {
            FileWriter myWriter = new FileWriter("src/main/resources/dataset/moving-" + WINDOW + "-overall.csv");
            myWriter.write("date,moving_average\n");
            for (String key : avgPrice.keySet().stream().sorted().collect(Collectors.toList())) {
                myWriter.write(key + "," + avgPrice.get(key) + "\n");
            }
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        for (String key : avgPrice.keySet()) {
            priceData.getData().add(new XYChart.Data<>(key, avgPrice.get(key)));
            countData.getData().add(new XYChart.Data<>(key, count.get(key)));
        }
        priceData.getData().sort(Comparator.comparing(XYChart.Data::getXValue));
        countData.getData().sort(Comparator.comparing(XYChart.Data::getXValue));
        avgSymbolPrice.getData().setAll(priceData);
        symbolsCount.getData().setAll(countData);
    }
}
