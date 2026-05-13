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

    private static final int CATEGORY_CAFE = 1;
    private static final int CATEGORY_TRA_SUA = 2;
    private static final int CATEGORY_TRA = 3;
    private static final int CATEGORY_DAC_BIET = 4;

    private final ProductService productService = new ProductService();
    private final OrderService orderService = new OrderService();

    // TODO: Sau này thay bằng user đang đăng nhập từ SessionManager
    private int currentUserId = 1;

    @FXML private FlowPane productGrid;

    @FXML private Button btnAll;
    @FXML private Button btnTraSua;
    @FXML private Button btnCafe;
    @FXML private Button btnTra;
    @FXML private Button btnDacBiet;

    @FXML private TableView<OrderDetailDTO> cartTableView;
    @FXML private Label lblTotalAmount;
    @FXML private ComboBox<String> cmbPaymentMethod;

    @FXML
    public void initialize() {
        setupPaymentMethods();
        loadProductsFromDatabase();
        updateCartDisplay();
        updateTotalAmount();
    }

    private void setupPaymentMethods() {
        cmbPaymentMethod.setItems(FXCollections.observableArrayList(
                "Tiền mặt",
                "Thẻ ghi nợ",
                "QR Pay"
        ));
        cmbPaymentMethod.setValue("Tiền mặt");
    }

    private void loadProductsFromDatabase() {
        try {
            List<ProductDTO> products = productService.getAllActiveProducts();
            displayProducts(products);
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Lỗi", "Không thể tải danh sách sản phẩm!");
        }
    }

    public void handleAddToCart(int productId, String productName, BigDecimal price) {
        try {
            orderService.addToCart(productId, productName, price, 1);
            refreshCart();

            showSuccessAlert(
                    "Thêm vào giỏ",
                    productName + " ✓\nGiá: " + formatPrice(price)
            );
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Lỗi", "Không thể thêm sản phẩm vào giỏ hàng!");
        }
    }

    @FXML
    private void handleClearCart(ActionEvent event) {
        if (orderService.isCartEmpty()) {
            showInfoAlert("Thông báo", "Giỏ hàng đã trống!");
            return;
        }

        boolean confirmed = showConfirmDialog(
                "Xác nhận",
                "Bạn có chắc muốn xóa toàn bộ giỏ hàng?"
        );

        if (!confirmed) {
            return;
        }

        orderService.clearCart();
        refreshCart();
        showInfoAlert("Thành công", "Giỏ hàng đã được xóa!");
    }

    @FXML
    private void handleCheckout(ActionEvent event) {
        try {
            if (orderService.isCartEmpty()) {
                showErrorAlert("Lỗi", "Giỏ hàng trống! Vui lòng thêm sản phẩm.");
                return;
            }

            String paymentMethod = cmbPaymentMethod.getValue();
            if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
                showErrorAlert("Lỗi", "Vui lòng chọn phương thức thanh toán!");
                return;
            }

            Order order = orderService.getCurrentOrder();
            order.setUserId(currentUserId);
            order.setStatus("PAID");
            order.setPaymentMethod(paymentMethod);

            boolean success = orderService.checkoutCurrentOrder();

            if (success) {
                showSuccessAlert(
                        "✓ Thanh toán thành công!",
                        "Tổng tiền: " + formatPrice(order.getTotalAmount())
                );

                orderService.clearCart();
                refreshCart();
            } else {
                showErrorAlert("Lỗi thanh toán", "Có lỗi xảy ra khi lưu đơn hàng!");
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        filterByCategory(CATEGORY_TRA_SUA);
    }

    @FXML
    private void filterCafe(ActionEvent event) {
        setActiveButton(btnCafe);
        filterByCategory(CATEGORY_CAFE);
    }

    @FXML
    private void filterTra(ActionEvent event) {
        setActiveButton(btnTra);
        filterByCategory(CATEGORY_TRA);
    }

    @FXML
    private void filterDacBiet(ActionEvent event) {
        setActiveButton(btnDacBiet);
        filterByCategory(CATEGORY_DAC_BIET);
    }

    private void filterByCategory(int categoryId) {
        try {
            List<ProductDTO> filteredProducts = productService.getProductsByCategory(categoryId);
            displayProducts(filteredProducts);
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Lỗi", "Không thể lọc sản phẩm theo danh mục!");
        }
    }

    private void displayProducts(List<ProductDTO> products) {
        productGrid.getChildren().clear();

        if (products == null || products.isEmpty()) {
            return;
        }

        for (ProductDTO product : products) {
            VBox card = loadProductCard(product);

            if (card != null) {
                productGrid.getChildren().add(card);
            }
        }
    }

    private VBox loadProductCard(ProductDTO product) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/vtea/view/ProductCard.fxml")
            );

            VBox cardNode = loader.load();

            Label lblName = (Label) cardNode.lookup("#lblProductName");
            Label lblCategory = (Label) cardNode.lookup("#lblCategory");
            Label lblPrice = (Label) cardNode.lookup("#lblPrice");
            ImageView imgProduct = (ImageView) cardNode.lookup("#imgProduct");
            Button btnAdd = (Button) cardNode.lookup("#btnAddToCart");

            if (lblName != null) {
                lblName.setText(product.getName());
            }

            if (lblCategory != null) {
                lblCategory.setText(product.getCategoryName());
            }

            if (lblPrice != null) {
                lblPrice.setText(formatPrice(product.getPrice()));
            }

            if (btnAdd != null) {
                btnAdd.setOnAction(event -> handleAddToCart(
                        product.getProductId(),
                        product.getName(),
                        product.getPrice()
                ));
            }

            loadProductImage(imgProduct, product.getImageUrl());

            return cardNode;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void loadProductImage(ImageView imgProduct, String imagePath) {
        if (imgProduct == null) {
            return;
        }

        if (imagePath == null || imagePath.trim().isEmpty()) {
            return;
        }

        try {
            Image image = new Image(getClass().getResourceAsStream(imagePath));

            if (!image.isError()) {
                imgProduct.setImage(image);
            }
        } catch (Exception e) {
            System.err.println("Không load được ảnh sản phẩm: " + imagePath);
            e.printStackTrace();
        }
    }

    private void refreshCart() {
        updateCartDisplay();
        updateTotalAmount();
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
        Button[] allButtons = {
                btnAll,
                btnTraSua,
                btnCafe,
                btnTra,
                btnDacBiet
        };

        for (Button button : allButtons) {
            if (button != null) {
                button.getStyleClass().remove("category-btn-active");
            }
        }

        if (clickedButton != null && !clickedButton.getStyleClass().contains("category-btn-active")) {
            clickedButton.getStyleClass().add("category-btn-active");
        }
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) {
            return "0 đ";
        }

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