package com.vtea.controller;

import com.vtea.dto.LoginRequestDTO;
import com.vtea.dto.UserSessionDTO;
import com.vtea.main.MainApp;
import com.vtea.service.AuthService;
import com.vtea.utils.SessionManager;
import com.vtea.utils.DialogHelper;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import com.vtea.service.POSCacheService;
import javafx.application.Platform;
import java.util.concurrent.CompletableFuture;

public class LoginController {

    // 1. Khai báo các thành phần giao diện (Nhớ phải có cờ @FXML)
    @FXML
    private TextField txtUsername; // Ô nhập tài khoản

    @FXML
    private PasswordField txtPassword; // Ô nhập mật khẩu (ẩn ký tự)

    @FXML
    private TextField txtPasswordVisible; // Ô hiển thị mật khẩu

    @FXML
    private CheckBox showPasswordCheckbox; // Checkbox hiển thị mật khẩu

    @FXML
    private Button btnLogin; // Nút đăng nhập

    @FXML
    public void initialize() {
        // Đồng bộ dữ liệu giữa 2 ô nhập liệu
        txtPasswordVisible.textProperty().bindBidirectional(txtPassword.textProperty());

        // Bắt sự kiện check/uncheck
        showPasswordCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                // Hiện ô password text rõ, ẩn ô ẩn
                txtPasswordVisible.setVisible(true);
                txtPasswordVisible.setManaged(true);
                txtPassword.setVisible(false);
                txtPassword.setManaged(false);
            } else {
                // Ẩn ô password text rõ, hiện ô ẩn
                txtPasswordVisible.setVisible(false);
                txtPasswordVisible.setManaged(false);
                txtPassword.setVisible(true);
                txtPassword.setManaged(true);
            }
        });
    }

    // 2. Khởi tạo Service để xử lý logic
    private AuthService authService = new AuthService();

    //Khoi tao cache tu luc log in
    private final POSCacheService posCacheService = POSCacheService.getInstance();

    // 3. Hàm này sẽ được gọi khi người dùng bấm nút "Đăng Nhập"
    @FXML
    public void handleLogin(ActionEvent event) {
        // Lấy dữ liệu từ màn hình
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        // Kiểm tra xem người dùng có để trống không
        if (username.isEmpty() || password.isEmpty()) {
            DialogHelper.showInfo("Cảnh báo", "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!");
            return; // Dừng lại, không chạy xuống dưới nữa
        }

        try {
            // Đóng gói dữ liệu gửi xuống Service
            LoginRequestDTO request = new LoginRequestDTO(username, password);

            // Service sẽ ném lỗi ra nếu sai, còn qua được dòng này là thành công!
            UserSessionDTO sessionInfo = authService.login(request);

            // Lưu thông tin người dùng vào SessionManager
            SessionManager.login(sessionInfo);

            preloadSystemDataThenOpenMain(sessionInfo.getFullName());
        } catch (Exception e) {
            // Nếu AuthService ném lỗi (sai pass, tài khoản khóa...), hiện Popup báo lỗi
            DialogHelper.showInfo("Lỗi Đăng Nhập", e.getMessage());
        }
    }

    @FXML
    private void handleForgotPassword(ActionEvent event) {
        DialogHelper.showInfo(
                "Quên mật khẩu",
                "Vui lòng liên hệ số điện thoại 0987654321 để được hỗ trợ xử lý."
        );
    }

    private void preloadSystemDataThenOpenMain(String fullName) {
        btnLogin.setDisable(true);
        btnLogin.setText("Đang tải dữ liệu...");

        CompletableFuture
                .runAsync(() -> posCacheService.loadIfNeeded())
                .thenRun(() -> Platform.runLater(() -> {
                    DialogHelper.showInfo(
                            "Thành công",
                            "Đăng nhập thành công!\nXin chào: " + fullName
                    );

                    System.out.println("Đã tải cache POS, chuẩn bị chuyển sang màn hình chính...");
                    MainApp.setRoot("main-layout");
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        btnLogin.setDisable(false);
                        btnLogin.setText("Đăng Nhập");

                        ex.printStackTrace();
                        DialogHelper.showInfo("Lỗi", "Không thể tải dữ liệu hệ thống!");
                    });

                    return null;
                });
    }

    // 4. Hàm tiện ích để hiển thị Popup thông báo cho gọn code
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
