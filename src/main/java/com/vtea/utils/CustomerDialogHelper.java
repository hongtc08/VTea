package com.vtea.utils;

import com.vtea.controller.CustomerDialogController;
import com.vtea.dto.CustomerCheckoutResult;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.math.BigDecimal;

public class CustomerDialogHelper {

    private CustomerDialogHelper() {
    }

    public static CustomerCheckoutResult showCheckoutDialog(
            BigDecimal subtotal,
            BigDecimal vat,
            BigDecimal total
    ) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    CustomerDialogHelper.class.getResource("/com/vtea/view/CustomerDialog.fxml")
            );
            Parent root = loader.load();

            CustomerDialogController controller = loader.getController();
            controller.initCheckoutData(subtotal, vat, total);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.setTitle("Thông tin khách hàng");

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            stage.setMinWidth(540);
            stage.setMinHeight(640);
            stage.sizeToScene();
            stage.showAndWait();

            return controller.getResult();
        } catch (IOException e) {
            e.printStackTrace();
            DialogHelper.showInfo("Lỗi", "Không thể mở dialog khách hàng!");
            return CustomerCheckoutResult.cancelled();
        }
    }
}
