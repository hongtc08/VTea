package com.vtea.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class InvoiceDetailDialogController {

    // 1. Khai báo 2 nút đóng cửa sổ (Trên và Dưới)
    @FXML private Button btnCloseTop;
    @FXML private Button btnCloseBottom;

    // Các nhãn thông tin chung
    @FXML private Label lblDetailId;
    @FXML private Label lblDetailDateTime;
    @FXML private Label lblDetailEmployee;
    @FXML private Label lblDetailCustomer;
    @FXML private Label lblDetailPhone;

    // Khu vực chứa danh sách món nước
    @FXML private VBox productItemsBox;

    // Các nhãn tính tiền
    @FXML private Label lblSubTotal;
    @FXML private Label lblVat;
    @FXML private Label lblDiscount;
    @FXML private Label lblTotal;

    @FXML
    public void initialize() {
        // ==========================================
        // GẮN SỰ KIỆN ĐÓNG CỬA SỔ VÀO 2 NÚT BẤM
        // ==========================================
        btnCloseTop.setOnAction(e -> closeDialog());
        btnCloseBottom.setOnAction(e -> closeDialog());
    }

    // ==========================================
    // HÀM ĐÓNG DIALOG CHI TIẾT HÓA ĐƠN
    // ==========================================
    private void closeDialog() {
        // Lấy sân khấu (Stage) hiện tại và ra lệnh đóng
        Stage stage = (Stage) btnCloseTop.getScene().getWindow();
        stage.close();
    }

    // Hàm nhận dữ liệu từ màn hình Lịch sử Hóa đơn truyền sang
    public void setInvoiceData(Object invoice) {
        // TODO: Đổ dữ liệu thật vào đây
        // lblDetailId.setText(...);
    }
}