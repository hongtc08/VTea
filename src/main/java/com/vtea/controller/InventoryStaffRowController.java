package com.vtea.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class InventoryStaffRowController {

    @FXML private Label lblName;
    @FXML private Label lblUnit;
    @FXML private Label lblSystemQuantity;
    @FXML private TextField txtActualQuantity;
    @FXML private Button btnSave;

    // TODO: Backend - Khai báo biến lưu đối tượng Ingredient hiện tại

    public void initialize() {
        // TODO: Backend - Thêm listener cho txtActualQuantity để chỉ cho phép nhập số nếu cần
    }

    // TODO: Backend - Viết hàm setData(Ingredient ingredient) để đổ dữ liệu vào các Label
    // public void setData(Ingredient ingredient) { ... }

    @FXML
    public void handleSave(ActionEvent actionEvent) {
        // TODO: Backend - Lấy giá trị từ txtActualQuantity (Tồn kho thực tế)
        // TODO: Backend - Lấy id người dùng hiện tại SessionManager.getCurrentUser().getId()
        // TODO: Backend - Gọi hàm IngredientDAO.updateActualQuantity() để lưu xuống database
        // TODO: Backend - Hiển thị thông báo lưu thành công và cập nhật lại lblSystemQuantity
    }
}
