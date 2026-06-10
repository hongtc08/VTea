package com.vtea.controller;

import com.vtea.dto.IngredientDTO;
import com.vtea.model.Ingredient;
import com.vtea.service.IngredientService;
import com.vtea.utils.DialogHelper;
import com.vtea.utils.SessionManager;
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
    @FXML private TextField txtMinStock;
    @FXML private ComboBox<String> cbCategory;
    @FXML private Button btnCancel;
    @FXML private Button btnSubmit;

    private IngredientDTO currentIngredient;
    private InventoryController parentController;
    private final IngredientService ingredientService = new IngredientService();

    @FXML
    public void initialize() {
        // Setup initial values
        cbUnit.setItems(FXCollections.observableArrayList("kg", "lít", "hộp", "chai", "gói", "gram", "ml"));

        // Chi cho phep nhap so vao quantity va minStock
        txtQuantity.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                txtQuantity.setText(oldValue);
            }
        });
        txtMinStock.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                txtMinStock.setText(oldValue);
            }
        });

        // Setup button actions
        btnClose.setOnAction(e -> closeForm());
        btnCancel.setOnAction(e -> closeForm());
        btnSubmit.setOnAction(e -> submitForm());
    }

    public void setIngredient(IngredientDTO ingredient, InventoryController parentController) {
        this.parentController = parentController;
        this.currentIngredient = ingredient;

        if (ingredient != null) {
            // Edit mode
            lblFormTitle.setText("Sửa nguyên liệu");
            btnSubmit.setText("Cập nhật");

            txtName.setText(ingredient.getName());
            cbUnit.setValue(ingredient.getUnit());
            txtQuantity.setText(ingredient.getStockQty().toString());
            txtMinStock.setText(ingredient.getMinStock().toString());
        } else {
            // Add mode
            lblFormTitle.setText("Thêm nguyên liệu mới");
            btnSubmit.setText("Thêm mục");
            cbUnit.getSelectionModel().selectFirst();
        }
    }

    private void submitForm() {
        try {
            String name = txtName.getText();
            String unit = cbUnit.getValue();
            String qtyStr = txtQuantity.getText();
            String minStockStr = txtMinStock.getText();

            if (name == null || name.trim().isEmpty() || unit == null) {
                DialogHelper.showInfo("Lỗi", "Vui lòng nhập đầy đủ Tên và Đơn vị.");
                return;
            }

            BigDecimal stockQty = (qtyStr == null || qtyStr.trim().isEmpty()) ? BigDecimal.ZERO : new BigDecimal(qtyStr);
            BigDecimal minStock = (minStockStr == null || minStockStr.trim().isEmpty()) ? BigDecimal.ZERO : new BigDecimal(minStockStr);

            Ingredient item = new Ingredient();
            item.setName(name);
            item.setUnit(unit);
            item.setStockQty(stockQty);
            item.setMinStock(minStock);
            item.setAvailable(true);

            int adminId = SessionManager.getCurrentUser() != null ? SessionManager.getCurrentUser().getId() : 0;
            boolean success = false;

            if (currentIngredient == null) {
                // Add new
                success = ingredientService.addIngredient(item, adminId);
            } else {
                // Edit
                item.setIngredientId(currentIngredient.getIngredientId());
                success = ingredientService.updateIngredientInfo(item, adminId);
            }

            if (success) {
                DialogHelper.showInfo("Thành công", currentIngredient == null ? "Đã thêm nguyên liệu mới!" : "Đã cập nhật nguyên liệu!");
                if (parentController != null) {
                    parentController.loadData();
                }
                closeForm();
            } else {
                DialogHelper.showInfo("Thất bại", "Có lỗi xảy ra khi lưu vào CSDL.");
            }

        } catch (NumberFormatException e) {
            DialogHelper.showInfo("Lỗi", "Vui lòng kiểm tra lại số lượng nhập vào.");
        } catch (IllegalArgumentException e) {
            DialogHelper.showInfo("Lỗi", e.getMessage());
        } catch (Exception e) {
            DialogHelper.showInfo("Lỗi", "Lỗi: " + e.getMessage());
        }
    }

    private void closeForm() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }
}
