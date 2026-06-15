package com.vtea.controller;

import com.vtea.dto.IngredientDTO;
import com.vtea.service.IngredientService;
import com.vtea.utils.DialogHelper;
import com.vtea.utils.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

public class InventoryAdminRowController {

    @FXML private Label lblName;
    @FXML private Label lblUnit;
    @FXML private Label lblQuantity;
    @FXML private Label lblMinStock;
    @FXML private Label lblStatus;
    @FXML private Label lblLastUpdate;
    @FXML private Label lblStaffName;
    @FXML private StackPane badgeStatus;
    @FXML private FontIcon iconWarning;
    @FXML private Button btnActions;

    private IngredientDTO currentIngredient;
    private InventoryController parentController;
    private final IngredientService ingredientService = new IngredientService();

    private javafx.scene.control.ContextMenu actionMenu;
    private javafx.scene.control.MenuItem miUpdateStock;
    private javafx.scene.control.MenuItem miEdit;
    private javafx.scene.control.MenuItem miDelete;

    public void initialize() {
        actionMenu = new javafx.scene.control.ContextMenu();
        
        miUpdateStock = new javafx.scene.control.MenuItem("Nhập/Xuất kho");
        FontIcon updateIcon = new FontIcon("fth-plus-square");
        updateIcon.setIconColor(javafx.scene.paint.Color.valueOf("#10b981"));
        miUpdateStock.setGraphic(updateIcon);

        miEdit = new javafx.scene.control.MenuItem("Chỉnh sửa");
        FontIcon editIcon = new FontIcon("fth-edit-2");
        editIcon.setIconColor(javafx.scene.paint.Color.valueOf("#1890ff"));
        miEdit.setGraphic(editIcon);

        miDelete = new javafx.scene.control.MenuItem("Xóa");
        FontIcon deleteIcon = new FontIcon("fth-trash-2");
        deleteIcon.setIconColor(javafx.scene.paint.Color.valueOf("#D93025"));
        miDelete.setGraphic(deleteIcon);

        actionMenu.getItems().addAll(miUpdateStock, miEdit, miDelete);

        miUpdateStock.setOnAction(this::handleUpdateStock);
        miEdit.setOnAction(this::handleEdit);
        miDelete.setOnAction(this::handleDelete);

        btnActions.setOnMouseClicked(e -> {
            actionMenu.show(btnActions, javafx.geometry.Side.BOTTOM, 0, 0);
        });
    }

    public void setData(IngredientDTO ingredient, InventoryController parentController) {
        this.currentIngredient = ingredient;
        this.parentController = parentController;

        lblName.setText(ingredient.getName());
        lblUnit.setText(ingredient.getUnit());

        DecimalFormat format = new DecimalFormat("0.##");
        lblQuantity.setText(format.format(ingredient.getStockQty()));
        lblMinStock.setText(format.format(ingredient.getMinStock()));

        if (!ingredient.isAvailable()) {
            lblStatus.setText("Đã xóa");
            badgeStatus.getStyleClass().setAll("badge-error");
            iconWarning.setVisible(true);
            iconWarning.setManaged(true);
        } else if (ingredient.getStockQty().compareTo(ingredient.getMinStock()) <= 0) {
            lblStatus.setText("Sắp hết");
            badgeStatus.getStyleClass().setAll("badge-warning");
            iconWarning.setVisible(true);
            iconWarning.setManaged(true);
        } else {
            lblStatus.setText("Đủ hàng");
            badgeStatus.getStyleClass().setAll("badge-success");
            iconWarning.setVisible(false);
            iconWarning.setManaged(false);
        }

        if (ingredient.getLastUpdated() != null) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            lblLastUpdate.setText(ingredient.getLastUpdated().format(dtf));
        } else {
            lblLastUpdate.setText("Chưa cập nhật");
        }

        lblStaffName.setText(ingredient.getStaffName() != null ? ingredient.getStaffName() : "N/A");
    }

    @FXML
    public void handleEdit(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vtea/view/InventoryForm.fxml"));
            Parent root = loader.load();

            InventoryFormController controller = loader.getController();
            controller.setIngredient(currentIngredient, parentController);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);

            Scene scene = new Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            stage.setScene(scene);
            
            com.vtea.utils.DialogHelper.applyBlurBackground(true);
            com.vtea.utils.DialogHelper.animateDialog(root);
            stage.showAndWait();
            com.vtea.utils.DialogHelper.applyBlurBackground(false);
        } catch (IOException e) {
            e.printStackTrace();
            DialogHelper.showInfo("Lỗi", "Không thể mở form sửa nguyên liệu: " + e.getMessage());
        }
    }

    @FXML
    public void handleUpdateStock(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vtea/view/StockUpdateForm.fxml"));
            Parent root = loader.load();

            StockUpdateController controller = loader.getController();
            controller.setIngredient(currentIngredient, parentController);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);

            Scene scene = new Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            stage.setScene(scene);
            
            com.vtea.utils.DialogHelper.applyBlurBackground(true);
            com.vtea.utils.DialogHelper.animateDialog(root);
            stage.showAndWait();
            com.vtea.utils.DialogHelper.applyBlurBackground(false);
        } catch (IOException e) {
            e.printStackTrace();
            DialogHelper.showInfo("Lỗi", "Không thể mở form nhập xuất kho.");
        }
    }

    @FXML
    public void handleDelete(ActionEvent actionEvent) {
        if (currentIngredient == null) return;
        boolean confirm = DialogHelper.showConfirm("Xóa nguyên liệu", "Bạn có chắc chắn muốn xóa/ẩn nguyên liệu '" + currentIngredient.getName() + "' không?");
        if (confirm) {
            try {
                int adminId = SessionManager.getCurrentUser() != null ? SessionManager.getCurrentUser().getId() : 0;
                boolean success = ingredientService.deleteIngredient(currentIngredient.getIngredientId(), adminId);
                if (success) {
                    DialogHelper.showInfo("Thành công", "Đã xóa nguyên liệu.");
                    if (parentController != null) {
                        parentController.loadData();
                    }
                } else {
                    DialogHelper.showInfo("Thất bại", "Không thể xóa nguyên liệu.");
                }
            } catch (Exception e) {
                DialogHelper.showInfo("Lỗi", e.getMessage());
            }
        }
    }
}
