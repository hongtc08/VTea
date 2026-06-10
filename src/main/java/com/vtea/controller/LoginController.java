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
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ProgressBar;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.scene.control.ProgressIndicator;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
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
    private StackPane loadingOverlay;

    @FXML
    private ProgressIndicator loadingSpinner;

    @FXML
    private FontIcon successIcon;

    @FXML
    private Label loadingLabel;

    @FXML
    private ProgressBar successProgressBar;

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

        // Hiển thị loading overlay
        if (loadingOverlay != null) {
            loadingOverlay.setVisible(true);
            if (loadingSpinner != null) {
                loadingSpinner.setVisible(true);
            }
            successIcon.setVisible(false);
            loadingLabel.setText("Đang xử lý...");
            loadingLabel.setStyle("-fx-text-fill: #5d4037;");
            if (successProgressBar != null) {
                successProgressBar.setVisible(false);
                successProgressBar.setManaged(false);
                successProgressBar.setProgress(0.0);
            }
        }
        btnLogin.setDisable(true);

        CompletableFuture.supplyAsync(() -> {
            try {
                // Đóng gói dữ liệu gửi xuống Service
                LoginRequestDTO request = new LoginRequestDTO(username, password);
                return authService.login(request);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenAccept(sessionInfo -> {
            Platform.runLater(() -> {
                SessionManager.login(sessionInfo);
                preloadSystemDataThenOpenMain(sessionInfo.getFullName());
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                if (loadingOverlay != null) loadingOverlay.setVisible(false);
                btnLogin.setDisable(false);
                
                // Lấy ra nguyên nhân gốc rễ (root cause) để không bị dính chữ Exception
                Throwable rootCause = ex;
                while (rootCause.getCause() != null) {
                    rootCause = rootCause.getCause();
                }
                
                String errorMessage = rootCause.getMessage() != null ? rootCause.getMessage() : "Lỗi không xác định!";
                DialogHelper.showInfo("Lỗi Đăng Nhập", errorMessage);
            });
            return null;
        });
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
                    // Hiệu ứng thành công
                    if (loadingSpinner != null) {
                        loadingSpinner.setVisible(false);
                    }
                    if (successIcon != null) successIcon.setVisible(true);
                    if (loadingLabel != null) {
                        loadingLabel.setText("Xin chào, " + fullName + "!");
                        loadingLabel.setStyle("-fx-text-fill: #16a34a;");
                    }
                    
                    if (successProgressBar != null) {
                        successProgressBar.setVisible(true);
                        successProgressBar.setManaged(true);
                        
                        Timeline timeline = new Timeline(
                                new KeyFrame(Duration.ZERO, new KeyValue(successProgressBar.progressProperty(), 0.0)),
                                new KeyFrame(Duration.millis(800), new KeyValue(successProgressBar.progressProperty(), 1.0))
                        );
                        timeline.setOnFinished(event -> {
                            System.out.println("Đã tải cache POS, chuẩn bị chuyển sang màn hình chính...");
                            MainApp.setRoot("main-layout");
                        });
                        timeline.play();
                    } else {
                        PauseTransition pause = new PauseTransition(Duration.millis(800));
                        pause.setOnFinished(event -> {
                            System.out.println("Đã tải cache POS, chuẩn bị chuyển sang màn hình chính...");
                            MainApp.setRoot("main-layout");
                        });
                        pause.play();
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        if (loadingOverlay != null) loadingOverlay.setVisible(false);
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
