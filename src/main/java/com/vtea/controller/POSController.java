package com.vtea.controller;

import com.vtea.service.OrderService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class POSController {

    private OrderService orderService = new OrderService();

    public void handleAddToCart(int productId, String productName, double price) {
        orderService.addToCart(productId, productName, BigDecimal.valueOf(price), 1);

        System.out.println("Đã thêm " + productName + " vào giỏ!");
        System.out.println("Tổng tiền hiện tại: " + orderService.getCurrentOrder().getTotalAmount());

    }

    @javafx.fxml.FXML
    private void handleClearCart(javafx.event.ActionEvent event) {
        System.out.println("Clear cart clicked");
    }

    @javafx.fxml.FXML
    private void handleCheckout(javafx.event.ActionEvent event) {
        System.out.println("Checkout clicked");
    }



    @FXML private FlowPane productGrid;

    // 1. Khai báo các Button từ FXML
    @FXML private Button btnAll;
    @FXML private Button btnTraSua;
    @FXML private Button btnCafe;
    @FXML private Button btnTra;
    @FXML private Button btnDacBiet;
    @FXML

    // ==========================================
    // HÀM INIT
    // ==========================================
    public void initialize() {
    }

    // ==========================================
    // CÁC HÀM XỬ LÝ SỰ KIỆN CLICK BUTTON LỌC (Phần này giữ để dùng luôn)
    // ==========================================

    @FXML
    private void filterAll(ActionEvent event) {

    }

    @FXML
    private void filterTraSua(ActionEvent event) {
        setActiveButton(btnTraSua);
        filterByCategory("Trà sữa");
    }

    @FXML
    private void filterCafe(ActionEvent event) {
        setActiveButton(btnCafe);
        filterByCategory("Cafe");
    }

    @FXML
    private void filterTra(ActionEvent event) {
        setActiveButton(btnTra);
        filterByCategory("Trà");
    }

    @FXML
    private void filterDacBiet(ActionEvent event) {
        setActiveButton(btnDacBiet);
        filterByCategory("Đặc biệt");
    }

    // ==========================================
    // LOGIC LỌC VÀ HIỂN THỊ
    // ==========================================

    // Hàm lọc danh sách theo danh mục
    private void filterByCategory(String category) {
    }

    // Hàm hiển thị danh sách sản phẩm lên giao diện
    private void displayProducts() {

    }

    // Hàm đổi màu nút đang được chọn
    private void setActiveButton(Button clickedButton) {
        // Tạo mảng chứa tất cả các nút
        Button[] allButtons = {btnAll, btnTraSua, btnCafe, btnTra, btnDacBiet};

        for (Button btn : allButtons) {
            if (btn != null) {
                // Gỡ bỏ class active khỏi tất cả các nút
                btn.getStyleClass().remove("category-btn-active");
            }
        }
        // Thêm class active vào nút vừa được click
        clickedButton.getStyleClass().add("category-btn-active");
    }

    // ==========================================
    // HÀM LOAD FXML CARD SẢN PHẨM (Đã có từ trước)
    // ==========================================

    // ==========================================
    // LOGIC THÊM VÀO GIỎ HÀNG
    // ==========================================

    // ==========================================
    // TÍNH TOÁN VÀ CẬP NHẬT TỔNG TIỀN
    // ==========================================

    // ==========================================
    // LOGIC LOAD PRODUCT CARD (Đã sửa để nhận Click)
    // ==========================================
}