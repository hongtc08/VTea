package com.vtea.controller;

import com.vtea.main.MainApp;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class LoginController {

    @FXML
    private void handleLogin(ActionEvent event) {
        MainApp.setRoot("main-layout");
    }
}
