package com.vtea.controller;

import com.vtea.main.MainApp;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class MainLayoutController implements Initializable {

    @FXML private StackPane contentArea;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private Label userInitialLabel;

    // Menu buttons
    @FXML private Button btnDashboard;
    @FXML private Button btnPOS;

    private Button activeButton;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Hiển thị thông tin user đăng nhập
        if (LoginController.currentUser != null) {
            String fullName = LoginController.currentUser.getFullName();
            String role = LoginController.currentUser.getRole();
            userNameLabel.setText(fullName != null ? fullName : LoginController.currentUser.getUserName());
            userRoleLabel.setText(role != null ? capitalize(role) : "Nhân viên");
            // Lấy chữ cái đầu làm avatar
            String initial = (fullName != null && !fullName.isEmpty())
                    ? String.valueOf(fullName.charAt(0)).toUpperCase()
                    : "U";
            userInitialLabel.setText(initial);
        }

        // Load Dashboard mặc định
        navigateTo("/com/vtea/view/dashboard.fxml", btnDashboard);
    }

    @FXML
    private void handleDashboard() {
        navigateTo("/com/vtea/view/dashboard.fxml", btnDashboard);
    }

    @FXML
    private void handlePOS() {
        navigateTo("/com/vtea/view/pos.fxml", btnPOS);
    }

    @FXML
    private void handleLogout() {
        try {
            LoginController.currentUser = null;
            MainApp.navigateTo("/com/vtea/view/login.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void navigateTo(String fxmlPath, Button clickedButton) {
        try {
            Node view = FXMLLoader.load(
                    Objects.requireNonNull(getClass().getResource(fxmlPath))
            );
            contentArea.getChildren().setAll(view);

            // Cập nhật trạng thái active cho menu
            if (activeButton != null) {
                activeButton.getStyleClass().remove("nav-btn-active");
                activeButton.getStyleClass().add("nav-btn");
            }
            clickedButton.getStyleClass().remove("nav-btn");
            clickedButton.getStyleClass().add("nav-btn-active");
            activeButton = clickedButton;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
