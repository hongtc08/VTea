package com.vtea.controller;

import com.vtea.model.Ingredient;
import com.vtea.service.InventoryService;
import com.vtea.utils.DialogHelper;
import com.vtea.utils.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class InventoryStaffRowController {

    @FXML private Label lblName;
    @FXML private Label lblUnit;
    @FXML private Label lblSystemQuantity;
    @FXML private TextField txtActualQuantity;
    @FXML private Button btnSave;

    private Ingredient currentIngredient;
    private InventoryController parentController;

    private final InventoryService inventoryService = new InventoryService();

    public void initialize() {
        txtActualQuantity.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                txtActualQuantity.setText(oldValue);
            }
        });
    }

    public void setData(Ingredient ingredient, InventoryController parentController) {
        this.currentIngredient = ingredient;
        this.parentController = parentController;

        lblName.setText(ingredient.getName());
        lblUnit.setText(ingredient.getUnit());

        DecimalFormat format = new DecimalFormat("0.##");
        lblSystemQuantity.setText(format.format(ingredient.getStockQty()));

        txtActualQuantity.clear();
    }

    @FXML
    public void handleSave(ActionEvent actionEvent) {
        if (currentIngredient == null) return;

        String inputStr = txtActualQuantity.getText();
        if (inputStr == null || inputStr.trim().isEmpty()) {
            DialogHelper.showInfo("Lỗi", "Vui lòng nhập số lượng tồn kho thực tế.");
            return;
        }

        try {
            BigDecimal actualQty = new BigDecimal(inputStr.trim());
            BigDecimal systemQty = currentIngredient.getStockQty();
            int staffId = SessionManager.getCurrentUser() != null ? SessionManager.getCurrentUser().getId() : 0;

            boolean success = inventoryService.submitCount(staffId, currentIngredient.getIngredientId(), systemQty, actualQty);

            if (success) {
                if (actualQty.compareTo(systemQty) == 0) {
                    DialogHelper.showInfo("Thành công", "Số lượng khớp 100%. Đã chốt sổ!");
                } else {
                    DialogHelper.showInfo("Thành công", "Đã gửi phiếu báo cáo chênh lệch cho Quản lý duyệt!");
                }

                // Refresh list
                if (parentController != null) {
                    parentController.loadData();
                }
            } else {
                DialogHelper.showInfo("Thất bại", "Không thể chốt tồn kho, vui lòng thử lại.");
            }
        } catch (NumberFormatException e) {
            DialogHelper.showInfo("Lỗi", "Số lượng không hợp lệ.");
        } catch (Exception e) {
            DialogHelper.showInfo("Lỗi", "Lỗi: " + e.getMessage());
        }
    }
}