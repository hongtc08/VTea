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

public class EmployeeEditController {

    @FXML private Button btnClose;
    @FXML private TextField txtName;
    @FXML private ComboBox<String> cbRole;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;
    @FXML private TextField txtSalary;

    private User currentUser;
    private EmployeeController parentController;
    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        cbRole.setItems(FXCollections.observableArrayList("ADMIN", "STAFF"));

        // Ràng buộc chỉ nhập số cho ô lương
        txtSalary.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtSalary.setText(oldValue);
            }
        });
    }

    public void setEmployeeData(User user, EmployeeController parent) {
        this.currentUser = user;
        this.parentController = parent;

        // Điền dữ liệu cũ lên form
        txtName.setText(user.getFullName());
        cbRole.setValue(user.getRole());
        txtEmail.setText(user.getEmail() != null ? user.getEmail() : "");
        txtPhone.setText(user.getPhone() != null ? user.getPhone() : "");
        txtSalary.setText(user.getSalary() != null ? user.getSalary().toString() : "");
    }

    @FXML
    public void handleSave(ActionEvent event) {
        String fullName = txtName.getText().trim();
        String role = cbRole.getValue();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();
        String salaryStr = txtSalary.getText().trim();

        if (fullName.isEmpty() || role == null) {
            DialogHelper.showInfo("Lỗi nhập liệu", "Vui lòng nhập họ tên và chọn vai trò!");
            return;
        }

        try {
            currentUser.setFullName(fullName);
            currentUser.setRole(role);
            currentUser.setEmail(email.isEmpty() ? null : email);
            currentUser.setPhone(phone.isEmpty() ? null : phone);
            if (!salaryStr.isEmpty()) {
                currentUser.setSalary(new BigDecimal(salaryStr));
            } else {
                currentUser.setSalary(null);
            }

            int performerId = SessionManager.getCurrentUser() != null ? SessionManager.getCurrentUser().getId() : 0;

            if (userService.updateUserInfo(currentUser, performerId)) {
                DialogHelper.showInfo("Thành công", "Đã cập nhật thông tin nhân viên!");
                if (parentController != null) parentController.loadData();
                closeStage();
            }

        } catch (Exception e) {
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