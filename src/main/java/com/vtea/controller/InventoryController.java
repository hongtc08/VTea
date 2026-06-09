package com.vtea.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class InventoryController {

    @FXML
    private HBox adminHeaderRow;

    @FXML
    private HBox staffHeaderRow;

    @FXML
    private VBox inventoryListContainer;

    @FXML
    private Button btnAddInventory;

    @FXML
    public void initialize() {
        // TODO: Backend - Lấy role của user hiện tại từ SessionManager
        // Ví dụ: String role = SessionManager.getCurrentUser().getRole();
        // Kiểm tra role:
        // Nếu role == "ADMIN":
        //    adminHeaderRow.setVisible(true);
        //    adminHeaderRow.setManaged(true);
        //    staffHeaderRow.setVisible(false);
        //    staffHeaderRow.setManaged(false);
        //    btnAddInventory.setVisible(true);
        //    // TODO: Gọi IngredientDAO.getAllIngredientsForAdmin() và nạp danh sách InventoryAdminRow.fxml vào inventoryListContainer
        // Nếu role == "STAFF":
        //    adminHeaderRow.setVisible(false);
        //    adminHeaderRow.setManaged(false);
        //    staffHeaderRow.setVisible(true);
        //    staffHeaderRow.setManaged(true);
        //    btnAddInventory.setVisible(false);
        //    // TODO: Gọi IngredientDAO.getAllActiveIngredients() và nạp danh sách InventoryStaffRow.fxml vào inventoryListContainer
    }

    @FXML
    public void handleAddInventory(ActionEvent actionEvent) {
        // TODO: Backend - Mở modal/form thêm nguyên liệu mới (chỉ dành cho ADMIN)
    }
}
