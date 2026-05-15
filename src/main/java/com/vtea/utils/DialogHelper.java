package com.vtea.utils;

import com.vtea.controller.CustomDialogController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
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
}