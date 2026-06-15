package com.vtea.controller;

import com.vtea.dto.LoginRequestDTO;
import com.vtea.main.MainApp;
import com.vtea.service.AuthService;
import com.vtea.utils.SessionManager;
import com.vtea.utils.DialogHelper;
import com.vtea.utils.EmailService;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.application.Platform;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ProgressBar;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
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
                com.vtea.utils.SnackbarHelper.showSnackbar(com.vtea.utils.SnackbarHelper.ERROR, errorMessage);
            });
            return null;
        });
    }

    @FXML
    private void handleForgotPassword(ActionEvent event) {
        // 1. Yeu cau nhap Username
        String username = showCustomDialog("Khôi phục mật khẩu", "Nhập tên đăng nhập của bạn:", true, true);

        if (username != null && !username.trim().isEmpty()) {
            String email = authService.getEmailByUsername(username.trim());

            if (email != null) {
                // 2. Tao OTP va gui Email
                String otp = EmailService.generateOTP();
                if (EmailService.sendOTPEmail(email, otp)) {

                    // 3. Yeu cau nhap OTP
                    String otpInput = showCustomDialog("Xác thực OTP", "Mã đã gửi đến: " + email + "\nVui lòng nhập 6 số OTP:", true, true);

                    if (otpInput != null && otpInput.trim().equals(otp)) {

                        // 4. Khop OTP -> Dat mat khau moi
                        String newPassword = showCustomDialog("Mật khẩu mới", "Xác thực thành công!\nNhập mật khẩu mới của bạn:", true, true);

                        if (newPassword != null && !newPassword.trim().isEmpty()) {
                            if (authService.updatePassword(username.trim(), newPassword.trim())) {
                                showCustomDialog("Thành công", "Đổi mật khẩu thành công! Vui lòng đăng nhập lại.", false, false);
                            } else {
                                showCustomDialog("Thất bại", "Không thể cập nhật mật khẩu mới.", false, false);
                            }
                        }
                    } else {
                        showCustomDialog("Lỗi xác thực", "Mã OTP không chính xác hoặc đã bị hủy!", false, false);
                    }
                } else {
                    showCustomDialog("Lỗi hệ thống", "Trạm gửi thư đang bận. Không thể gửi email!", false, false);
                }
            } else {
                showCustomDialog("Không tìm thấy", "Tên đăng nhập không tồn tại hoặc chưa liên kết Email!", false, false);
            }
        }
    }

    // Ham goi Custom Dialog
    private String showCustomDialog(String title, String message, boolean isConfirmType, boolean hasInput) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vtea/view/CustomDialog.fxml"));
            Parent root = loader.load();

            CustomDialogController controller = loader.getController();
            controller.setDialogData(title, message, isConfirmType, hasInput);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            stage.showAndWait();

            if (controller.isConfirmed()) {
                return controller.getInputResult();
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi load giao diện CustomDialog");
            e.printStackTrace();
        }
        return null;
    }

    private void preloadSystemDataThenOpenMain(String fullName) {
        btnLogin.setDisable(true);
        btnLogin.setText("Đang tải dữ liệu...");

        CompletableFuture
                .runAsync(() -> {
                    // Cache removed, do nothing
                })
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
                            System.out.println("Chuẩn bị chuyển sang màn hình chính...");
                            MainApp.setRoot("main-layout");
                        });
                        timeline.play();
                    } else {
                        PauseTransition pause = new PauseTransition(Duration.millis(800));
                        pause.setOnFinished(event -> {
                            System.out.println("Chuẩn bị chuyển sang màn hình chính...");
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
