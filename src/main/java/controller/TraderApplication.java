package controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import rl4j.TrainA3C;

import java.io.IOException;

public class TraderApplication {
    public void getDataButtonClicked() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/LoadDataWindow.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Get data");
        stage.setScene(new Scene(root, 200, 300));
        stage.show();
    }

    public void browseSymbolsButtonClicked() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/SymbolBrowser.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Browse symbols");
        stage.setScene(new Scene(root));
        stage.show();
    }

    public void statisticsButtonClicked() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/SymbolStatistics.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Statistics");
        stage.setScene(new Scene(root));
        stage.show();
    }

    public void trainButtonClicked() {
        Thread fuck = new Thread(new TrainA3C());
        fuck.start();
    }
}
