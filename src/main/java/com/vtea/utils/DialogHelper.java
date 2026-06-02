package com.vtea.utils;

import com.vtea.controller.CustomDialogController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
public class DialogHelper {

    // Gọi hàm này để tạo cửa sổ xác nhận (Trả về true/false)
    public static boolean showConfirm(String title, String message) {
        return showDialog(title, message, true);
    }

    // Gọi hàm này để tạo thông báo bình thường (Chỉ có nút Đóng)
    public static void showInfo(String title, String message) {
        showDialog(title, message, false);
    }

    private static boolean showDialog(String title, String message, boolean isConfirmType) {
        try {
            FXMLLoader loader = new FXMLLoader(DialogHelper.class.getResource("/com/vtea/view/CustomDialog.fxml"));
            Parent root = loader.load();

            CustomDialogController controller = loader.getController();
            controller.setDialogData(title, message, isConfirmType);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL); // Khóa màn hình chính khi popup hiện lên
            stage.initStyle(StageStyle.TRANSPARENT); // Làm nền cửa sổ trong suốt

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT); // Xóa nền trắng mặc định của Scene

            stage.setScene(scene);
            stage.showAndWait(); // Dừng luồng chạy ở đây cho đến khi tắt popup

            return controller.isConfirmed();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean showSuccessWithBillButton(String title, String message) {
        final boolean[] exportBill = {false};

        Stage stage = new Stage();
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.initModality(Modality.APPLICATION_MODAL);

        Label iconLabel = new Label("ⓘ");
        iconLabel.setStyle("""
            -fx-font-size: 24px;
            -fx-text-fill: #5a3a2b;
            -fx-background-color: #f8eee9;
            -fx-background-radius: 50;
            -fx-min-width: 48;
            -fx-min-height: 48;
            -fx-max-width: 48;
            -fx-max-height: 48;
            -fx-alignment: center;
            """);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("""
            -fx-font-size: 20px;
            -fx-font-weight: bold;
            -fx-text-fill: #111827;
            """);

        HBox titleBox = new HBox(14, iconLabel, titleLabel);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setStyle("""
            -fx-font-size: 15px;
            -fx-text-fill: #5f6368;
            -fx-line-spacing: 4;
            """);

        Button closeButton = new Button("Đóng");
        closeButton.setPrefWidth(108);
        closeButton.setPrefHeight(40);
        closeButton.setStyle("""
            -fx-background-color: #5a3a2b;
            -fx-text-fill: white;
            -fx-font-size: 14px;
            -fx-font-weight: bold;
            -fx-background-radius: 10;
            -fx-cursor: hand;
            """);

        Button exportButton = new Button("Xuất bill");
        exportButton.setPrefWidth(118);
        exportButton.setPrefHeight(40);
        exportButton.setStyle("""
            -fx-background-color: #2d8cff;
            -fx-text-fill: white;
            -fx-font-size: 14px;
            -fx-font-weight: bold;
            -fx-background-radius: 10;
            -fx-cursor: hand;
            """);

        closeButton.setOnAction(event -> {
            exportBill[0] = false;
            stage.close();
        });

        exportButton.setOnAction(event -> {
            exportBill[0] = true;
            stage.close();
        });

        HBox buttonBox = new HBox(12, closeButton, exportButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(18, titleBox, messageLabel, buttonBox);
        root.setPadding(new Insets(24, 30, 24, 30));
        root.setPrefWidth(480);
        root.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 16;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.20), 18, 0, 0, 6);
            """);

        Scene scene = new Scene(root);
        scene.setFill(null);

        stage.setScene(scene);
        stage.showAndWait();

        return exportBill[0];
    }
}
