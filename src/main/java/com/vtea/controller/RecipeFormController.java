package com.vtea.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class RecipeFormController {
    @FXML private Button btnClose;
    @FXML private Button btnCancel;
    @FXML private Button btnSubmit;

    @FXML
    public void initialize() {
        btnClose.setOnAction(event -> closeWindow());
        btnCancel.setOnAction(event -> closeWindow());
        btnSubmit.setOnAction(event -> closeWindow());
    }

    private void closeWindow() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }
}
