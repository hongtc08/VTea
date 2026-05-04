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




    private List<ProductMock> allProducts = new ArrayList<>();


    @FXML private FlowPane productGrid;

    // 1. Khai báo các Button từ FXML
    @FXML private Button btnAll;
    @FXML private Button btnTraSua;
    @FXML private Button btnCafe;
    @FXML private Button btnTra;
    @FXML private Button btnDacBiet;
    @FXML
    public void initialize() {
        // Khởi tạo dữ liệu mẫu
        allProducts.add(new ProductMock("Trà sữa trân châu", "Trà sữa", "50.000đ", "/com/vtea/images/cup.png"));
        allProducts.add(new ProductMock("Trà sữa ô long", "Trà sữa", "55.000đ", "/com/vtea/images/cup.png"));
        allProducts.add(new ProductMock("Cafe sữa đá", "Cafe", "35.000đ", "/com/vtea/images/cup.png"));
        allProducts.add(new ProductMock("Bạc xỉu", "Cafe", "35.000đ", "/com/vtea/images/cup.png"));
        allProducts.add(new ProductMock("Trà đào cam sả", "Trà", "45.000đ", "/com/vtea/images/cup.png"));
        allProducts.add(new ProductMock("Trà vải", "Trà", "45.000đ", "/com/vtea/images/cup.png"));
        allProducts.add(new ProductMock("Sữa chua trân châu", "Đặc biệt", "40.000đ", "/com/vtea/images/cup.png"));

        // Mặc định hiển thị tất cả sản phẩm
        displayProducts(allProducts);
    }

    // ==========================================
    // CÁC HÀM XỬ LÝ SỰ KIỆN CLICK BUTTON LỌC
    // ==========================================

    @FXML
    private void filterAll(ActionEvent event) {
        setActiveButton(btnAll);
        displayProducts(allProducts);
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
        List<ProductMock> filteredList = new ArrayList<>();
        for (ProductMock p : allProducts) {
            if (p.category.equals(category)) {
                filteredList.add(p);
            }
        }
        displayProducts(filteredList);
    }

    // Hàm hiển thị danh sách sản phẩm lên giao diện
    private void displayProducts(List<ProductMock> productsToDisplay) {
        productGrid.getChildren().clear(); // Xóa các sản phẩm cũ đi
        for (ProductMock p : productsToDisplay) {
            VBox card = loadProductCard(p.name, p.category, p.price, p.imagePath);
            if (card != null) {
                productGrid.getChildren().add(card);
            }
        }
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
    private VBox loadProductCard(String name, String category, String price, String imagePath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vtea/view/ProductCard.fxml"));
            VBox cardNode = loader.load();

            Label lblProductName = (Label) cardNode.lookup("#lblProductName");
            Label lblCategory = (Label) cardNode.lookup("#lblCategory");
            Label lblPrice = (Label) cardNode.lookup("#lblPrice");
            ImageView imgProduct = (ImageView) cardNode.lookup("#imgProduct");

            lblProductName.setText(name);
            lblCategory.setText(category);
            lblPrice.setText(price);

            try {
                Image image = new Image(getClass().getResourceAsStream(imagePath));
                imgProduct.setImage(image);
            } catch (Exception e) {
                // Bỏ qua nếu không tìm thấy ảnh
            }
            return cardNode;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Class phụ để chứa dữ liệu mẫu (Mock data)
    // Thực tế sau này bạn sẽ dùng Class Product từ DB
    private class ProductMock {
        String name, category, price, imagePath;
        public ProductMock(String name, String category, String price, String imagePath) {
            this.name = name;
            this.category = category;
            this.price = price;
            this.imagePath = imagePath;
        }
    }
}