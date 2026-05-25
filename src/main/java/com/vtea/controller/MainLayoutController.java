package com.vtea.controller;

import com.vtea.dto.UserSessionDTO;
import com.vtea.main.MainApp;
import com.vtea.utils.DialogHelper;
import com.vtea.utils.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.net.URL;

public class MainLayoutController {

    @FXML private StackPane contentArea;
    @FXML private Label userInitialLabel;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private Button btnDashboard;
    @FXML private Button btnPOS;
    @FXML private Button btnMenu;
    @FXML private Button btnInventory;
    @FXML private Button btnEmployee;

    @FXML
    public void initialize() {
        bindUserHeader();
        applyRoleBasedMenu();
        loadView("dashboard");
    }

    private void bindUserHeader() {
        UserSessionDTO user = SessionManager.getCurrentUser();
        if (user == null) {
            return;
        }

        String fullName = user.getFullName() != null ? user.getFullName() : user.getUsername();
        userNameLabel.setText(fullName);
        userRoleLabel.setText(SessionManager.isAdmin() ? "Quản lý" : "Nhân viên");

        if (fullName != null && !fullName.isBlank()) {
            userInitialLabel.setText(fullName.substring(0, 1).toUpperCase());
        }
    }

    private void applyRoleBasedMenu() {
        boolean admin = SessionManager.isAdmin();
        setNavVisible(btnMenu, admin);
        setNavVisible(btnInventory, admin);
        setNavVisible(btnEmployee, admin);
    }

    private void setNavVisible(Button button, boolean visible) {
        if (button != null) {
            button.setVisible(visible);
            button.setManaged(visible);
        }
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
    private void handleMenu(ActionEvent event) {
        loadView("menu");
    }

    @FXML
    public void handleInventory(ActionEvent actionEvent) {
        loadView("inventory");
    }

    @FXML
    public void handleEmployee(ActionEvent actionEvent) {
        loadView("employee");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        SessionManager.logout();
        MainApp.setRoot("login");
    }

    private void loadView(String fxml) {
        try {
            URL fileUrl = MainApp.class.getResource("/com/vtea/view/" + fxml + ".fxml");

            if (fileUrl == null) {
                DialogHelper.showInfo("Lỗi Đường Dẫn", "Không tìm thấy file: " + fxml + ".fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fileUrl);
            Parent view = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);

        } catch (Exception e) {
            e.printStackTrace();
            DialogHelper.showInfo("Lỗi Code Bên Trong File " + fxml, "Nguyên nhân: " + e.getMessage());
        }
    }
}
