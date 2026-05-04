package com.vtea.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        primaryStage.setTitle("VTea - POS & Quản lý");
        setRoot("login");
        primaryStage.show();
    }

    public static void setRoot(String fxml) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("/com/vtea/view/" + fxml + ".fxml"));
            Parent root = fxmlLoader.load();
            if (primaryStage.getScene() == null) {
                primaryStage.setScene(new Scene(root, 1280, 800));
            } else {
                primaryStage.getScene().setRoot(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
