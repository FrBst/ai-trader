package controller;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.commons.io.IOUtils;

import java.awt.event.MouseEvent;
import java.io.*;
import java.nio.Buffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

// todo: запилить поиск.
public class SymbolBrowser {

    String dailyAdjustedDataFolder = "src/main/resources/dataset/daily-adjusted/";
    private final ObservableList<String> symbols = FXCollections.observableArrayList(
            Arrays.stream(new File(dailyAdjustedDataFolder).list())
            .map(s -> s.substring(0, s.length() - 4))
            .collect(Collectors.toList())
    );

    @FXML
    private ListView<String> symbolListView;

    @FXML
    private LineChart chart;

    public void initialize() {
        symbolListView.setItems(symbols);
        System.out.println(symbols.size());
        symbolListView.setCellFactory(new Callback<ListView<String>, ListCell<String>>()
        {
            @Override
            public ListCell<String> call(ListView<String> listView)
            {
                final ListCell<String> cell = new ListCell<>() {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            setText(item);
                        }
                    }
                };
                return cell;
            }
        });
    }

    public void onSymbolSelected() throws IOException {
        String selectedSymbol = symbolListView.getSelectionModel().getSelectedItem();

        var data = new XYChart.Series<String, Number>();
        try (BufferedReader br = new BufferedReader(new FileReader(dailyAdjustedDataFolder + selectedSymbol + ".csv"))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(",");
                data.getData().add(new XYChart.Data<>(tokens[0], (Double.parseDouble(tokens[5]))));
            }
        }
        data.getData().sort(Comparator.comparing(XYChart.Data::getXValue));
        chart.getData().setAll(data);
    }
}
