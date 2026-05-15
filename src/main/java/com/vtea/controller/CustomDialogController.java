package com.vtea.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

public class CustomDialogController {

    @FXML private Label lblTitle;
    @FXML private Label lblMessage;
    @FXML private Button btnCancel;
    @FXML private Button btnConfirm;
    @FXML private FontIcon iconDialog;

    private boolean isConfirmed = false;

    // Hàm thiết lập dữ liệu trước khi hộp thoại hiện lên
    public void setDialogData(String title, String message, boolean isConfirmType) {
        lblTitle.setText(title);
        lblMessage.setText(message);

        if (isConfirmType) {
            // Nếu là dạng câu hỏi (Xác nhận/Hủy)
            iconDialog.setIconLiteral("fth-help-circle");
            btnCancel.setVisible(true);
            btnCancel.setManaged(true);
        } else {
            // Nếu chỉ là thông báo (Chỉ có nút OK)
            iconDialog.setIconLiteral("fth-info");
            btnCancel.setVisible(false); // Ẩn nút hủy
            btnCancel.setManaged(false);
            btnConfirm.setText("Đóng");
        }
    }

    @FXML
    private void handleConfirm(ActionEvent event) {
        isConfirmed = true;
        closeDialog();
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        isConfirmed = false;
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) btnConfirm.getScene().getWindow();
        stage.close();
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }
}