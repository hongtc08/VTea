package com.vtea.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import javafx.scene.layout.StackPane;

import atlantafx.base.theme.PrimerLight;

public class MainApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        primaryStage = stage;
        primaryStage.setTitle("VTea - POS & Quản lý");
        setRoot("login");
        primaryStage.show();
    }

    private static StackPane rootLayer;
    private static StackPane mainContainer;
    private static StackPane overlayContainer;

    public static void setRoot(String fxml) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("/com/vtea/view/" + fxml + ".fxml"));
            Parent root = fxmlLoader.load();
            
            if (rootLayer == null) {
                mainContainer = new StackPane();
                overlayContainer = new StackPane();
                overlayContainer.setPickOnBounds(false); // Cho phép click xuyên qua lớp overlay nếu không click trúng thông báo
                
                rootLayer = new StackPane(mainContainer, overlayContainer);
                rootLayer.getStylesheets().add(MainApp.class.getResource("/com/vtea/css/styles.css").toExternalForm());
                
                if (primaryStage.getScene() == null) {
                    primaryStage.setScene(new Scene(rootLayer, 1280, 800));
                } else {
                    primaryStage.getScene().setRoot(rootLayer);
                }
            }
            
            mainContainer.getChildren().clear();
            mainContainer.getChildren().add(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static StackPane getOverlayContainer() {
        return overlayContainer;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
