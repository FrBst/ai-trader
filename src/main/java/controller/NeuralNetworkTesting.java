package controller;

import application.Configuration;
import com.clearspring.analytics.stream.ITopK;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import org.deeplearning4j.rl4j.network.ac.ActorCriticSeparate;
import org.deeplearning4j.rl4j.policy.ACPolicy;
import org.deeplearning4j.util.ModelSerializer;
import rl4j.SimpleBroker;
import service.NetworkTest;

import javafx.scene.control.*;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class NeuralNetworkTesting {

    private ObservableList<String> results;
    private ObservableList<String> networks;

    private ACPolicy<SimpleBroker> selectedPolicy;

    @FXML
    private TextField numberTextField;

    @FXML
    private ListView<String> resultsList;

    @FXML
    private LineChart priceChart;
    @FXML
    private LineChart portfolioChart;
    @FXML
    private LineChart volumeChart;

    @FXML
    private TextArea summary;

    @FXML
    private ChoiceBox<String> networkChoice;

    public void initialize() {
        results = FXCollections.observableArrayList(Arrays.stream(new File(Configuration.getConfig("temp-folder")).list()).toList());

        networks = FXCollections.observableArrayList(
                Arrays.stream(new File(Configuration.getConfig("network-folder")).list())
                        .map(s -> s.substring(s.indexOf('-')+1))
                        .distinct()
                        .toList());
        networkChoice.setItems(networks);
        networkChoice.getSelectionModel()
                .selectedItemProperty()
                .addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> selectNetwork(newValue));
        networkChoice.getSelectionModel().selectFirst();

        resultsList.setItems(results);
        resultsList.setCellFactory(new Callback<ListView<String>, ListCell<String>>()
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
        try {
            makeSummary();
        } catch (IOException e) {
            e.printStackTrace();
        }

        networkChoice.setItems(networks);
    }

    // todo: threading????
    // todo: code style? Please? Somebody?
    public void onLaunchButtonClicked() {
        clearTemp();
        int threadNumber;
        if (numberTextField.getText() == "") {
            threadNumber = 1;
        } else {
            threadNumber = Integer.parseInt(numberTextField.getText());
        }
        if (threadNumber < 1) { threadNumber = 1; }
        List<Thread> testers = new LinkedList<>();
        for (int i = 0; i < threadNumber; i++) {
            //testers.add(new Thread(new NetworkTest(1000)));
            //testers.get(testers.size()-1).start();
            new NetworkTest(selectedPolicy, 1000, i).run();
        }
        resultsList.refresh();
        results.setAll(Arrays.stream(new File(Configuration.getConfig("temp-folder")).list()).toList());
        try {
            makeSummary();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        for (Thread th : testers ) {
//            try {
//                th.join();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
    }

    private void makeSummary() throws IOException {
        if (results.isEmpty()) {
            summary.setText("Запустите тест, чтобы увидеть производительность");
            return;
        }
        double yearlyGrowth = 0;

        for (String name : results) {
            try (BufferedReader br = new BufferedReader(new FileReader(Configuration.getConfig("temp-folder") + name))) {
                String line;
                br.readLine();
                line = br.readLine();
                int len = 1;
                String[] tokens;
                do {
                    tokens = line.split(",");
                    len++;
                } while ((line = br.readLine()) != null);
                yearlyGrowth += (Double.parseDouble(tokens[0]) - 1000.0) / (double) len * 36.5;
            }
        }
        yearlyGrowth /= results.size();

        summary.setText("Средняя доходность " + String.format("%.2f", yearlyGrowth) + "% в год");
    }

    private void clearTemp() {
        File[] files = new File(Configuration.getConfig("temp-folder")).listFiles();
        if(files != null) {
            for(File f: files) {
                f.delete();
            }
        }
    }

    public void onSymbolSelected() throws IOException {
        String selectedSymbol = resultsList.getSelectionModel().getSelectedItem();

        var portfolio = new XYChart.Series<Number, Number>();
        var volume = new XYChart.Series<Number, Number>();
        var price = new XYChart.Series<Number, Number>();
        try (BufferedReader br = new BufferedReader(new FileReader(Configuration.getConfig("temp-folder") + selectedSymbol))) {
            String line;
            br.readLine();
            int i = 1;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(",");
                portfolio.getData().add(new XYChart.Data<>(i, Double.parseDouble(tokens[0])));
                volume.getData().add(new XYChart.Data<>(i, Double.parseDouble(tokens[1])));
                price.getData().add(new XYChart.Data<>(i, Double.parseDouble(tokens[2])));
                i++;
            }
        }
        priceChart.getData().setAll(price);
        portfolioChart.getData().setAll(portfolio);
        volumeChart.getData().setAll(volume);
    }

    @FXML
    private void selectNetwork(String name) {
        try {
            selectedPolicy = new ACPolicy<SimpleBroker>(
                    new ActorCriticSeparate(ModelSerializer.restoreMultiLayerNetwork(Configuration.getConfig("network-folder") + "value-" + name),
                    ModelSerializer.restoreMultiLayerNetwork(Configuration.getConfig("network-folder") + "policy-" + name)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
