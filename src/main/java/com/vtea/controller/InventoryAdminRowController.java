package com.vtea.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.javafx.FontIcon;

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
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    // TODO: Backend - Khai báo biến lưu đối tượng IngredientDTO hiện tại

    public void initialize() {
        // Khởi tạo nếu cần
    }

    // TODO: Backend - Viết hàm setData(IngredientDTO ingredient) để đổ dữ liệu vào các Label
    // public void setData(IngredientDTO ingredient) { ... }

    @FXML
    public void handleEdit(ActionEvent actionEvent) {
        // TODO: Backend - Gọi logic mở form chỉnh sửa thông tin nguyên liệu (Name, Unit, MinStock)
    }

    @FXML
    public void handleDelete(ActionEvent actionEvent) {
        // TODO: Backend - Gọi logic xóa (ẩn) nguyên liệu IngredientDAO.deleteIngredient(id)
    }
}
