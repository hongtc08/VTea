package com.vtea.controller;

import com.vtea.utils.DialogHelper;
import com.vtea.dto.CategoryDTO;
import com.vtea.dto.OrderDetailDTO;
import com.vtea.dto.ProductDTO;
import com.vtea.model.Order;
import com.vtea.service.CategoryService;
import com.vtea.service.OrderService;
import com.vtea.service.ProductService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class POSController {

    private final ProductService productService = new ProductService();
    private final CategoryService categoryService = new CategoryService();
    private final OrderService orderService = new OrderService();

    // TODO: Sau này thay bằng user đang đăng nhập từ SessionManager
    private int currentUserId = 1;

    @FXML private FlowPane productGrid;

    @FXML private HBox categoryBar;

    @FXML private VBox cartItemsBox;
    @FXML private VBox cartEmptyLabel;
    @FXML private Label lblScreenTitle;
    @FXML private Label subtotalLabel;
    @FXML private Label taxLabel;
    @FXML private Label lblTotalAmount;
    @FXML private ComboBox<String> cmbPaymentMethod;

    // ==================== INIT ====================

    @FXML
    public void initialize() {
        setupPaymentMethods();
        setupCategoryButtons();
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

    private void setupCategoryButtons() {
        if (categoryBar == null) {
            return;
        }

        categoryBar.getChildren().clear();

        Button allButton = createCategoryButton("T\u1ea5t c\u1ea3");
        allButton.getStyleClass().add("category-btn-active");
        allButton.setOnAction(event -> {
            setActiveButton(allButton);
            loadProductsFromDatabase();
        });
        categoryBar.getChildren().add(allButton);

        try {
            List<CategoryDTO> categories = categoryService.getAllActiveCategories();
            for (CategoryDTO category : categories) {
                Button categoryButton = createCategoryButton(category.getName());
                categoryButton.setOnAction(event -> {
                    setActiveButton(categoryButton);
                    filterByCategory(category.getCategoryId());
                });
                categoryBar.getChildren().add(categoryButton);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("L\u1ed7i", "Kh\u00f4ng th\u1ec3 t\u1ea3i danh m\u1ee5c s\u1ea3n ph\u1ea9m!");
        }
    }

    // Topping mode state
    private boolean toppingMode = false;
    private int toppingTargetProductId = -1; // productId của OrderDetail đang được thêm topping

    private Button createCategoryButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("category-btn");
        return button;
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

            if (lblName != null) {
                lblName.setText(product.getName());
            }

            if (lblCategory != null) {
                lblCategory.setText(product.getCategoryName());
            }

            if (lblPrice != null) {
                lblPrice.setText(formatPrice(product.getPrice()));
            }

            cardNode.setOnMouseClicked(event -> {
                if (toppingMode) {
                    // Khi đang ở chế độ thêm topping: nhấn lên 1 sản phẩm sẽ thêm nó làm topping cho item được chọn
                    try {
                        orderService.addToppingToItem(toppingTargetProductId, product.getProductId());
                        refreshCart();
                        showSuccessAlert("Thêm topping", product.getName() + " ✓");
                    } catch (Exception e) {
                        e.printStackTrace();
                        showErrorAlert("Lỗi", "Không thể thêm topping: " + e.getMessage());
                    }
                } else {
                    handleAddToCart(
                            product.getProductId(),
                            product.getName(),
                            product.getPrice()
                    );
                }
            });

            cardNode.setCursor(javafx.scene.Cursor.HAND);

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
            Image image;
            if (imagePath.startsWith("http") || imagePath.startsWith("file:")) {
                image = new Image(imagePath);
            } else {
                image = new Image(getClass().getResourceAsStream(imagePath));
            }

            if (image != null && !image.isError()) {
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

        if (cartItemsBox != null) {
            cartItemsBox.getChildren().clear();
        }

        if (cartItems == null || cartItems.isEmpty()) {
            showEmptyCartMessage();
            return;
        }

        for (OrderDetailDTO item : cartItems) {
            HBox cartNode = loadCartItemNode(item);

            if (cartNode != null && cartItemsBox != null) {
                cartItemsBox.getChildren().add(cartNode);
            }
        }
    }

    private HBox loadCartItemNode(OrderDetailDTO item) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/vtea/view/CartItem.fxml")
            );

            HBox cartNode = loader.load();

            bindCartItemData(cartNode, item);
            bindCartItemEvents(cartNode, item);

            return cartNode;
        } catch (IOException e) {
            System.err.println("Lỗi load UI CartItem.fxml");
            e.printStackTrace();
            return null;
        }
    }

    private void bindCartItemData(HBox cartNode, OrderDetailDTO item) {
        Label lblCartName = (Label) cartNode.lookup("#lblCartName");
        Label lblCartPrice = (Label) cartNode.lookup("#lblCartPrice");
        Label lblQty = (Label) cartNode.lookup("#lblQty");
        Label lblSubTotal = (Label) cartNode.lookup("#lblSubTotal");

        if (lblCartName != null) {
            lblCartName.setText(item.getProductName());
        }

        if (lblQty != null) {
            lblQty.setText(String.valueOf(item.getQuantity()));
        }

        if (lblCartPrice != null) {
            // đơn giá
            lblCartPrice.setText(formatPrice(item.getUnitPrice()));
        }

        if (lblSubTotal != null) {
            lblSubTotal.setText(formatPrice(item.getSubTotal()));
        }

        // Hiển thị danh sách topping nếu có
        VBox toppingsContainer = (VBox) cartNode.lookup("#toppingsContainer");
        if (toppingsContainer != null) {
            toppingsContainer.getChildren().clear();
            if (item.getToppingQuantities() != null && !item.getToppingQuantities().isEmpty()) {
                toppingsContainer.setVisible(true);
                toppingsContainer.setManaged(true);

                // Lấy thông tin topping từ service để hiển thị tên và giá
                for (var entry : item.getToppingQuantities().entrySet()) {
                    int toppingId = entry.getKey();
                    int qty = entry.getValue();

                    var topping = orderService.findActiveToppingById(toppingId);
                    String toppingLabel = (topping != null) ? topping.getName() + " (x" + qty + ")" : "Topping#" + toppingId + " (x" + qty + ")";

                    HBox row = new HBox();
                    row.setSpacing(8);
                    Label lbl = new Label(toppingLabel);
                    lbl.getStyleClass().add("cart-item-topping");

                    // Buttons: - / + / remove
                    Button btnMinusT = new Button("-");
                    Button btnPlusT = new Button("+");
                    Button btnRemoveT = new Button("x");

                    btnMinusT.getStyleClass().add("qty-btn");
                    btnPlusT.getStyleClass().add("qty-btn");
                    btnRemoveT.getStyleClass().add("btn-remove-topping");

                    row.getChildren().addAll(lbl, btnMinusT, btnPlusT, btnRemoveT);

                    // attach handlers (will be bound in bindCartItemEvents)
                    row.setUserData(toppingId);

                    toppingsContainer.getChildren().add(row);
                }
            } else {
                toppingsContainer.setVisible(false);
                toppingsContainer.setManaged(false);
            }
        }
    }

    private void bindCartItemEvents(HBox cartNode, OrderDetailDTO item) {
        Button btnMinus = (Button) cartNode.lookup("#btnMinus");
        Button btnPlus = (Button) cartNode.lookup("#btnPlus");
        Button btnRemove = (Button) cartNode.lookup("#btnRemove");

        // Khi bấm nút +, tăng số lượng món hiện tại trong giỏ hàng,
        // sau đó refresh lại giao diện giỏ hàng và tổng tiền.
        if (btnPlus != null) {
            btnPlus.setOnAction(event -> {
                orderService.increaseQuantity(item.getProductId());
                refreshCart();
            });
        }

        // Khi bấm nút -, giảm số lượng món hiện tại trong giỏ hàng.
        // Nếu số lượng còn 1 thì service sẽ tự xóa món khỏi giỏ.
        // Sau đó refresh lại giao diện giỏ hàng và tổng tiền.
        if (btnMinus != null) {
            btnMinus.setOnAction(event -> {
                orderService.decreaseQuantity(item.getProductId());
                refreshCart();
            });
        }

        // Khi bấm nút xóa, xóa món hiện tại khỏi giỏ hàng,
        // sau đó refresh lại giao diện giỏ hàng và tổng tiền.
        if (btnRemove != null) {
            btnRemove.setOnAction(event -> {
                orderService.removeFromCart(item.getProductId());
                refreshCart();
            });
        }

        // Nút bật/tắt chế độ thêm topping cho món này
        Button btnTopping = (Button) cartNode.lookup("#btnTopping");
        if (btnTopping != null) {

            // 1. Cập nhật trạng thái hiển thị của nút ngay khi load item
            if (toppingMode && toppingTargetProductId == item.getProductId()) {
                btnTopping.setText("Đang thêm topping");
                if (!btnTopping.getStyleClass().contains("btn-topping-active")) {
                    btnTopping.getStyleClass().add("btn-topping-active");
                }
            } else {
                btnTopping.setText("Thêm topping");
                btnTopping.getStyleClass().remove("btn-topping-active");
            }

            // 2. Xử lý logic khi click
            btnTopping.setOnAction(event -> {
                if (!toppingMode || toppingTargetProductId != item.getProductId()) {
                    // Bật chế độ thêm topping
                    toppingMode = true;
                    toppingTargetProductId = item.getProductId();
                    if (lblScreenTitle != null) lblScreenTitle.setText("Thêm topping");
                    showOnlyToppings();
                } else {
                    // Tắt chế độ
                    toppingMode = false;
                    toppingTargetProductId = -1;
                    if (lblScreenTitle != null) lblScreenTitle.setText("Bán hàng (POS)");
                    loadProductsFromDatabase();
                }

                // Cập nhật lại toàn bộ giao diện giỏ hàng để đổi trạng thái UI của nút
                refreshCart();
            });
        }

        // Các nút + - / x trong danh sách topping (tạo động)
        VBox toppingsContainer = (VBox) cartNode.lookup("#toppingsContainer");
        if (toppingsContainer != null) {
            for (javafx.scene.Node node : toppingsContainer.getChildren()) {
                if (node instanceof HBox hrow) {
                    Object ud = hrow.getUserData();
                    if (ud instanceof Integer toppingId) {
                        Button btnMinusT = (Button) hrow.getChildren().get(1);
                        Button btnPlusT = (Button) hrow.getChildren().get(2);
                        Button btnRemoveT = (Button) hrow.getChildren().get(3);

                        btnMinusT.setOnAction(evt -> {
                            orderService.changeToppingQuantity(item.getProductId(), toppingId, -1);
                            refreshCart();
                        });

                        btnPlusT.setOnAction(evt -> {
                            orderService.changeToppingQuantity(item.getProductId(), toppingId, 1);
                            refreshCart();
                        });

                        btnRemoveT.setOnAction(evt -> {
                            orderService.removeToppingFromItem(item.getProductId(), toppingId);
                            refreshCart();
                        });
                    }
                }
            }
        }
    }

    private void showOnlyToppings() {
        try {
            // Lấy các topping hiện có (OrderService -> ToppingDAO)
            var all = productService.getAllActiveProducts();
            List<ProductDTO> toppings = new java.util.ArrayList<>();
            for (ProductDTO p : all) {
                if (p.getCategoryName() != null && p.getCategoryName().toLowerCase().contains("topping")) {
                    toppings.add(p);
                }
            }

            // Nếu không tìm thấy theo danh mục, fallback: lấy từ OrderService.getAllActiveToppings
            if (toppings.isEmpty()) {
                var tList = orderService.getAllActiveToppings();
                // Convert Topping -> ProductDTO-like minimal objects for display
                for (var t : tList) {
                    ProductDTO pd = new ProductDTO();
                    pd.setProductId(t.getToppingId());
                    pd.setName(t.getName());
                    pd.setPrice(t.getPrice());
                    pd.setCategoryName("Topping");
                    toppings.add(pd);
                }
            }

            displayProducts(toppings);
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Lỗi", "Không thể tải danh sách topping!");
        }
    }

    private void showEmptyCartMessage() {
        if (cartItemsBox == null || cartEmptyLabel == null) {
            return;
        }

        if (!cartItemsBox.getChildren().contains(cartEmptyLabel)) {
            cartItemsBox.getChildren().add(cartEmptyLabel);
        }
    }

    private void updateTotalAmount() {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderDetailDTO item : orderService.getCartItems()) {
            subtotal = subtotal.add(item.getSubTotal());
        }
        
        BigDecimal tax = subtotal.multiply(new BigDecimal("0.10"));
        BigDecimal total = orderService.getCurrentOrder().getTotalAmount();

        if (subtotalLabel != null) {
            subtotalLabel.setText(formatPrice(subtotal));
        }
        if (taxLabel != null) {
            taxLabel.setText(formatPrice(tax));
        }
        lblTotalAmount.setText(formatPrice(total));
    }

    // ==================== UI HELPERS ====================

    private void setActiveButton(Button clickedButton) {
        if (categoryBar != null) {
            for (javafx.scene.Node node : categoryBar.getChildren()) {
                if (node instanceof Button button) {
                    button.getStyleClass().remove("category-btn-active");
                }
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

    // ==================== ALERT HELPERS (ĐÃ NÂNG CẤP LÊN CUSTOM DIALOG) ====================

    private void showSuccessAlert(String title, String message) {
        DialogHelper.showInfo(title, message);
    }

    private void showErrorAlert(String title, String message) {
        DialogHelper.showInfo(title, message);
    }

    private void showInfoAlert(String title, String message) {
        DialogHelper.showInfo(title, message);
    }

    private boolean showConfirmDialog(String title, String message) {
        return DialogHelper.showConfirm(title, message);
    }
}
