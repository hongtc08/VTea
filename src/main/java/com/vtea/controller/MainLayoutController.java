package com.vtea.controller;

import com.vtea.main.MainApp;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;
import com.vtea.utils.DialogHelper;

import java.net.URL;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

public class MainLayoutController {

    @FXML
    private StackPane contentArea;

    @FXML private javafx.scene.control.Label userInitialLabel;
    @FXML private javafx.scene.control.Label userNameLabel;
    @FXML private javafx.scene.control.Label userRoleLabel;

    @FXML
    public void initialize() {
        // Cập nhật thông tin người dùng đang đăng nhập
        com.vtea.dto.UserSessionDTO user = com.vtea.utils.SessionManager.getCurrentUser();
        if (user != null) {
            userNameLabel.setText(user.getFullName());
            // Format role (ADMIN -> Quản lý, STAFF -> Nhân viên)
            String roleText = "ADMIN".equalsIgnoreCase(user.getRole()) ? "Quản lý" : "Nhân viên";
            userRoleLabel.setText(roleText);
            
            if (user.getFullName() != null && !user.getFullName().isEmpty()) {
                userInitialLabel.setText(user.getFullName().substring(0, 1).toUpperCase());
            }
        }

        // Mặc định load màn hình dashboard đầu tiên
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

    // Thêm hàm xử lý nút Thực đơn
    @FXML
    private void handleMenu(ActionEvent event) {
        loadView("menu");
    }

    @FXML
    public void handleInventory(ActionEvent actionEvent) {loadView("inventory");}

    @FXML
    public void handleEmployee(ActionEvent actionEvent) {loadView("employee");}

    @FXML
    public void handleInvoiceHistory(ActionEvent actionEvent) {loadView("invoiceHistory");}

    @FXML
    private void handleLogout(ActionEvent event) {
        // Trở về màn hình đăng nhập
        MainApp.setRoot("login");
    }

    // Hàm load màn hình con siêu "xịn" giúp bạn phát hiện mọi lỗi
    private void loadView(String fxml) {
        try {
            // 1. Kiểm tra xem có tìm thấy file FXML không
            URL fileUrl = MainApp.class.getResource("/com/vtea/view/" + fxml + ".fxml");

            if (fileUrl == null) {
                // Nếu đường dẫn bị sai, hiển thị thông báo ngay
                DialogHelper.showInfo("Lỗi Đường Dẫn", "Không tìm thấy file: " + fxml + ".fxml\nHãy kiểm tra lại thư mục /com/vtea/view/");
                return;
            }

            // Hiển thị hiệu ứng loading nhỏ nhắn mượt mà
            javafx.scene.control.ProgressIndicator spinner = new javafx.scene.control.ProgressIndicator();
            spinner.setMaxSize(40, 40);
            
            StackPane loadingPane = new StackPane(spinner);
            contentArea.getChildren().clear();
            contentArea.getChildren().add(loadingPane);

            // Load giao diện ở background thread để vòng xoay không bị "đứng hình"
            javafx.concurrent.Task<Parent> loadTask = new javafx.concurrent.Task<Parent>() {
                @Override
                protected Parent call() throws Exception {
                    FXMLLoader loader = new FXMLLoader(fileUrl);
                    return loader.load();
                }
            };

            loadTask.setOnSucceeded(e -> {
                Parent view = loadTask.getValue();
                // Thêm hiệu ứng Fade In để UX mượt mà hơn
                view.setOpacity(0);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(250), view);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();

                // Đưa vào màn hình chính
                contentArea.getChildren().clear();
                contentArea.getChildren().add(view);
            });

            loadTask.setOnFailed(e -> {
                Throwable ex = loadTask.getException();
                ex.printStackTrace();
                DialogHelper.showInfo("Lỗi Load FXML", "Có lỗi xảy ra khi tải giao diện: " + ex.getMessage());
            });

            Thread thread = new Thread(loadTask);
            thread.setDaemon(true);
            thread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}