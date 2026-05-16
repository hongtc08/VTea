package com.vtea.controller;

import com.vtea.main.MainApp;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;
import com.vtea.utils.DialogHelper;

import java.net.URL;

public class MainLayoutController {

    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
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

            // 2. Tiến hành load giao diện
            FXMLLoader loader = new FXMLLoader(fileUrl);
            Parent view = loader.load();

            // 3. Đưa vào màn hình chính
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);

        } catch (Exception e) {
            // Nếu file tìm thấy nhưng load bị lỗi (do sai Controller, thiếu ID...)
            e.printStackTrace();
            DialogHelper.showInfo("Lỗi Code Bên Trong File " + fxml, "Nguyên nhân: " + e.getMessage() + "\n(Vui lòng xem thêm chi tiết màu đỏ dưới console của IDE)");        }
    }
}