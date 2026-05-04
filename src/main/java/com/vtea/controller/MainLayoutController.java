package com.vtea.controller;

import com.vtea.main.MainApp;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class MainLayoutController {

    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        loadView("dashboard");
    }

    @FXML
    private void handleDashboard(ActionEvent event) {
        loadView("dashboard");
    }

    @FXML
    private void handlePOS(ActionEvent event) {
        loadView("pos");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        MainApp.setRoot("login");
    }

    private void loadView(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/com/vtea/view/" + fxml + ".fxml"));
            Parent view = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
