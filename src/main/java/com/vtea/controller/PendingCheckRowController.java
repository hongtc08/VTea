package com.vtea.controller;

import com.vtea.dto.InventoryCheckDTO;
import com.vtea.service.InventoryService;
import com.vtea.utils.DialogHelper;
import com.vtea.utils.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class PendingCheckRowController {

    @FXML private Label lblTime;
    @FXML private Label lblIngredient;
    @FXML private Label lblStaff;
    @FXML private Label lblSystemQty;
    @FXML private Label lblActualQty;
    @FXML private Label lblDifference;
    @FXML private Button btnApprove;
    @FXML private Button btnReject;

    private InventoryCheckDTO currentCheck;
    private InventoryController parentController;
    private final InventoryService inventoryService = new InventoryService();
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void setData(InventoryCheckDTO check, InventoryController parent) {
        this.currentCheck = check;
        this.parentController = parent;

        if (check.getCreatedAt() != null) {
            lblTime.setText(check.getCreatedAt().format(timeFormatter));
        }
        lblIngredient.setText(check.getIngredientName());
        lblStaff.setText(check.getStaffName());
        lblSystemQty.setText(check.getSystemQty().stripTrailingZeros().toPlainString());
        lblActualQty.setText(check.getActualQty().stripTrailingZeros().toPlainString());

        BigDecimal diff = check.getDifference();
        String diffStr = diff.stripTrailingZeros().toPlainString();

        if (diff.compareTo(BigDecimal.ZERO) > 0) {
            lblDifference.setText("+" + diffStr);
            lblDifference.setStyle("-fx-font-weight: bold; -fx-text-fill: #10b981;");
        } else {
            lblDifference.setText(diffStr);
            lblDifference.setStyle("-fx-font-weight: bold; -fx-text-fill: #ef4444;");
        }
    }

    @FXML
    public void handleApprove(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog("Duyệt chênh lệch kiểm kho");
        dialog.setTitle("Xác nhận duyệt");
        dialog.setHeaderText("Duyệt phiếu kiểm kê của nhân viên: " + currentCheck.getStaffName());
        dialog.setContentText("Ghi chú (Tùy chọn):");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                int adminId = SessionManager.getCurrentUser().getId();
                if (inventoryService.approveCheck(currentCheck.getLogId(), adminId, result.get())) {
                    DialogHelper.showInfo("Thành công", "Đã duyệt và cập nhật kho chính thức!");
                    if (parentController != null) {
                        parentController.loadData();
                        parentController.loadPendingChecks();
                    }
                }
            } catch (Exception e) {
                DialogHelper.showInfo("Lỗi", e.getMessage());
            }
        }
    }

    @FXML
    public void handleReject(ActionEvent event) {
        try {
            int adminId = SessionManager.getCurrentUser().getId();
            if (inventoryService.rejectCheck(currentCheck.getLogId(), adminId)) {
                DialogHelper.showInfo("Thành công", "Đã từ chối phiếu kiểm kê này!");
                if (parentController != null) {
                    parentController.loadData();
                    parentController.loadPendingChecks();
                }
            }
        } catch (Exception e) {
            DialogHelper.showInfo("Lỗi", e.getMessage());
        }
    }
}