package com.vtea.controller;

import com.vtea.model.Ingredient;
import com.vtea.service.InventoryService;
import com.vtea.utils.DialogHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class InventoryController {

    @FXML private TextField searchField;
    @FXML private VBox inventoryListContainer;

    private final InventoryService inventoryService = new InventoryService();
    private List<Ingredient> allIngredients;

    @FXML
    public void initialize() {
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> renderInventoryList());
        }
        loadInventory();
    }

    public void loadInventory() {
        allIngredients = inventoryService.getAllIngredients();
        renderInventoryList();
    }

    private void renderInventoryList() {
        if (inventoryListContainer == null) {
            return;
        }
        inventoryListContainer.getChildren().clear();

        String keyword = searchField != null && searchField.getText() != null
                ? searchField.getText().trim().toLowerCase()
                : "";

        for (Ingredient item : allIngredients) {
            if (!keyword.isEmpty() && (item.getName() == null || !item.getName().toLowerCase().contains(keyword))) {
                continue;
            }
            HBox row = loadInventoryRow(item);
            if (row != null) {
                inventoryListContainer.getChildren().add(row);
            }
        }

        if (inventoryListContainer.getChildren().isEmpty()) {
            inventoryListContainer.getChildren().add(new Label("Không có nguyên liệu nào"));
        }
    }

    private HBox loadInventoryRow(Ingredient item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vtea/view/InventoryRow.fxml"));
            HBox row = loader.load();

            BigDecimal qty = item.getStockQty() != null ? item.getStockQty() : BigDecimal.ZERO;
            String unit = item.getUnit() != null ? item.getUnit() : "";

            ((Label) row.lookup("#lblName")).setText(item.getName());
            ((Label) row.lookup("#lblCategory")).setText("Nguyên liệu");
            ((Label) row.lookup("#lblQuantity")).setText(qty.stripTrailingZeros().toPlainString() + " " + unit);
            ((Label) row.lookup("#lblMinStock")).setText("—");
            ((Label) row.lookup("#lblStatus")).setText(qty.compareTo(BigDecimal.ZERO) > 0 ? "Đủ hàng" : "Hết hàng");
            ((Label) row.lookup("#lblLastUpdate")).setText(LocalDate.now().toString());

            Button btnEdit = (Button) row.lookup("#btnEdit");
            Button btnDelete = (Button) row.lookup("#btnDelete");

            if (btnEdit != null) {
                btnEdit.setOnAction(e -> openInventoryForm(item));
            }
            if (btnDelete != null) {
                btnDelete.setOnAction(e -> deleteIngredient(item));
            }

            return row;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @FXML
    public void handleAddInventory(ActionEvent actionEvent) {
        openInventoryForm(null);
    }

    private void openInventoryForm(Ingredient item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vtea/view/InventoryForm.fxml"));
            Parent root = loader.load();

            InventoryFormController controller = loader.getController();
            controller.setIngredient(item, this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            DialogHelper.showInfo("Lỗi", "Không thể mở form kho!");
        }
    }

    private void deleteIngredient(Ingredient item) {
        if (inventoryService.deleteIngredient(item.getIngredientId())) {
            DialogHelper.showInfo("Thành công", "Đã ngừng sử dụng nguyên liệu: " + item.getName());
            loadInventory();
        } else {
            DialogHelper.showInfo("Lỗi", "Không thể xóa nguyên liệu!");
        }
    }
}
