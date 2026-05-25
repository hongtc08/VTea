package com.vtea.controller;

import com.vtea.model.Ingredient;
import com.vtea.service.InventoryService;
import com.vtea.utils.DialogHelper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.math.BigDecimal;

public class InventoryFormController {

    @FXML private Label lblFormTitle;
    @FXML private Button btnClose;
    @FXML private TextField txtName;
    @FXML private TextField txtQuantity;
    @FXML private ComboBox<String> cbUnit;
    @FXML private Button btnCancel;
    @FXML private Button btnSubmit;

    private final InventoryService inventoryService = new InventoryService();
    private Ingredient editingIngredient;
    private InventoryController parentController;

    @FXML
    public void initialize() {
        cbUnit.setItems(FXCollections.observableArrayList("kg", "g", "l", "ml", "hộp", "gói"));

        btnClose.setOnAction(e -> closeWindow());
        btnCancel.setOnAction(e -> closeWindow());
        btnSubmit.setOnAction(e -> handleSave());
    }

    public void setIngredient(Ingredient ingredient, InventoryController parent) {
        this.parentController = parent;
        this.editingIngredient = ingredient;

        if (ingredient == null) {
            lblFormTitle.setText("Thêm nguyên liệu mới");
            btnSubmit.setText("Thêm mục");
            cbUnit.setValue("kg");
        } else {
            lblFormTitle.setText("Cập nhật nguyên liệu");
            btnSubmit.setText("Lưu");
            txtName.setText(ingredient.getName());
            txtQuantity.setText(ingredient.getStockQty() != null
                    ? ingredient.getStockQty().stripTrailingZeros().toPlainString()
                    : "0");
            cbUnit.setValue(ingredient.getUnit());
        }
    }

    private void handleSave() {
        String name = txtName.getText() != null ? txtName.getText().trim() : "";
        String unit = cbUnit.getValue() != null ? cbUnit.getValue() : "kg";
        BigDecimal qty = parseDecimal(txtQuantity.getText());

        if (name.isEmpty()) {
            DialogHelper.showInfo("Cảnh báo", "Vui lòng nhập tên nguyên liệu!");
            return;
        }

        boolean success;
        if (editingIngredient == null) {
            success = inventoryService.createIngredient(name, unit, qty);
            if (!success) {
                DialogHelper.showInfo("Lỗi", "Không thể thêm nguyên liệu!");
                return;
            }
            DialogHelper.showInfo("Thành công", "Đã thêm nguyên liệu!");
        } else {
            editingIngredient.setName(name);
            editingIngredient.setUnit(unit);
            success = inventoryService.updateIngredientInfo(editingIngredient);
            if (success && qty != null) {
                success = inventoryService.updateStockQuantity(editingIngredient.getIngredientId(), qty);
            }
            if (!success) {
                DialogHelper.showInfo("Lỗi", "Không thể cập nhật nguyên liệu!");
                return;
            }
            DialogHelper.showInfo("Thành công", "Đã cập nhật nguyên liệu!");
        }

        if (parentController != null) {
            parentController.loadInventory();
        }
        closeWindow();
    }

    private BigDecimal parseDecimal(String text) {
        if (text == null || text.isBlank()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(text.trim().replace(",", ""));
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
}
