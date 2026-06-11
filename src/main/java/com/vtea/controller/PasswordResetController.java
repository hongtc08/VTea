package com.vtea.controller;

import com.vtea.model.User;
import com.vtea.service.UserService;
import com.vtea.utils.DialogHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

public class PasswordResetController {

    @FXML private Button btnClose;
    @FXML private PasswordField txtOldPassword;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmPassword;

    private User targetUser;
    private final UserService userService = new UserService();

    public void setTargetUser(User user) {
        this.targetUser = user;
    }

    @FXML
    public void handleSave(ActionEvent event) {
        String oldPass = txtOldPassword.getText();
        String newPass = txtNewPassword.getText();
        String confirmPass = txtConfirmPassword.getText();

        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            DialogHelper.showInfo("Lỗi nhập liệu", "Vui lòng nhập đầy đủ thông tin mật khẩu!");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            DialogHelper.showInfo("Lỗi nhập liệu", "Mật khẩu xác nhận không khớp với mật khẩu mới!");
            return;
        }

        try {
            if (userService.changePassword(targetUser.getUserId(), oldPass, newPass)) {
                DialogHelper.showInfo("Thành công", "Đã cập nhật mật khẩu thành công!");
                closeStage();
            }
        } catch (Exception e) {
            DialogHelper.showInfo("Lỗi", e.getMessage());
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