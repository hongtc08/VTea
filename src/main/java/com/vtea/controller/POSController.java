package com.vtea.controller;
import com.vtea.service.POSCacheService;
import com.vtea.utils.CustomerDialogHelper;
import com.vtea.utils.DialogHelper;
import com.vtea.dto.CategoryDTO;
import com.vtea.dto.CustomerCheckoutResult;
import com.vtea.service.CustomerService;
import com.vtea.dto.OrderDetailDTO;
import com.vtea.dto.ProductDTO;
import com.vtea.dto.UserSessionDTO;
import com.vtea.model.Order;
import com.vtea.service.OrderService;
import com.vtea.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import javafx.application.Platform;
import java.util.concurrent.CompletableFuture;

public class POSController {

    //Them cache cai thien toc do
    private final POSCacheService posCacheService = POSCacheService.getInstance();
    //
    private final OrderService orderService = new OrderService();

    private int currentCategoryId = -1;

    @FXML private FlowPane productGrid;
    @FXML private TextField searchField;

    @FXML private ScrollPane categoryScrollPane;
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
        setupProductSearch();
        updateCartDisplay();
        updateTotalAmount();

        loadPOSCacheAsync();
    }

    private int resolveCurrentUserId() {
        UserSessionDTO user = SessionManager.getCurrentUser();
        return user != null ? user.getId() : 1;
    }

    private void setupProductSearch() {
        if (searchField == null) {
            return;
        }
        searchField.textProperty().addListener((obs, oldVal, newVal) -> refreshProductGrid());
    }

    private void refreshProductGrid() {
        if (toppingMode) {
            displayProducts(filterProductsByName(posCacheService.getToppings()));
            return;
        }
        if (currentCategoryId == -1) {
            displayProducts(filterProductsByName(posCacheService.getProducts()));
        } else {
            displayProducts(filterProductsByName(posCacheService.getProductsByCategory(currentCategoryId)));
        }
    }

    private List<ProductDTO> filterProductsByName(List<ProductDTO> products) {
        if (products == null) {
            return List.of();
        }
        String keyword = searchField != null && searchField.getText() != null
                ? searchField.getText().trim().toLowerCase()
                : "";
        if (keyword.isEmpty()) {
            return products;
        }
        return products.stream()
                .filter(p -> p.getName() != null && p.getName().toLowerCase().contains(keyword))
                .toList();
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
            currentCategoryId = -1;
            refreshProductGrid();
        });
        categoryBar.getChildren().add(allButton);

        try {
            List<CategoryDTO> categories = posCacheService.getCategories();
            for (CategoryDTO category : categories) {
                Button categoryButton = createCategoryButton(category.getName());
                categoryButton.setOnAction(event -> {
                    setActiveButton(categoryButton);
                    currentCategoryId = category.getCategoryId();
                    refreshProductGrid();
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
    private OrderDetailDTO toppingTargetItem = null; // item trong giỏ đang được thêm topping

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
        if (toppingMode) {
            exitToppingMode();
        }
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

            BigDecimal subtotal = calculateCartSubtotal();
            BigDecimal vat = subtotal.multiply(new BigDecimal("0.10"));
            BigDecimal total = orderService.getCurrentOrder().getTotalAmount();

            CustomerCheckoutResult customerResult = CustomerDialogHelper.showCheckoutDialog(subtotal, vat, total);
            if (!customerResult.isConfirmed()) {
                return;
            }

            Order order = orderService.getCurrentOrder();
            order.setUserId(resolveCurrentUserId());
            order.setStatus("PAID");
            order.setPaymentMethod(paymentMethod);
            order.setCustomerId(customerResult.getCustomerId());
            order.setTotalAmount(customerResult.getFinalTotal());

            int pointsDelta = 0;
            if (customerResult.getCustomerId() != null) {
                pointsDelta = customerResult.isUsePoints()
                        ? -customerResult.getPointsUsed()
                        : customerResult.getPointsToEarn();
            }

            boolean success = orderService.checkoutCurrentOrder(pointsDelta);

            if (success) {
                showSuccessAlert(
                        "✓ Thanh toán thành công!",
                        buildCheckoutSuccessMessage(customerResult)
                );

                orderService.clearCart();
                if (toppingMode) {
                    exitToppingMode();
                }
                refreshCart();
            } else {
                showErrorAlert("Lỗi thanh toán", "Có lỗi xảy ra khi lưu đơn hàng!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Lỗi", "Có lỗi xảy ra: " + e.getMessage());
        }
    }

    private String buildCheckoutSuccessMessage(CustomerCheckoutResult customerResult) {
        StringBuilder message = new StringBuilder();
        message.append("Tổng tiền: ").append(formatPrice(customerResult.getFinalTotal()));

        if (customerResult.getCustomerId() != null) {
            if (customerResult.isUsePoints() && customerResult.getPointsUsed() > 0) {
                message.append("\nĐã dùng ").append(customerResult.getPointsUsed()).append(" điểm");
            } else if (customerResult.getPointsToEarn() > 0) {
                message.append("\nĐã tích ").append(customerResult.getPointsToEarn()).append(" điểm");
            }
        }

        return message.toString();
    }

    private BigDecimal calculateCartSubtotal() {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderDetailDTO item : orderService.getCartItems()) {
            subtotal = subtotal.add(item.getSubTotal());
        }
        return subtotal;
    }

    // ==================== PRODUCT DISPLAY ====================


    /*
    Load cache Database
     */
    private void loadPOSCacheAsync() {
        CompletableFuture
                .runAsync(() -> posCacheService.loadIfNeeded())
                .thenRun(() -> Platform.runLater(() -> {
                    setupCategoryButtons();
                    refreshProductGrid();
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        ex.printStackTrace();
                        showErrorAlert("Lỗi", "Không thể tải dữ liệu POS!");
                    });

                    return null;
                });
    }

    /*
    Hien product tu cache lay tu Database
     */
    private void loadProductsFromDatabase() {
        currentCategoryId = -1;
        refreshProductGrid();
    }

    private void showOnlyToppings() {
        setCategoryBarVisible(false);
        displayProducts(filterProductsByName(posCacheService.getToppings()));
    }

    private void onCartItemRemoved(OrderDetailDTO removedItem) {
        if (toppingMode && toppingTargetItem == removedItem) {
            exitToppingMode();
        }
    }

    private void setCategoryBarVisible(boolean visible) {
        if (categoryScrollPane != null) {
            categoryScrollPane.setVisible(visible);
            categoryScrollPane.setManaged(visible);
        }
    }

    private void enterToppingMode(OrderDetailDTO item) {
        toppingMode = true;
        toppingTargetItem = item;
        if (lblScreenTitle != null) {
            lblScreenTitle.setText("Thêm topping");
        }
        showOnlyToppings();
    }

    private void exitToppingMode() {
        toppingMode = false;
        toppingTargetItem = null;
        if (lblScreenTitle != null) {
            lblScreenTitle.setText("Bán hàng (POS)");
        }
        setCategoryBarVisible(true);
        loadProductsFromDatabase();
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
                        orderService.addToppingToItem(toppingTargetItem, product.getProductId());
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
                image = new Image(imagePath, true);
            } else {
                var imageStream = getClass().getResourceAsStream(imagePath);

                if (imageStream == null) {
                    System.err.println("Không tìm thấy ảnh trong resources: " + imagePath);
                    return;
                }

                image = new Image(imageStream);
            }

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

                    ProductDTO topping = posCacheService.findToppingById(toppingId);
                    String toppingLabel = (topping != null)
                            ? topping.getName() + " (x" + qty + ")"
                            : "Topping#" + toppingId + " (x" + qty + ")";

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
                orderService.increaseQuantity(item);
                refreshCart();
            });
        }

        // Khi bấm nút -, giảm số lượng món hiện tại trong giỏ hàng.
        // Nếu số lượng còn 1 thì service sẽ tự xóa món khỏi giỏ.
        // Sau đó refresh lại giao diện giỏ hàng và tổng tiền.
        if (btnMinus != null) {
            btnMinus.setOnAction(event -> {
                boolean wasInCart = orderService.getCartItems().contains(item);
                orderService.decreaseQuantity(item);
                if (wasInCart && !orderService.getCartItems().contains(item)) {
                    onCartItemRemoved(item);
                }
                refreshCart();
            });
        }

        if (btnRemove != null) {
            btnRemove.setOnAction(event -> {
                orderService.removeFromCart(item);
                onCartItemRemoved(item);
                refreshCart();
            });
        }

        // Nút bật/tắt chế độ thêm topping cho món này
        Button btnTopping = (Button) cartNode.lookup("#btnTopping");
        if (btnTopping != null) {

            // 1. Cập nhật trạng thái hiển thị của nút ngay khi load item
            if (toppingMode && toppingTargetItem == item) {
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
                if (!toppingMode || toppingTargetItem != item) {
                    enterToppingMode(item);
                } else {
                    exitToppingMode();
                }

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
                            orderService.changeToppingQuantity(item, toppingId, -1);
                            refreshCart();
                        });

                        btnPlusT.setOnAction(evt -> {
                            orderService.changeToppingQuantity(item, toppingId, 1);
                            refreshCart();
                        });

                        btnRemoveT.setOnAction(evt -> {
                            orderService.removeToppingFromItem(item, toppingId);
                            refreshCart();
                        });
                    }
                }
            }
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
