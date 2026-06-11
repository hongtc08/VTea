package com.vtea.controller;

import com.vtea.dto.InventoryTransactionDTO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class TransactionRowController {

    @FXML private Label lblTime;
    @FXML private Label lblType;
    @FXML private StackPane badgeType;
    @FXML private Label lblIngredient;
    @FXML private Label lblQuantity;
    @FXML private Label lblAdmin;
    @FXML private Label lblNote;

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void setData(InventoryTransactionDTO tx) {
        if (tx.getCreatedAt() != null) {
            lblTime.setText(tx.getCreatedAt().format(timeFormatter));
        }

        lblIngredient.setText(tx.getIngredientName());
        lblAdmin.setText(tx.getAdminName());
        lblNote.setText(tx.getNote() != null ? tx.getNote() : "");

        String type = tx.getChangeType();
        BigDecimal qty = tx.getQuantityChanged();
        String qtyStr = qty.stripTrailingZeros().toPlainString();

        if (qty.compareTo(BigDecimal.ZERO) > 0) {
            lblQuantity.setText("+" + qtyStr);
            lblQuantity.setStyle("-fx-font-weight: bold; -fx-text-fill: #10b981;");
        } else {
            lblQuantity.setText(qtyStr);
            lblQuantity.setStyle("-fx-font-weight: bold; -fx-text-fill: #ef4444;");
        }

        switch (type) {
            case "IMPORT":
                lblType.setText("Nhập kho");
                badgeType.setStyle("-fx-background-color: #d1fae5; -fx-background-radius: 12; -fx-padding: 4 10;");
                lblType.setStyle("-fx-text-fill: #065f46; -fx-font-weight: bold;");
                break;
            case "EXPORT":
                lblType.setText("Xuất kho");
                badgeType.setStyle("-fx-background-color: #fee2e2; -fx-background-radius: 12; -fx-padding: 4 10;");
                lblType.setStyle("-fx-text-fill: #991b1b; -fx-font-weight: bold;");
                break;
            case "DAMAGE":
                lblType.setText("Hư hỏng");
                badgeType.setStyle("-fx-background-color: #fef08a; -fx-background-radius: 12; -fx-padding: 4 10;");
                lblType.setStyle("-fx-text-fill: #854d0e; -fx-font-weight: bold;");
                break;
            case "ADJUSTMENT":
                lblType.setText("Kiểm kê");
                badgeType.setStyle("-fx-background-color: #dbeafe; -fx-background-radius: 12; -fx-padding: 4 10;");
                lblType.setStyle("-fx-text-fill: #1e40af; -fx-font-weight: bold;");
                break;
            default:
                lblType.setText(type);
        }
    }
}