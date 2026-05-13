package com.vtea.controller;

import com.vtea.dto.LoginRequestDTO;
import com.vtea.dto.UserSessionDTO;
import com.vtea.main.MainApp;
import com.vtea.service.AuthService;
import com.vtea.utils.SessionManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;

public class LoginController {

    // 1. Khai báo các thành phần giao diện (Nhớ phải có cờ @FXML)
    @FXML
    private TextField txtUsername; // Ô nhập tài khoản

    @FXML
    private PasswordField txtPassword; // Ô nhập mật khẩu (ẩn ký tự)

    @FXML
    private Button btnLogin; // Nút đăng nhập

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
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!");
            return; // Dừng lại, không chạy xuống dưới nữa
        }

        try {
            // Đóng gói dữ liệu gửi xuống Service
            LoginRequestDTO request = new LoginRequestDTO(username, password);

            // Service sẽ ném lỗi ra nếu sai, còn qua được dòng này là thành công!
            UserSessionDTO sessionInfo = authService.login(request);

            // Lưu thông tin người dùng vào SessionManager
            SessionManager.login(sessionInfo);

            // Hiện thông báo chào mừng
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đăng nhập thành công!\nXin chào: " + sessionInfo.getFullName());

            // TODO: Viết code đóng màn hình Login và mở màn hình POSController ở đây
            System.out.println("Chuẩn bị chuyển sang màn hình chính...");
            MainApp.setRoot("main-layout");
        } catch (Exception e) {
            // Nếu AuthService ném lỗi (sai pass, tài khoản khóa...), hiện Popup báo lỗi
            showAlert(Alert.AlertType.ERROR, "Lỗi Đăng Nhập", e.getMessage());
        }
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
