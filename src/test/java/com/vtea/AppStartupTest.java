package com.vtea;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AppStartupTest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        URL fxml = getClass().getResource("/com/vtea/view/login.fxml");

        assertNotNull(fxml, "Không tìm thấy file login.fxml");

        Parent root = FXMLLoader.load(fxml);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    void appShouldStartSuccessfully() {
        assertNotNull(window(".*"), "App không mở được window JavaFX");
    }
}