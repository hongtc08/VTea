package com.vtea.controller;

import com.vtea.dto.ProductDTO;
import com.vtea.dto.OrderDetailDTO;
import com.vtea.model.Order;
import com.vtea.service.OrderService;
import com.vtea.service.ProductService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class POSController {

    private OrderService orderService = new OrderService();
    private ProductService productService = new ProductService();
    private int currentUserId = 1;

    @FXML private FlowPane productGrid;
    @FXML private Button btnAll, btnTraSua, btnCafe, btnTra, btnDacBiet;
    @FXML private TableView<OrderDetailDTO> cartTableView;
    @FXML private Label lblTotalAmount;
    @FXML private ComboBox<String> cmbPaymentMethod;

    @FXML
    public void initialize() {
        try {
            loadProductsFromDatabase();
            cmbPaymentMethod.setItems(FXCollections.observableArrayList("Tiền mặt", "Thẻ ghi nợ", "QR Pay"));
            cmbPaymentMethod.setValue("Tiền mặt");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadProductsFromDatabase() {
        try {
            List<ProductDTO> products = productService.getAllActiveProducts();
            displayProducts(products);
        } catch (Exception e) {
            showErrorAlert("Lỗi", "Không thể tải danh sách sản phẩm!");
        }
    }

    public void handleAddToCart(int productId, String productName, BigDecimal price) {
        try {
            orderService.addToCart(productId, productName, price, 1);
            updateCartDisplay();
            updateTotalAmount();
            showSuccessAlert("Thêm vào giỏ", productName + " ✓\nGiá: " + formatPrice(price));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClearCart(ActionEvent event) {
        if (orderService.getCartItems().isEmpty()) {
            showInfoAlert("Thông báo", "Giỏ hàng đã trống!");
            return;
        }
        if (showConfirmDialog("Xác nhận", "Bạn có chắc muốn xóa toàn bộ giỏ hàng?")) {
            orderService = new OrderService();
            updateCartDisplay();
            updateTotalAmount();
            showInfoAlert("Thành công", "Giỏ hàng đã được xóa!");
        }
    }

    @FXML
    private void handleCheckout(ActionEvent event) {
        try {
            if (orderService.getCartItems().isEmpty()) {
                showErrorAlert("Lỗi", "Giỏ hàng trống! Vui lòng thêm sản phẩm.");
                return;
            }

            Order order = orderService.getCurrentOrder();
            order.setUserId(currentUserId);
            order.setStatus("PAID");
            order.setPaymentMethod(cmbPaymentMethod.getValue());

            if (orderService.checkoutCurrentOrder()) {
                showSuccessAlert("✓ Thanh toán thành công!", "Tổng tiền: " + formatPrice(order.getTotalAmount()));
                orderService = new OrderService();
                updateCartDisplay();
                updateTotalAmount();
            } else {
                showErrorAlert("Lỗi thanh toán", "Có lỗi xảy ra khi lưu đơn hàng!");
            }
        } catch (Exception e) {
            showErrorAlert("Lỗi", "Có lỗi xảy ra: " + e.getMessage());
        }
    }

    @FXML
    private void filterAll(ActionEvent event) {
        setActiveButton(btnAll);
        loadProductsFromDatabase();
    }

    @FXML
    private void filterTraSua(ActionEvent event) {
        setActiveButton(btnTraSua);
        filterByCategory(2);
    }

    @FXML
    private void filterCafe(ActionEvent event) {
        setActiveButton(btnCafe);
        filterByCategory(1);
    }

    @FXML
    private void filterTra(ActionEvent event) {
        setActiveButton(btnTra);
        filterByCategory(3);
    }

    @FXML
    private void filterDacBiet(ActionEvent event) {
        setActiveButton(btnDacBiet);
        filterByCategory(4);
    }

    private void filterByCategory(int categoryId) {
        try {
            List<ProductDTO> filtered = productService.getProductsByCategory(categoryId);
            displayProducts(filtered);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayProducts(List<ProductDTO> products) {
        productGrid.getChildren().clear();
        for (ProductDTO p : products) {
            VBox card = loadProductCard(p.getProductId(), p.getName(), p.getCategoryName(), p.getPrice(), p.getImageUrl());
            if (card != null) {
                productGrid.getChildren().add(card);
            }
        }
    }

    private void updateCartDisplay() {
        List<OrderDetailDTO> cartItems = orderService.getCartItems();
        cartTableView.setItems(FXCollections.observableArrayList(cartItems));
    }

    private void updateTotalAmount() {
        BigDecimal total = orderService.getCurrentOrder().getTotalAmount();
        lblTotalAmount.setText(formatPrice(total));
    }

    private void setActiveButton(Button clickedButton) {
        Button[] allButtons = {btnAll, btnTraSua, btnCafe, btnTra, btnDacBiet};
        for (Button btn : allButtons) {
            if (btn != null) {
                btn.getStyleClass().remove("category-btn-active");
            }
        }
        clickedButton.getStyleClass().add("category-btn-active");
    }

    private VBox loadProductCard(int productId, String name, String category, BigDecimal price, String imagePath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vtea/view/ProductCard.fxml"));
            VBox cardNode = loader.load();

            Label lblName = (Label) cardNode.lookup("#lblProductName");
            Label lblCat = (Label) cardNode.lookup("#lblCategory");
            Label lblPrice = (Label) cardNode.lookup("#lblPrice");
            ImageView imgProduct = (ImageView) cardNode.lookup("#imgProduct");
            Button btnAdd = (Button) cardNode.lookup("#btnAddToCart");

            lblName.setText(name);
            lblCat.setText(category);
            lblPrice.setText(formatPrice(price));

            if (btnAdd != null) {
                btnAdd.setOnAction(e -> handleAddToCart(productId, name, price));
            }

            if (imagePath != null && !imagePath.isEmpty()) {
                try {
                    Image image = new Image(getClass().getResourceAsStream(imagePath));
                    imgProduct.setImage(image);
                } catch (Exception e) {
                    // Do nothing
                }
            }
            return cardNode;
        } catch (IOException e) {
            return null;
        }
    }

    private String formatPrice(BigDecimal price) {
        return String.format("%,.0f đ", price);
    }

    private void showSuccessAlert(String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, title, message);
    }

    private void showErrorAlert(String title, String message) {
        showAlert(Alert.AlertType.ERROR, title, message);
    }

    private void showInfoAlert(String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, title, message);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean showConfirmDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
}