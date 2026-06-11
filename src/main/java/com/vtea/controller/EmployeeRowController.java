package com.vtea.controller;

import com.vtea.model.User;
import com.vtea.service.UserService;
import com.vtea.utils.DialogHelper;
import com.vtea.utils.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

public class EmployeeRowController {

    // --- Các biến ánh xạ từ FXML ---
    @FXML private Label lblAvatarText;
    @FXML private Label lblName;
    @FXML private Label lblEmail;

    @FXML private StackPane badgeRole;
    @FXML private Label lblRole;

    @FXML private Label lblPhone;
    @FXML private Label lblSalary;
    @FXML private Label lblStartDate;

    @FXML private StackPane badgeStatus;
    @FXML private Label lblStatus;

    @FXML private Button btnEdit;
    @FXML private Button btnResetPass;
    @FXML private Button btnToggleLock;
    @FXML private FontIcon iconLock;

    // --- Biến cục bộ ---
    private User currentUser;
    private EmployeeController parentController;
    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        btnToggleLock.setOnAction(this::handleToggleLock);
        btnEdit.setOnAction(this::handleEdit);
        btnResetPass.setOnAction(this::handleResetPass);
    }

    /**
     * Hàm nhận dữ liệu từ vòng lặp ngoài truyền vào
     */
    public void setData(User user, EmployeeController parent) {
        this.currentUser = user;
        this.parentController = parent;

        // 1. Thông tin định danh cơ bản
        lblName.setText(user.getFullName());
        lblAvatarText.setText(user.getFullName().substring(0, 1).toUpperCase());
        lblRole.setText("ADMIN".equalsIgnoreCase(user.getRole()) ? "Quản lý" : "Nhân viên");

        // 2. Dữ liệu thật từ DB (Email, Phone, Salary, StartDate)
        lblEmail.setText(user.getEmail() != null && !user.getEmail().isEmpty() ? user.getEmail() : "Chưa cập nhật email");
        lblPhone.setText(user.getPhone() != null && !user.getPhone().isEmpty() ? user.getPhone() : "Chưa cập nhật");

        if (user.getSalary() != null) {
            DecimalFormat df = new DecimalFormat("#,###đ");
            lblSalary.setText(df.format(user.getSalary()));
        } else {
            lblSalary.setText("0đ");
        }

        if (user.getStartDate() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            lblStartDate.setText(user.getStartDate().format(formatter));
        } else {
            lblStartDate.setText("Chưa rõ");
        }

        // 3. Cập nhật giao diện Status và đổi Icon nút Khóa/Mở Khóa
        if (User.STATUS_LOCKED.equalsIgnoreCase(user.getStatus())) {
            // Đang bị khóa
            lblStatus.setText("Đã khóa");
            badgeStatus.setStyle("-fx-background-color: #fee2e2;");
            lblStatus.setStyle("-fx-text-fill: #b91c1c;");

            // Đổi nút thành "Mở khóa" (Màu xanh)
            iconLock.setIconLiteral("fth-unlock");
            iconLock.setIconColor(javafx.scene.paint.Color.valueOf("#10b981"));
        } else {
            // Đang hoạt động
            lblStatus.setText("Đang làm việc");
            badgeStatus.setStyle("-fx-background-color: #d1fae5;");
            lblStatus.setStyle("-fx-text-fill: #047857;");

            // Đổi nút thành "Khóa" (Màu đỏ)
            iconLock.setIconLiteral("fth-lock");
            iconLock.setIconColor(javafx.scene.paint.Color.valueOf("#D93025"));
        }
    }

    // ================= CHỨC NĂNG 1: KHÓA / MỞ KHÓA =================

    private void handleToggleLock(ActionEvent event) {
        if (currentUser == null) return;

        int performerId = SessionManager.getCurrentUser() != null ? SessionManager.getCurrentUser().getId() : 0;

        // Chặn tự khóa chính mình
        if (currentUser.getUserId() == performerId) {
            DialogHelper.showInfo("Từ chối", "Bạn không thể tự khóa tài khoản của chính mình!");
            return;
        }

        try {
            boolean isCurrentlyLocked = User.STATUS_LOCKED.equalsIgnoreCase(currentUser.getStatus());
            boolean success;
            String successMsg;

            if (isCurrentlyLocked) {
                success = userService.unlockAccount(currentUser.getUserId(), performerId);
                successMsg = "Đã mở khóa tài khoản thành công!";
            } else {
                success = userService.lockAccount(currentUser.getUserId(), performerId);
                successMsg = "Đã khóa tài khoản thành công!";
            }

            if (success) {
                DialogHelper.showInfo("Thành công", successMsg);
                if (parentController != null) parentController.loadData(); // Load lại bảng
            }

        } catch (Exception e) {
            DialogHelper.showInfo("Lỗi hệ thống", e.getMessage());
        }
    }

    // ================= CHỨC NĂNG 2: ĐỔI MẬT KHẨU =================

    private void handleResetPass(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vtea/view/PasswordResetForm.fxml"));
            Parent root = loader.load();

            PasswordResetController controller = loader.getController();
            controller.setTargetUser(currentUser);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            DialogHelper.showInfo("Thông báo", "Không thể mở form đổi mật khẩu");
        }
    }

    // ================= CHỨC NĂNG 3: SỬA THÔNG TIN =================

    private void handleEdit(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vtea/view/EmployeeEditForm.fxml"));
            Parent root = loader.load();

            EmployeeEditController controller = loader.getController();
            controller.setEmployeeData(currentUser, parentController);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            DialogHelper.showInfo("Thông báo", "Không thể mở form sửa thông tin nhân viên");
        }
    }
}