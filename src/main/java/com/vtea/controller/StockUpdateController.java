package com.vtea.controller;

import com.vtea.dto.IngredientDTO;
import com.vtea.service.InventoryService;
import com.vtea.utils.DialogHelper;
import com.vtea.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class StockUpdateController {

    @FXML private Label lblTitle;
    @FXML private TextField txtCurrentStock;
    @FXML private TextField txtUnit;
    @FXML private ComboBox<String> cboChangeType;
    @FXML private TextField txtQuantity;
    @FXML private TextField txtNote;
    @FXML private Button btnCancel;

    private IngredientDTO currentIngredient;
    private InventoryController parentController;

    private final InventoryService inventoryService = new InventoryService();

    @FXML
    public void initialize() {
        cboChangeType.setItems(FXCollections.observableArrayList(
                "Nhập kho (Cộng thêm)",
                "Xuất kho (Trừ đi)",
                "Hư hỏng/Hao hụt (Trừ đi)"
        ));
    }

    // Hàm nhận dữ liệu từ màn hình cha truyền sang
    public void setIngredient(IngredientDTO ingredient, InventoryController parent) {
        this.currentIngredient = ingredient;
        this.parentController = parent;

        lblTitle.setText("Điều chỉnh kho: " + ingredient.getName());
        txtUnit.setText(ingredient.getUnit());

        DecimalFormat format = new DecimalFormat("0.##");
        txtCurrentStock.setText(format.format(ingredient.getStockQty()));
    }

    @FXML
    public void handleSave(ActionEvent event) {
        if (currentIngredient == null) return;

        String selectedType = cboChangeType.getValue();
        String quantityStr = txtQuantity.getText().trim();
        String note = txtNote.getText().trim();

        if (selectedType == null || quantityStr.isEmpty()) {
            DialogHelper.showInfo("Lỗi", "Vui lòng chọn loại giao dịch và nhập số lượng!");
            return;
        }

        try {
            BigDecimal qty = new BigDecimal(quantityStr);
            if (qty.compareTo(BigDecimal.ZERO) <= 0) {
                DialogHelper.showInfo("Lỗi", "Số lượng phải lớn hơn 0!");
                return;
            }

            String typeCode = "IMPORT";
            if (selectedType.contains("Xuất kho")) typeCode = "EXPORT";
            else if (selectedType.contains("Hư hỏng")) typeCode = "DAMAGE";

            int adminId = SessionManager.getCurrentUser() != null ? SessionManager.getCurrentUser().getId() : 0;
            boolean success = false;

            if ("IMPORT".equals(typeCode)) {
                success = inventoryService.importStock(adminId, currentIngredient.getIngredientId(), qty, note);
            } else {
                String finalNote = (note.isEmpty()) ? typeCode : typeCode + " - " + note;
                success = inventoryService.exportStock(adminId, currentIngredient.getIngredientId(), qty, finalNote);
            }

            if (success) {
                DialogHelper.showInfo("Thành công", "Đã lưu lịch sử và cập nhật kho!");
                if (parentController != null) {
                    parentController.loadData();
                }
                closeStage();
            } else {
                DialogHelper.showInfo("Thất bại", "Có lỗi xảy ra khi lưu giao dịch.");
            }

        } catch (NumberFormatException e) {
            DialogHelper.showInfo("Lỗi", "Số lượng không hợp lệ. Vui lòng nhập số.");
        } catch (Exception e) {
            DialogHelper.showInfo("Lỗi hệ thống", e.getMessage());
        }
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
}