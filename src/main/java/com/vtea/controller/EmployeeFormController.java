package com.vtea.controller;

import com.vtea.model.User;
import com.vtea.service.UserService;
import com.vtea.utils.DialogHelper;
import com.vtea.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;

public class EmployeeFormController {

    @FXML private Label lblFormTitle;
    @FXML private Button btnClose;
    @FXML private TextField txtName;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<String> cbRole;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;
    @FXML private TextField txtSalary;
    @FXML private DatePicker dpStartDate;

    @FXML private Button btnCancel;
    @FXML private Button btnSubmit;

    private final UserService userService = new UserService();
    private EmployeeController parentController;

    @FXML
    public void initialize() {
        cbRole.setItems(FXCollections.observableArrayList("ADMIN", "STAFF"));

        // Gán sẵn ngày hôm nay cho DatePicker
        dpStartDate.setValue(LocalDate.now());

        // Bắt lỗi nhập liệu: Ô lương chỉ cho phép nhập số
        txtSalary.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtSalary.setText(oldValue);
            }
        });
    }

    public void setParentController(EmployeeController parentController) {
        this.parentController = parentController;
    }

    @FXML
    public void handleSave(ActionEvent event) {
        // 1. Lấy dữ liệu từ Form
        String fullName = txtName.getText().trim();
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();
        String role = cbRole.getValue();

        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();
        String salaryStr = txtSalary.getText().trim();
        LocalDate startDate = dpStartDate.getValue();

        // 2. Validate các trường BẮT BUỘC (Có dấu *)
        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty() || role == null) {
            DialogHelper.showInfo("Lỗi nhập liệu", "Vui lòng điền đầy đủ 4 trường thông tin bắt buộc!");
            return;
        }

        try {
            // 3. Đóng gói toàn bộ vào Model
            User newStaff = new User();
            newStaff.setFullName(fullName);
            newStaff.setUserName(username);
            newStaff.setPassWord(password);
            newStaff.setRole(role);

            // Gán dữ liệu mới (Cho phép rỗng nếu quản lý không nhập)
            newStaff.setEmail(email.isEmpty() ? null : email);
            newStaff.setPhone(phone.isEmpty() ? null : phone);
            newStaff.setStartDate(startDate);

            if (!salaryStr.isEmpty()) {
                newStaff.setSalary(new BigDecimal(salaryStr));
            }

            // 4. Xác định người thực hiện
            int performerId = 0;
            if (SessionManager.getCurrentUser() != null) {
                performerId = SessionManager.getCurrentUser().getId();
            } else {
                DialogHelper.showInfo("Lỗi bảo mật", "Hết phiên đăng nhập. Vui lòng đăng nhập lại!");
                return;
            }

            // 5. Lưu xuống Database
            boolean isSuccess = userService.createNewStaff(newStaff, performerId);

            if (isSuccess) {
                DialogHelper.showInfo("Thành công", "Đã tạo tài khoản mới thành công cho: " + fullName);
                if (parentController != null) parentController.loadData();
                closeStage();
            } else {
                DialogHelper.showInfo("Thất bại", "Không thể tạo tài khoản do lỗi hệ thống.");
            }

        } catch (Exception e) {
            // Hứng lỗi trùng Username, Email, SĐT từ UserService ném lên
            DialogHelper.showInfo("Cảnh báo", e.getMessage());
        }
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }
}