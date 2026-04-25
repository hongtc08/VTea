package com.vtea.controller;

import com.vtea.main.MainApp;
import com.vtea.model.User;
import com.vtea.utils.DBConnection;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private VBox formCard;

    // Lưu thông tin user đăng nhập thành công để các màn hình sau dùng
    public static User currentUser;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Hiệu ứng fade-in khi màn hình load
        FadeTransition fade = new FadeTransition(Duration.millis(600), formCard);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

        // Disable nút login nếu field trống
        loginButton.disableProperty().bind(
                usernameField.textProperty().isEmpty()
                        .or(passwordField.textProperty().isEmpty())
        );

        // Ẩn label lỗi ban đầu
        errorLabel.setVisible(false);

        // Nhấn Enter để đăng nhập
        passwordField.setOnAction(e -> handleLogin());
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        // Unbind trước khi set disable
        loginButton.disableProperty().unbind();
        loginButton.setDisable(true);
        loginButton.setText("Đang đăng nhập...");
        errorLabel.setVisible(false);

        // Chạy DB query trên thread riêng để không block UI
        new Thread(() -> {
            User user = authenticateUser(username, password);
            Platform.runLater(() -> {
                loginButton.setDisable(false);
                loginButton.setText("Đăng nhập");
                // Re-bind sau khi xong
                loginButton.disableProperty().bind(
                        usernameField.textProperty().isEmpty()
                                .or(passwordField.textProperty().isEmpty())
                );
                if (user != null) {
                    currentUser = user;
                    try {
                        MainApp.navigateTo("/com/vtea/view/main-layout.fxml");
                    } catch (IOException ex) {
                        showError("Không thể tải màn hình chính!");
                        ex.printStackTrace();
                    }
                } else {
                    showError("Tên đăng nhập hoặc mật khẩu không đúng!");
                    // Hiệu ứng rung form
                    shakeForm();
                }
            });
        }).start();
    }

    private User authenticateUser(String username, String password) {
        // 🔧 MODE TEST: Không cần Database, chấp nhận bất kỳ username/password nào
        // Để bật Database thực, hãy comment dòng dưới và uncomment khối try-catch
        
        // ✅ TEST MODE: Tạo user ảo cho testing UI
        if (!username.isEmpty() && !password.isEmpty()) {
            User user = new User();
            user.setUserId(1);
            user.setUserName(username);
            user.setFullName("Test User - " + username);
            user.setRole("admin");
            user.setStatus("active");
            System.out.println("✅ [TEST MODE] Đăng nhập thành công: " + username);
            return user;
        }
        
        // ❌ DATABASE MODE (uncomment nếu có DB)
        /*
        String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND status = 'active' LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUserName(rs.getString("username"));
                user.setFullName(rs.getString("full_name"));
                user.setRole(rs.getString("role"));
                user.setStatus(rs.getString("status"));
                return user;
            }
        } catch (SQLException e) {
            System.out.println("❌ Lỗi xác thực: " + e.getMessage());
        }
        */
        return null;
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        FadeTransition fade = new FadeTransition(Duration.millis(300), errorLabel);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private void shakeForm() {
        javafx.animation.TranslateTransition shake = new javafx.animation.TranslateTransition(Duration.millis(60), formCard);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();
    }
}
