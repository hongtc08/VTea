package com.vtea.utils;

import com.vtea.main.MainApp;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

public class SnackbarHelper {

    public static final String SUCCESS = "success";
    public static final String ERROR = "error";
    public static final String WARNING = "warning";
    public static final String INFO = "info";

    public static void showSnackbar(String type, String message) {
        Platform.runLater(() -> {
            StackPane overlayContainer = MainApp.getOverlayContainer();
            // Nếu chưa có overlayContainer (ví dụ do lỗi chưa khởi tạo), fallback sang DialogHelper
            if (overlayContainer == null) {
                DialogHelper.showInfo("Thông báo", message);
                return;
            }

            // Nếu container chưa có VBox chứa snackbar, tạo mới
            VBox snackbarContainer = (VBox) overlayContainer.lookup("#snackbarContainer");
            if (snackbarContainer == null) {
                snackbarContainer = new VBox();
                snackbarContainer.setId("snackbarContainer");
                snackbarContainer.setAlignment(Pos.BOTTOM_CENTER);
                snackbarContainer.setSpacing(10);
                snackbarContainer.setPickOnBounds(false); // Cho phép click xuyên qua
                // Để cách đáy màn hình một chút
                StackPane.setMargin(snackbarContainer, new javafx.geometry.Insets(0, 0, 30, 0));
                overlayContainer.getChildren().add(snackbarContainer);
            }

            // Tạo cấu trúc Snackbar
            VBox snackbarBox = new VBox();
            snackbarBox.getStyleClass().addAll("snackbar-box", "snackbar-" + type);
            // Kích thước tối đa vừa phải để không choán hết màn hình
            snackbarBox.setMaxWidth(350);

            HBox contentBox = new HBox();
            contentBox.getStyleClass().add("snackbar-content");

            FontIcon icon = new FontIcon();
            icon.getStyleClass().add("snackbar-icon");
            switch (type) {
                case SUCCESS: icon.setIconLiteral("fth-check-circle"); break;
                case ERROR: icon.setIconLiteral("fth-alert-circle"); break;
                case WARNING: icon.setIconLiteral("fth-alert-triangle"); break;
                default: icon.setIconLiteral("fth-info"); break;
            }

            Label messageLabel = new Label(message);
            messageLabel.getStyleClass().add("snackbar-text");
            messageLabel.setWrapText(true);

            contentBox.getChildren().addAll(icon, messageLabel);

            // Tạo thanh tiến trình (Progress Bar)
            ProgressBar progressBar = new ProgressBar(1.0);
            progressBar.getStyleClass().add("snackbar-progress");
            progressBar.setMaxWidth(Double.MAX_VALUE);
            progressBar.setPrefHeight(4);

            snackbarBox.getChildren().addAll(contentBox, progressBar);

            final VBox containerRef = snackbarContainer;

            // Giới hạn tối đa 3 thông báo cùng lúc
            // Vòng lặp while đề phòng trường hợp vì lý do nào đó có hơn 3 cái
            while (containerRef.getChildren().size() >= 3) {
                // Cái cũ nhất sẽ nằm ở index 0 (trên cùng), xóa ngay lập tức
                containerRef.getChildren().remove(0);
            }

            // Thêm vào container (sẽ xếp chồng từ dưới lên do VBox Alignment BOTTOM_CENTER)
            containerRef.getChildren().add(snackbarBox);

            // Animation hiện (Trượt từ dưới lên + Fade In)
            snackbarBox.setTranslateY(30);
            snackbarBox.setOpacity(0);
            
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), snackbarBox);
            slideIn.setToY(0);
            
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), snackbarBox);
            fadeIn.setToValue(1.0);
            
            slideIn.play();
            fadeIn.play();

            // Animation thanh tiến trình (Chạy lùi 3 giây)
            Timeline progressTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(progressBar.progressProperty(), 1.0)),
                new KeyFrame(Duration.millis(3000), new KeyValue(progressBar.progressProperty(), 0.0))
            );

            // Animation ẩn (Trượt xuống + Fade Out) sau khi progress bar chạy xong
            progressTimeline.setOnFinished(e -> {
                TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), snackbarBox);
                slideOut.setToY(30);
                
                FadeTransition fadeOut = new FadeTransition(Duration.millis(300), snackbarBox);
                fadeOut.setToValue(0.0);
                
                slideOut.setOnFinished(ev -> containerRef.getChildren().remove(snackbarBox));
                
                slideOut.play();
                fadeOut.play();
            });

            progressTimeline.play();
        });
    }
}
