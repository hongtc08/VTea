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

    // Gọi hàm này để tạo thông báo nổi (Snackbar) thay vì hộp thoại chèn ép màn hình
    public static void showInfo(String title, String message) {
        String type = SnackbarHelper.INFO;
        String lowerTitle = title.toLowerCase();
        
        // Tự động phân tích Title để gán màu sắc phù hợp cho Snackbar
        if (lowerTitle.contains("lỗi") || lowerTitle.contains("thất bại")) {
            type = SnackbarHelper.ERROR;
        } else if (lowerTitle.contains("thành công")) {
            type = SnackbarHelper.SUCCESS;
        } else if (lowerTitle.contains("cảnh báo")) {
            type = SnackbarHelper.WARNING;
        }
        
        SnackbarHelper.showSnackbar(type, message);
    }

    private static boolean showDialog(String title, String message, boolean isConfirmType) {
        try {
            FXMLLoader loader = new FXMLLoader(DialogHelper.class.getResource("/com/vtea/view/CustomDialog.fxml"));
            Parent root = loader.load();

            CustomDialogController controller = loader.getController();
            controller.setDialogData(title, message, isConfirmType, false);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL); // Khóa màn hình chính khi popup hiện lên
            stage.initStyle(StageStyle.TRANSPARENT); // Làm nền cửa sổ trong suốt

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT); // Xóa nền trắng mặc định của Scene

            stage.setScene(scene);
            
            applyBlurBackground(true);
            animateDialog(root);
            try {
                stage.showAndWait();
            } finally {
                applyBlurBackground(false);
            }

            return controller.isConfirmed();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Gọi hàm này khi cần nhập liệu trong hộp thoại xác nhận (Trả về Optional chứa nội dung nhập, hoặc empty nếu Hủy)
    public static java.util.Optional<String> showInputConfirm(String title, String message) {
        try {
            FXMLLoader loader = new FXMLLoader(DialogHelper.class.getResource("/com/vtea/view/CustomDialog.fxml"));
            Parent root = loader.load();

            CustomDialogController controller = loader.getController();
            controller.setDialogData(title, message, true, true);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);

            stage.setScene(scene);
            
            applyBlurBackground(true);
            animateDialog(root);
            try {
                stage.showAndWait();
            } finally {
                applyBlurBackground(false);
            }

            if (controller.isConfirmed()) {
                String input = controller.getInputResult() != null ? controller.getInputResult().trim() : "";
                return java.util.Optional.of(input);
            }
            return java.util.Optional.empty();
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Optional.empty();
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
        applyBlurBackground(true);
        animateDialog(root);
        try {
            stage.showAndWait();
        } finally {
            applyBlurBackground(false);
        }

        return exportBill[0];
    }

    public static void applyBlurBackground(boolean apply) {
        if (com.vtea.main.MainApp.getRootLayer() != null) {
            if (apply) {
                javafx.scene.effect.GaussianBlur blur = new javafx.scene.effect.GaussianBlur(12);
                javafx.scene.effect.ColorAdjust dim = new javafx.scene.effect.ColorAdjust();
                dim.setBrightness(-0.3);
                blur.setInput(dim);
                com.vtea.main.MainApp.getRootLayer().setEffect(blur);
            } else {
                com.vtea.main.MainApp.getRootLayer().setEffect(null);
            }
        }
    }

    public static void animateDialog(javafx.scene.Node root) {
        if (root == null) return;
        
        root.setOpacity(0);
        root.setScaleX(0.8);
        root.setScaleY(0.8);

        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(javafx.util.Duration.millis(250), root);
        fade.setFromValue(0);
        fade.setToValue(1);

        javafx.animation.ScaleTransition scale = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(250), root);
        scale.setFromX(0.8);
        scale.setFromY(0.8);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.setInterpolator(javafx.animation.Interpolator.EASE_OUT);

        javafx.animation.ParallelTransition pt = new javafx.animation.ParallelTransition(fade, scale);
        pt.play();
    }
}
