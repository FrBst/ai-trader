package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

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
                Double prev = null;
                while ((line = br.readLine()) != null) {
                    tokens = line.split(",");
                    if (prev == null) {
                        prev = (Double.parseDouble(tokens[1]) + Double.parseDouble(tokens[2]) +
                                Double.parseDouble(tokens[3]) + Double.parseDouble(tokens[4])) / 4 *
                                Double.parseDouble(tokens[6]);
                        continue;
                    }
                    Double toAdd = prev -
                            ((Double.parseDouble(tokens[1]) + Double.parseDouble(tokens[2]) +
                                    Double.parseDouble(tokens[3]) + Double.parseDouble(tokens[4])) / 4 *
                                    Double.parseDouble(tokens[6]));
                    if (toAdd.isInfinite() || toAdd.isNaN()) {
                        continue;
                    }
                    if (!avgPrice.containsKey(tokens[0])) {
                        avgPrice.put(tokens[0], toAdd);
                        count.put(tokens[0], 1);
                    } else {
                        avgPrice.replace(tokens[0], avgPrice.get(tokens[0]) + toAdd);
                        count.replace(tokens[0], count.get(tokens[0]) + 1);
                    }
                    prev = (Double.parseDouble(tokens[1]) + Double.parseDouble(tokens[2]) +
                            Double.parseDouble(tokens[3]) + Double.parseDouble(tokens[4])) / 4 *
                            Double.parseDouble(tokens[6]);
                }
            }
        }

        for (String key : avgPrice.keySet()) {
            avgPrice.replace(key, avgPrice.get(key) / count.get(key));
        }
        for (String key : avgPrice.keySet()) {
            if (Math.abs(avgPrice.get(key)) > 2500000000.0) {
                continue;
            }
            priceData.getData().add(new XYChart.Data<>(key, avgPrice.get(key)));
            countData.getData().add(new XYChart.Data<>(key, count.get(key)));
        }
        priceData.getData().sort(Comparator.comparing(XYChart.Data::getXValue));
        countData.getData().sort(Comparator.comparing(XYChart.Data::getXValue));
        avgSymbolPrice.getData().setAll(priceData);
        symbolsCount.getData().setAll(countData);
    }
}
