package com.vtea.controller;

import com.vtea.model.User;
import com.vtea.service.EmployeeService;
import com.vtea.utils.DialogHelper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EmployeeFormController {

    @FXML private Label lblFormTitle;
    @FXML private Button btnClose;
    @FXML private TextField txtName;
    @FXML private ComboBox<String> cbRole;
    @FXML private TextField txtPhone;
    @FXML private TextField txtEmail;
    @FXML private ComboBox<String> cbStatus;
    @FXML private Button btnCancel;
    @FXML private Button btnSubmit;

    private final EmployeeService employeeService = new EmployeeService();
    private User editingUser;
    private EmployeeController parentController;

    @FXML
    public void initialize() {
        cbRole.setItems(FXCollections.observableArrayList("ADMIN", "STAFF"));
        cbStatus.setItems(FXCollections.observableArrayList("Active", "Locked"));

        btnClose.setOnAction(e -> closeWindow());
        btnCancel.setOnAction(e -> closeWindow());
        btnSubmit.setOnAction(e -> handleSave());
    }

    public void setEmployee(User user, EmployeeController parent) {
        this.parentController = parent;
        this.editingUser = user;

        if (user == null) {
            lblFormTitle.setText("Thêm nhân viên mới");
            btnSubmit.setText("Thêm nhân viên");
            cbRole.setValue("STAFF");
            cbStatus.setValue("Active");
        } else {
            lblFormTitle.setText("Sửa nhân viên");
            btnSubmit.setText("Lưu thay đổi");
            txtName.setText(user.getFullName());
            txtEmail.setText(user.getUserName());
            txtPhone.setText(user.getUserName());
            cbRole.setValue(user.getRole());
            cbStatus.setValue(user.getStatus());
            txtEmail.setDisable(true);
        }
    }

    private void handleSave() {
        String fullName = txtName.getText() != null ? txtName.getText().trim() : "";
        String username = txtEmail.getText() != null ? txtEmail.getText().trim() : "";
        if (username.isEmpty() && txtPhone.getText() != null) {
            username = txtPhone.getText().trim();
        }
        String role = cbRole.getValue();
        String status = cbStatus.getValue();

        if (fullName.isEmpty() || username.isEmpty()) {
            DialogHelper.showInfo("Cảnh báo", "Vui lòng nhập họ tên và tên đăng nhập!");
            return;
        }

        boolean success;
        if (editingUser == null) {
            success = employeeService.createEmployee(username, fullName, role, status);
            if (success) {
                DialogHelper.showInfo("Thành công",
                        "Đã thêm nhân viên.\nMật khẩu mặc định: " + employeeService.getDefaultPasswordHint());
            } else {
                DialogHelper.showInfo("Lỗi", "Không thể thêm (có thể trùng tên đăng nhập)!");
                return;
            }
        } else {
            editingUser.setFullName(fullName);
            editingUser.setRole(role);
            editingUser.setStatus(EmployeeService.normalizeStatus(status));
            success = employeeService.updateEmployee(editingUser);
            if (!success) {
                DialogHelper.showInfo("Lỗi", "Không thể cập nhật nhân viên!");
                return;
            }
            DialogHelper.showInfo("Thành công", "Đã cập nhật nhân viên!");
        }

        if (parentController != null) {
            parentController.loadEmployees();
        }
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
}
