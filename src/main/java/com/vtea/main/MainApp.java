package com.vtea.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class MainApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        primaryStage.setTitle("VTea - Cafe & Trà Sữa");
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(680);
        primaryStage.setWidth(1280);
        primaryStage.setHeight(800);

        navigateTo("/com/vtea/view/login.fxml");
        primaryStage.show();
    }

    /**
     * Chuyển màn hình toàn bộ stage sang FXML mới
     */
    public static void navigateTo(String fxmlPath) throws IOException {
        Parent root = FXMLLoader.load(
                Objects.requireNonNull(MainApp.class.getResource(fxmlPath))
        );
        Scene scene = new Scene(root);
        scene.getStylesheets().add(
                Objects.requireNonNull(MainApp.class.getResource("/com/vtea/css/styles.css")).toExternalForm()
        );
        primaryStage.setScene(scene);
    }

    /**
     * Lấy Stage chính để các Controller có thể dùng
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
