package com.vtea.controller;

import com.vtea.dto.OrderDetailDTO;
import com.vtea.dto.ProductDTO;
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

    @FXML private VBox cartItemsBox;
    @FXML private VBox cartEmptyLabel;
    @FXML private Label lblTotalAmount;
    @FXML private ComboBox<String> cmbPaymentMethod;

    // ==================== INIT ====================

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

    // ==================== PRODUCT EVENTS ====================

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

    // ==================== CART EVENTS ====================

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

    // Xử lý khi người dùng bấm nút tăng số lượng món trong giỏ hàng.
    // Lấy món đang được chọn trong bảng giỏ hàng, gọi service tăng số lượng,
    // sau đó refresh lại giỏ hàng và tổng tiền.
    @FXML
    private void handleIncreaseSelectedCartItem(ActionEvent event) {
        OrderDetailDTO selectedItem = getSelectedCartItem();

        if (selectedItem == null) {
            showInfoAlert("Thông báo", "Vui lòng chọn món cần tăng số lượng!");
            return;
        }

        orderService.increaseQuantity(selectedItem.getProductId());
        refreshCart();
    }

    // Xử lý khi người dùng bấm nút giảm số lượng món trong giỏ hàng.
    // Lấy món đang được chọn trong bảng giỏ hàng, gọi service giảm số lượng.
    // Nếu số lượng còn 1 thì service sẽ tự xóa món khỏi giỏ.
    // Sau đó refresh lại giỏ hàng và tổng tiền.
    @FXML
    private void handleDecreaseSelectedCartItem(ActionEvent event) {
        OrderDetailDTO selectedItem = getSelectedCartItem();

        if (selectedItem == null) {
            showInfoAlert("Thông báo", "Vui lòng chọn món cần giảm số lượng!");
            return;
        }

        orderService.decreaseQuantity(selectedItem.getProductId());
        refreshCart();
    }

    // Xử lý khi người dùng bấm nút xóa món khỏi giỏ hàng.
    // Lấy món đang được chọn trong bảng giỏ hàng, gọi service xóa món,
    // sau đó refresh lại giỏ hàng và tổng tiền.
    @FXML
    private void handleRemoveSelectedCartItem(ActionEvent event) {
        OrderDetailDTO selectedItem = getSelectedCartItem();

        if (selectedItem == null) {
            showInfoAlert("Thông báo", "Vui lòng chọn món cần xóa!");
            return;
        }

        orderService.removeFromCart(selectedItem.getProductId());
        refreshCart();
    }

    // Lấy món đang được chọn trong bảng giỏ hàng.
    // Method này dùng chung cho các event tăng, giảm và xóa món.
    private OrderDetailDTO getSelectedCartItem() {
        return cartTableView.getSelectionModel().getSelectedItem();
    }

    // ==================== CHECKOUT EVENTS ====================

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

    // ==================== PRODUCT DISPLAY ====================

    private void loadProductsFromDatabase() {
        try {
            List<ProductDTO> products = productService.getAllActiveProducts();
            displayProducts(products);
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Lỗi", "Không thể tải danh sách sản phẩm!");
        }
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

    // ==================== CART DISPLAY ====================

    private void refreshCart() {
        updateCartDisplay();
        updateTotalAmount();
    }

    private void updateCartDisplay() {
        List<OrderDetailDTO> cartItems = orderService.getCartItems();

        // 1. Xóa sạch giỏ hàng cũ trên UI trước khi vẽ lại
        if (cartItemsBox != null) {
            cartItemsBox.getChildren().clear();
        }

        // 2. Nếu giỏ hàng trống, hiển thị lại VBox "Chưa có sản phẩm nào"
        if (cartItems == null || cartItems.isEmpty()) {
            if (cartItemsBox != null && cartEmptyLabel != null && !cartItemsBox.getChildren().contains(cartEmptyLabel)) {
                cartItemsBox.getChildren().add(cartEmptyLabel);
            }
            return;
        }

        // 3. Nếu có hàng, duyệt qua danh sách và tạo từng thẻ Cart Item
        for (OrderDetailDTO item : cartItems) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vtea/view/CartItem.fxml"));
                javafx.scene.layout.HBox cartNode = loader.load();

                // Ánh xạ các thành phần bên trong thẻ CartItem
                Label lblCartName = (Label) cartNode.lookup("#lblCartName");
                Label lblCartPrice = (Label) cartNode.lookup("#lblCartPrice");
                Label lblQty = (Label) cartNode.lookup("#lblQty");
                Button btnRemove = (Button) cartNode.lookup("#btnRemove");

                // Gắn dữ liệu từ DTO vào UI
                // Lưu ý: Đảm bảo class OrderDetailDTO của bạn có các hàm getProductName(), getQuantity()...
                if (lblCartName != null) lblCartName.setText(item.getProductName());
                if (lblQty != null) lblQty.setText(String.valueOf(item.getQuantity()));

                // Giả sử giá tiền bạn muốn hiển thị là tổng giá của item đó (giá x số lượng)
                if (lblCartPrice != null) lblCartPrice.setText(formatPrice(item.getSubTotal()));

                // Gắn sự kiện xóa món khỏi giỏ hàng
                if (btnRemove != null) {
                    btnRemove.setOnAction(e -> {
                        // Gọi hàm xóa của Backend (bạn cần điều chỉnh hàm xóa theo logic của Service)
                        // Ví dụ: orderService.removeCartItem(item.getProductId());
                        // Sau đó gọi lại hàm refreshCart();
                    });
                }

                // Thêm thẻ vừa tạo vào VBox giỏ hàng
                cartItemsBox.getChildren().add(cartNode);

            } catch (java.io.IOException e) {
                System.err.println("Lỗi load UI CartItem.fxml");
                e.printStackTrace();
            }
        }
    }

    private void updateTotalAmount() {
        BigDecimal total = orderService.getCurrentOrder().getTotalAmount();
        lblTotalAmount.setText(formatPrice(total));
    }

    // ==================== UI HELPERS ====================

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

    // ==================== ALERT HELPERS ====================

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