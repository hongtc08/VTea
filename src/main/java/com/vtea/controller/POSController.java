package com.vtea.controller;

import com.vtea.dto.BillDTO;
import com.vtea.dto.CategoryDTO;
import com.vtea.dto.CustomerDTO;
import com.vtea.dto.OrderDetailDTO;
import com.vtea.dto.ProductDTO;
import com.vtea.dto.UserSessionDTO;
import com.vtea.model.Order;
import com.vtea.service.BillService;
import com.vtea.service.OrderService;
import com.vtea.service.CategoryService;
import com.vtea.service.ProductService;
import com.vtea.service.payment.PayOSCreateResponse;
import com.vtea.service.payment.PayOSPaymentClient;
import com.vtea.utils.DialogHelper;
import com.vtea.utils.FormatUtils;
import com.vtea.utils.SessionManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javafx.scene.layout.StackPane;
import javafx.animation.TranslateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

/**
 * Controller cho màn hình POS bán hàng.
 * File này phụ trách xử lý giao diện bán hàng: hiển thị sản phẩm, giỏ hàng, topping,
 * thanh toán và mở bill preview sau khi thanh toán thành công.
 */
public class POSController {

    // ==================== SERVICE / STATE (BE LOGIC GỌI TỪ UI) ====================

    private final ProductService productService = new ProductService();
    private final CategoryService categoryService = new CategoryService();

    // Service xử lý nghiệp vụ giỏ hàng và thanh toán.
    private final OrderService orderService = new OrderService();

    // Service lấy dữ liệu hóa đơn để preview hoặc in lại bill.
    private final BillService billService = new BillService();

    // Client gọi payment-backend để tạo link QR payOS và kiểm tra trạng thái thanh toán.
    private final PayOSPaymentClient payOSPaymentClient = new PayOSPaymentClient();

    private int currentUserId;

    // Trạng thái thêm topping cho một món trong giỏ.
    private boolean toppingMode = false;

    // Item đang được chọn để thêm topping.
    private OrderDetailDTO toppingTargetItem = null;

    // ==================== FXML COMPONENTS (FE UI) ====================

    @FXML private FlowPane productGrid;
    @FXML private StackPane posRoot;

    @FXML private ScrollPane categoryScroll;
    @FXML private HBox categoryBar;

    @FXML private VBox cartItemsBox;
    @FXML private VBox cartEmptyLabel;

    @FXML private Label lblScreenTitle;
    @FXML private Label subtotalLabel;
    @FXML private Label taxLabel;
    @FXML private Label lblTotalAmount;

    @FXML private ComboBox<String> cmbPaymentMethod;
    @FXML private TextField searchField;

    private int currentCategoryId = -1;

    // ==================== INIT ====================

    /**
     * Hàm khởi tạo màn POS sau khi FXML load xong.
     * Chỉ setup UI ban đầu và gọi load cache dữ liệu.
     */
    @FXML
    public void initialize() {
        // Lấy ID của user đang đăng nhập từ SessionManager
        UserSessionDTO currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            this.currentUserId = currentUser.getId();
        } else {
            // Nếu không có user đăng nhập, dùng ID mặc định
            this.currentUserId = 1;
        }

        setupPaymentMethods();
        updateCartDisplay();
        updateTotalAmount();
        loadPOSCacheAsync();

        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterProducts();
            });
        }
    }

    /**
     * Setup danh sách phương thức thanh toán trên combobox.
     */
    private void setupPaymentMethods() {
        cmbPaymentMethod.setItems(FXCollections.observableArrayList(
                "Tiền mặt",
                "QR Pay"
        ));
        cmbPaymentMethod.setValue("Tiền mặt");
    }

    // ==================== CHECKOUT EVENTS ====================

    private void playFlyingAnimation(javafx.scene.Node sourceNode, Runnable onFinished) {
        if (posRoot == null || cartItemsBox == null) {
            onFinished.run();
            return;
        }
        
        Bounds sourceBounds = sourceNode.localToScene(sourceNode.getBoundsInLocal());
        Bounds targetBounds = cartItemsBox.localToScene(cartItemsBox.getBoundsInLocal());
        
        Circle flyingDot = new Circle(12, javafx.scene.paint.Color.web("#12b6a2"));
        flyingDot.setManaged(false);
        
        Point2D sourceCenter = new Point2D(
                sourceBounds.getMinX() + sourceBounds.getWidth() / 2,
                sourceBounds.getMinY() + sourceBounds.getHeight() / 2
        );
        Point2D targetCenter = new Point2D(
                targetBounds.getMinX() + targetBounds.getWidth() / 2,
                targetBounds.getMinY() + 40 // Target the top area of the cart
        );
        
        Bounds rootBounds = posRoot.localToScene(posRoot.getBoundsInLocal());
        
        flyingDot.setLayoutX(sourceCenter.getX() - rootBounds.getMinX() - 12);
        flyingDot.setLayoutY(sourceCenter.getY() - rootBounds.getMinY() - 12);
        
        posRoot.getChildren().add(flyingDot);
        
        TranslateTransition tt = new TranslateTransition(Duration.millis(450), flyingDot);
        tt.setToX(targetCenter.getX() - sourceCenter.getX());
        tt.setToY(targetCenter.getY() - sourceCenter.getY());
        tt.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);
        
        ScaleTransition st = new ScaleTransition(Duration.millis(450), flyingDot);
        st.setToX(0.4);
        st.setToY(0.4);
        
        FadeTransition ft = new FadeTransition(Duration.millis(450), flyingDot);
        ft.setToValue(0.3);
        
        ParallelTransition pt = new ParallelTransition(tt, st, ft);
        pt.setOnFinished(e -> {
            posRoot.getChildren().remove(flyingDot);
            onFinished.run();
        });
        pt.play();
    }

    /**
     * Xử lý khi bấm nút thanh toán.
     * Luồng chính:
     * kiểm tra giỏ hàng -> chọn khách hàng -> xử lý phương thức thanh toán -> lưu order -> mở bill preview -> clear cart.
     */
    @FXML
    private void handleCheckout(ActionEvent event) {
        try {
            // 1. Kiểm tra xem giỏ hàng có trống không và đã chọn phương thức thanh toán chưa
            if (!validateCheckoutCondition()) return;

            String paymentMethod = cmbPaymentMethod.getValue();
            
            // 2. Mở popup cho phép nhân viên chọn Khách hàng (để tích/trừ điểm)
            CustomerDialogController customerDialog = showCustomerDialog();

            // Nếu nhân viên bấm "Hủy" hoặc đóng popup thì dừng thanh toán
            if (customerDialog == null || !customerDialog.isSubmitted()) {
                return;
            }

            // 3. Lấy thông tin khách hàng và số điểm họ muốn dùng từ popup
            CustomerDTO selectedCustomer = customerDialog.getSelectedCustomer();
            processCustomerSelection(customerDialog, selectedCustomer);

            int usedPoints = selectedCustomer != null ? customerDialog.getPointsToUse() : 0;
            
            // 4. Bắt đầu luồng lưu đơn hàng dựa trên phương thức thanh toán (Tiền mặt hoặc mã QR)
            buildAndSaveOrder(paymentMethod, selectedCustomer, usedPoints);

        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Lỗi", "Có lỗi xảy ra: " + e.getMessage());
        }
    }

    private boolean validateCheckoutCondition() {
        if (orderService.isCartEmpty()) {
            showErrorAlert("Lỗi", "Giỏ hàng trống! Vui lòng thêm sản phẩm.");
            return false;
        }

        String paymentMethod = cmbPaymentMethod.getValue();
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            showErrorAlert("Lỗi", "Vui lòng chọn phương thức thanh toán!");
            return false;
        }
        return true;
    }

    private void processCustomerSelection(CustomerDialogController dialog, CustomerDTO customer) throws Exception {
        if (customer != null && customer.getCustomerId() != null) {
            orderService.setCustomer(customer.getCustomerId());
            if (dialog.getPointsToUse() > 0) {
                orderService.applyRewardPoints(dialog.getPointsToUse());
            }
        } else {
            orderService.setCustomer(0);
        }
    }

    private void buildAndSaveOrder(String paymentMethod, CustomerDTO customer, int usedPoints) {
        Order order = orderService.getCurrentOrder();
        order.setUserId(currentUserId);
        order.setStatus("PAID");
        order.setPaymentMethod(resolvePaymentMethodForDatabase(paymentMethod));

        if (isPayOSPayment(paymentMethod)) {
            handlePayOSPayment(order, customer, usedPoints);
        } else {
            completeCheckout(order, customer, usedPoints);
        }
    }

    /**
     * Kiểm tra phương thức thanh toán hiện tại có phải QR payOS không.
     */
    private boolean isPayOSPayment(String paymentMethod) {
        return paymentMethod != null
                && paymentMethod.trim().equalsIgnoreCase("QR Pay");
    }

    /**
     * Chuẩn hóa tên phương thức thanh toán trước khi lưu database.
     * QR Pay được lưu là PAYOS để dễ lọc hóa đơn và thống kê.
     */
    private String resolvePaymentMethodForDatabase(String paymentMethod) {
        if (isPayOSPayment(paymentMethod)) {
            return "PAYOS";
        }

        return paymentMethod;
    }

    /**
     * Tạo link thanh toán payOS thông qua payment-backend.
     * Sau khi mở trang QR, app sẽ tự polling trạng thái thanh toán.
     */
    private void handlePayOSPayment(Order order, CustomerDTO selectedCustomer, int usedPoints) {
        try {
            String description = "VTEA" + System.currentTimeMillis() / 1000;

            PayOSCreateResponse payment = payOSPaymentClient.createPayment(
                    order.getTotalAmount(),
                    description
            );

            payOSPaymentClient.openCheckoutUrl(payment.getCheckoutUrl());

            Alert waitingAlert = new Alert(Alert.AlertType.INFORMATION);
            waitingAlert.setTitle("Thanh toán payOS");
            waitingAlert.setHeaderText("Đang chờ khách thanh toán");
            waitingAlert.setContentText(
                    "Mã giao dịch: " + payment.getOrderCode()
                            + "\nSố tiền: " + FormatUtils.formatPrice(order.getTotalAmount())
                            + "\nVui lòng quét QR hoặc chuyển khoản trên trình duyệt."
                            + "\nHệ thống sẽ tự kiểm tra trạng thái thanh toán."
            );
            waitingAlert.show();

            startPayOSStatusPolling(payment, waitingAlert, order, selectedCustomer, usedPoints);
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Lỗi payOS", "Không thể tạo thanh toán payOS: " + e.getMessage());
        }
    }

    /**
     * Kiểm tra trạng thái giao dịch payOS mỗi 3 giây.
     * Khi payment-backend trả PAID thì mới lưu order vào database.
     */
    private void startPayOSStatusPolling(
            PayOSCreateResponse payment,
            Alert waitingAlert,
            Order order,
            CustomerDTO selectedCustomer,
            int usedPoints
    ) {
        // Tạo một bộ đếm thời gian (Timeline) chạy ngầm
        Timeline timeline = new Timeline();

        // Nếu nhân viên đóng thông báo chờ -> Hủy bỏ việc kiểm tra trạng thái
        waitingAlert.setOnCloseRequest(event -> timeline.stop());

        // Thiết lập vòng lặp: Cứ mỗi 3 giây sẽ thực hiện khối lệnh bên dưới 1 lần
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(3), event ->
                CompletableFuture
                        .supplyAsync(() -> {
                            try {
                                // Gọi API lên máy chủ PayOS để hỏi xem mã đơn này khách đã chuyển khoản chưa
                                return payOSPaymentClient.getStatus(payment.getOrderCode());
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .thenAccept(status -> Platform.runLater(() -> {
                            // Nếu khách ĐÃ CHUYỂN KHOẢN thành công
                            if ("PAID".equalsIgnoreCase(status)) {
                                timeline.stop();       // Dừng vòng lặp 3 giây
                                waitingAlert.close();  // Đóng thông báo chờ
                                completeCheckout(order, selectedCustomer, usedPoints); // Chốt đơn!
                                return;
                            }

                            // Nếu khách HỦY hoặc GIAO DỊCH LỖI
                            if ("CANCELLED".equalsIgnoreCase(status) || "FAILED".equalsIgnoreCase(status)) {
                                timeline.stop();
                                waitingAlert.close();
                                showErrorAlert("Thanh toán thất bại", "Trạng thái payOS: " + status);
                            }
                        }))
                        .exceptionally(ex -> {
                            System.err.println("Không thể kiểm tra trạng thái payOS: " + ex.getMessage());
                            return null;
                        })
        );

        timeline.getKeyFrames().add(keyFrame);
        timeline.setCycleCount(Timeline.INDEFINITE); // Cho vòng lặp chạy vô hạn (đến khi gọi stop() thì thôi)
        timeline.play();
    }

    /**
     * Lưu đơn hàng sau khi phương thức thanh toán đã hoàn tất.
     * Method này dùng chung cho tiền mặt và QR payOS.
     */
    private void completeCheckout(Order order, CustomerDTO selectedCustomer, int usedPoints) {
        // 1. Gọi OrderService để chính thức lưu Hóa đơn vào CSDL và xử lý điểm thưởng
        boolean success = orderService.checkoutCurrentOrder();

        if (success) {
            // Lấy ra số điểm mà khách vừa được cộng thêm từ hóa đơn này
            int awardedPoints = selectedCustomer != null ? orderService.getLastEarnedPoints() : 0;

            // 2. Hiển thị thông báo thành công và hỏi xem có muốn in/xuất file Bill (PDF) không
            boolean exportBill = DialogHelper.showSuccessWithBillButton(
                    "✓ Thanh toán thành công!",
                    "Tổng tiền: " + FormatUtils.formatPrice(order.getTotalAmount())
                            + "\nKhách hàng: "
                            + (selectedCustomer != null ? selectedCustomer.getFullName() : "Khách vãng lai")
                            + "\nPhương thức: "
                            + order.getPaymentMethod()
                            + "\nĐiểm đã dùng: "
                            + usedPoints
                            + "\nĐiểm cộng: "
                            + awardedPoints
            );

            // 3. Nếu nhân viên chọn "Xuất Bill", mở màn hình xem trước hóa đơn
            if (exportBill) {
                showBillPreviewAfterCheckout(order);
            }

            // 4. Xóa sạch giỏ hàng và làm mới màn hình để chuẩn bị đón khách tiếp theo
            orderService.clearCart();
            refreshCart();
        } else {
            showErrorAlert("Lỗi thanh toán", "Có lỗi xảy ra khi lưu đơn hàng!");
        }
    }

    /**
     * Mở bill preview sau khi thanh toán thành công.
     * Lưu ý: OrderService cần set lại orderId cho currentOrder sau khi insert database.
     */
    private void showBillPreviewAfterCheckout(Order order) {
        if (order == null || order.getOrderId() <= 0) {
            showErrorAlert(
                    "Lỗi bill preview",
                    "Không lấy được mã hóa đơn sau thanh toán. Kiểm tra lại OrderService có set order_id sau khi insert chưa."
            );
            return;
        }

        showBillPreview(order.getOrderId());
    }

    // ==================== PRODUCT CACHE / LOAD DATA ====================

    /**
     * Load cache dữ liệu POS ở background để UI không bị đứng.
     * Sau khi load xong thì quay lại JavaFX thread để render danh mục và sản phẩm.
     */
    private void loadPOSCacheAsync() {
        CompletableFuture
                .runAsync(() -> {
                    // Cache removed, do nothing
                })
                .thenRun(() -> Platform.runLater(() -> {
                    setupCategoryButtons();
                    displayProducts(productService.getAllActiveProducts());
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        ex.printStackTrace();
                        showErrorAlert("Lỗi", "Không thể tải dữ liệu POS!");
                    });

                    return null;
                });
    }

    /**
     * Hiển thị lại danh sách sản phẩm từ cache.
     */
    private void loadProductsFromDatabase() {
        displayProducts(productService.getAllActiveProducts());
    }

    /**
     * Chỉ hiển thị topping khi người dùng đang ở chế độ thêm topping.
     */
    private void showOnlyToppings() {
        setCategoryVisible(false);
        displayProducts(getToppings());
    }

    private List<ProductDTO> getToppings() {
        List<ProductDTO> toppings = new java.util.ArrayList<>();
        List<com.vtea.model.Topping> tList = orderService.getAllActiveToppings();
        for (com.vtea.model.Topping t : tList) {
            ProductDTO pd = new ProductDTO();
            pd.setProductId(t.getToppingId());
            pd.setName(t.getName());
            pd.setPrice(t.getPrice());
            pd.setCategoryName("Topping");
            pd.setImageUrl(t.getImageUrl());
            toppings.add(pd);
        }
        return toppings;
    }

    // ==================== CATEGORY EVENTS / DISPLAY ====================

    /**
     * Tạo các nút danh mục từ dữ liệu cache.
     */
    private void setupCategoryButtons() {
        if (categoryBar == null) {
            return;
        }

        categoryBar.getChildren().clear();

        Button allButton = createCategoryButton("Tất cả");
        allButton.getStyleClass().add("category-btn-active");
        allButton.setOnAction(event -> {
            setActiveButton(allButton);
            currentCategoryId = -1;
            filterProducts();
        });
        categoryBar.getChildren().add(allButton);

        try {
            List<CategoryDTO> categories = categoryService.getAllActiveCategories();

            for (CategoryDTO category : categories) {
                Button categoryButton = createCategoryButton(category.getName());

                categoryButton.setOnAction(event -> {
                    setActiveButton(categoryButton);
                    currentCategoryId = category.getCategoryId();
                    filterProducts();
                });

                categoryBar.getChildren().add(categoryButton);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Lỗi", "Không thể tải danh mục sản phẩm!");
        }
    }

    /**
     * Tạo button danh mục dùng chung cho thanh category.
     */
    private Button createCategoryButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("category-btn");
        return button;
    }

    /**
     * Lọc sản phẩm theo category_id và searchText.
     */
    private void filterProducts() {
        List<ProductDTO> all = productService.getAllActiveProducts();
        String searchText = searchField != null ? searchField.getText().toLowerCase().trim() : "";
        
        List<ProductDTO> filtered = all.stream()
            .filter(p -> currentCategoryId == -1 || p.getCategoryId() == currentCategoryId)
            .filter(p -> searchText.isEmpty() || p.getName().toLowerCase().contains(searchText))
            .collect(java.util.stream.Collectors.toList());
            
        displayProducts(filtered);
    }

    // ==================== PRODUCT EVENTS / DISPLAY ====================

    /**
     * Xử lý thêm sản phẩm vào giỏ hàng.
     */
    private void handleAddToCart(int productId, String productName, BigDecimal price) {
        try {
            orderService.addToCart(productId, productName, price, 1);
            refreshCart();

            // Hiệu ứng "Ting" và nảy giỏ hàng
            com.vtea.utils.SoundHelper.playTingSound();
            if (lblTotalAmount != null) {
                javafx.animation.ScaleTransition bounce = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(150), lblTotalAmount);
                bounce.setFromX(1.0);
                bounce.setFromY(1.0);
                bounce.setToX(1.3);
                bounce.setToY(1.3);
                bounce.setAutoReverse(true);
                bounce.setCycleCount(2);
                bounce.play();
            }

            showSuccessAlert(
                    "Thêm món thành công",
                    "Đã thêm " + productName + " vào giỏ hàng."
            );
        } catch (Exception e) {
            showErrorAlert("Lỗi", e.getMessage());
        }
    }

    /**
     * Render danh sách product card lên productGrid.
     */
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

    /**
     * Load một card sản phẩm từ ProductCard.fxml và bind dữ liệu sản phẩm vào UI.
     */
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
                lblPrice.setText(FormatUtils.formatPrice(product.getPrice()));
            }

            cardNode.setOnMouseClicked(event -> {
                if (toppingMode) {
                    handleAddToppingToTargetItem(product);
                } else {
                    playFlyingAnimation(cardNode, () -> {
                        handleAddToCart(
                                product.getProductId(),
                                product.getName(),
                                product.getPrice()
                        );
                    });
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

    /**
     * Khi đang ở chế độ thêm topping, click vào product card sẽ thêm product đó như topping.
     */
    private void handleAddToppingToTargetItem(ProductDTO product) {
        try {
            orderService.addToppingToItem(toppingTargetItem, product.getProductId());
            refreshCart();

            com.vtea.utils.SoundHelper.playTingSound();
            if (lblTotalAmount != null) {
                javafx.animation.ScaleTransition bounce = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(150), lblTotalAmount);
                bounce.setFromX(1.0);
                bounce.setFromY(1.0);
                bounce.setToX(1.2);
                bounce.setToY(1.2);
                bounce.setAutoReverse(true);
                bounce.setCycleCount(2);
                bounce.play();
            }

            showSuccessAlert("Thêm topping", product.getName() + " đã được thêm làm topping.");
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Lỗi", "Không thể thêm topping: " + e.getMessage());
        }
    }

    /**
     * Load ảnh sản phẩm từ đường dẫn trong database.
     * Hỗ trợ cả link http, file local và ảnh trong resources.
     */
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

    // ==================== CART EVENTS ====================

    /**
     * Xóa toàn bộ giỏ hàng sau khi người dùng xác nhận.
     */
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

    // ==================== CART DISPLAY ====================

    /**
     * Refresh lại giỏ hàng và tổng tiền.
     * Nếu item đang thêm topping bị xóa thì tự thoát khỏi topping mode.
     */
    private void refreshCart() {
        if (toppingMode && (toppingTargetItem == null || !orderService.getCartItems().contains(toppingTargetItem))) {
            toppingMode = false;
            toppingTargetItem = null;

            if (lblScreenTitle != null) {
                lblScreenTitle.setText("Bán hàng (POS)");
            }

            setCategoryVisible(true);
            loadProductsFromDatabase();
        }

        updateCartDisplay();
        updateTotalAmount();
    }

    /**
     * Render danh sách item trong giỏ hàng.
     */
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

    /**
     * Load một dòng giỏ hàng từ CartItem.fxml.
     */
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

    /**
     * Bind dữ liệu item vào giao diện cart item.
     */
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
            lblCartPrice.setText(FormatUtils.formatPrice(item.getUnitPrice()));
        }

        if (lblSubTotal != null) {
            lblSubTotal.setText(FormatUtils.formatPrice(item.getSubTotal()));
        }

        bindToppingDisplay(cartNode, item);
    }

    /**
     * Render danh sách topping của một item trong giỏ hàng.
     */
    private void bindToppingDisplay(HBox cartNode, OrderDetailDTO item) {
        VBox toppingsContainer = (VBox) cartNode.lookup("#toppingsContainer");

        if (toppingsContainer == null) {
            return;
        }

        toppingsContainer.getChildren().clear();

        if (item.getToppingQuantities() == null || item.getToppingQuantities().isEmpty()) {
            toppingsContainer.setVisible(false);
            toppingsContainer.setManaged(false);
            return;
        }

        toppingsContainer.setVisible(true);
        toppingsContainer.setManaged(true);

        for (var entry : item.getToppingQuantities().entrySet()) {
            int toppingId = entry.getKey();
            int qty = entry.getValue();

            ProductDTO topping = getToppings().stream()
                    .filter(t -> t.getProductId() == toppingId)
                    .findFirst()
                    .orElse(null);
            String toppingLabel = (topping != null)
                    ? topping.getName() + " (x" + qty + ")"
                    : "Topping#" + toppingId + " (x" + qty + ")";

            HBox row = new HBox();
            row.setSpacing(8);
            row.setUserData(toppingId);

            Label lbl = new Label(toppingLabel);
            lbl.getStyleClass().add("cart-item-topping");

            Button btnMinusT = new Button("-");
            Button btnPlusT = new Button("+");
            Button btnRemoveT = new Button("x");

            btnMinusT.getStyleClass().add("qty-btn");
            btnPlusT.getStyleClass().add("qty-btn");
            btnRemoveT.getStyleClass().add("btn-remove-topping");

            row.getChildren().addAll(lbl, btnMinusT, btnPlusT, btnRemoveT);
            toppingsContainer.getChildren().add(row);
        }
    }

    /**
     * Gắn event cho các nút trong cart item: tăng, giảm, xóa, thêm topping.
     */
    private void bindCartItemEvents(HBox cartNode, OrderDetailDTO item) {
        bindCartQuantityEvents(cartNode, item);
        bindToppingModeEvent(cartNode, item);
        bindToppingQuantityEvents(cartNode, item);
    }

    /**
     * Gắn event tăng, giảm, xóa món trong giỏ hàng.
     */
    private void bindCartQuantityEvents(HBox cartNode, OrderDetailDTO item) {
        Button btnMinus = (Button) cartNode.lookup("#btnMinus");
        Button btnPlus = (Button) cartNode.lookup("#btnPlus");
        Button btnRemove = (Button) cartNode.lookup("#btnRemove");

        if (btnPlus != null) {
            btnPlus.setOnAction(event -> {
                orderService.increaseQuantity(item);
                refreshCart();
            });
        }

        if (btnMinus != null) {
            btnMinus.setOnAction(event -> {
                orderService.decreaseQuantity(item);
                refreshCart();
            });
        }

        if (btnRemove != null) {
            btnRemove.setOnAction(event -> {
                orderService.removeFromCart(item);
                refreshCart();
            });
        }
    }

    /**
     * Gắn event bật/tắt chế độ thêm topping cho một món.
     */
    private void bindToppingModeEvent(HBox cartNode, OrderDetailDTO item) {
        Button btnTopping = (Button) cartNode.lookup("#btnTopping");

        if (btnTopping == null) {
            return;
        }

        if (toppingMode && toppingTargetItem == item) {
            btnTopping.setText("Đang thêm topping");

            if (!btnTopping.getStyleClass().contains("btn-topping-active")) {
                btnTopping.getStyleClass().add("btn-topping-active");
            }
        } else {
            btnTopping.setText("Thêm topping");
            btnTopping.getStyleClass().remove("btn-topping-active");
        }

        btnTopping.setOnAction(event -> {
            if (!toppingMode || toppingTargetItem != item) {
                toppingMode = true;
                toppingTargetItem = item;

                if (lblScreenTitle != null) {
                    lblScreenTitle.setText("Thêm topping");
                }

                showOnlyToppings();
            } else {
                toppingMode = false;
                toppingTargetItem = null;

                if (lblScreenTitle != null) {
                    lblScreenTitle.setText("Bán hàng (POS)");
                }

                setCategoryVisible(true);
                loadProductsFromDatabase();
            }

            refreshCart();
        });
    }

    /**
     * Gắn event tăng, giảm, xóa topping trong từng item giỏ hàng.
     */
    private void bindToppingQuantityEvents(HBox cartNode, OrderDetailDTO item) {
        VBox toppingsContainer = (VBox) cartNode.lookup("#toppingsContainer");

        if (toppingsContainer == null) {
            return;
        }

        for (javafx.scene.Node node : toppingsContainer.getChildren()) {
            if (node instanceof HBox hrow) {
                Object userData = hrow.getUserData();

                if (userData instanceof Integer toppingId) {
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

    /**
     * Hiển thị thông báo giỏ hàng trống.
     */
    private void showEmptyCartMessage() {
        if (cartItemsBox != null) {
            cartItemsBox.getChildren().clear();
            
            javafx.scene.layout.VBox emptyState = new javafx.scene.layout.VBox(16);
            emptyState.setAlignment(javafx.geometry.Pos.CENTER);
            emptyState.setPadding(new javafx.geometry.Insets(80, 0, 0, 0));
            
            org.kordamp.ikonli.javafx.FontIcon icon = new org.kordamp.ikonli.javafx.FontIcon("fth-shopping-cart");
            icon.setIconSize(64);
            icon.setIconColor(javafx.scene.paint.Color.web("#d6d3d1"));
            
            javafx.scene.control.Label lbl = new javafx.scene.control.Label("Chưa có món nào");
            lbl.setStyle("-fx-text-fill: #a8a29e; -fx-font-size: 16px; -fx-font-weight: bold;");
            
            emptyState.getChildren().addAll(icon, lbl);
            cartItemsBox.getChildren().add(emptyState);
        }
    }

    /**
     * Cập nhật subtotal, VAT và tổng tiền trên giao diện.
     */
    private void updateTotalAmount() {
        BigDecimal subtotal = calculateSubtotal();
        BigDecimal tax = subtotal.multiply(new BigDecimal("0.10"));
        BigDecimal total = orderService.getCurrentOrder().getTotalAmount();

        if (subtotalLabel != null) {
            subtotalLabel.setText(FormatUtils.formatPrice(subtotal));
        }

        if (taxLabel != null) {
            taxLabel.setText(FormatUtils.formatPrice(tax));
        }

        if (lblTotalAmount != null) {
            lblTotalAmount.setText(FormatUtils.formatPrice(total));
        }
    }

    /**
     * Tính tạm subtotal từ danh sách item trong giỏ hàng.
     */
    private BigDecimal calculateSubtotal() {
        BigDecimal subtotal = BigDecimal.ZERO;

        for (OrderDetailDTO item : orderService.getCartItems()) {
            subtotal = subtotal.add(item.getSubTotal());
        }

        return subtotal;
    }

    // ==================== DIALOG / SCREEN OPENING ====================

    /**
     * Mở dialog khách hàng trước khi thanh toán.
     * Dialog này cho phép chọn khách hàng hoặc bỏ qua tích điểm.
     */
    private CustomerDialogController showCustomerDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/vtea/view/CustomerDialog.fxml")
            );

            Parent root = loader.load();
            CustomerDialogController controller = loader.getController();

            BigDecimal subtotal = calculateSubtotal();
            BigDecimal vat = subtotal.multiply(new BigDecimal("0.10"));
            BigDecimal total = orderService.getCurrentOrder().getTotalAmount();

            controller.setOrderSummary(subtotal, vat, total);

            Stage stage = new Stage();
            stage.initStyle(javafx.stage.StageStyle.UNDECORATED);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            
            com.vtea.utils.DialogHelper.applyBlurBackground(true); com.vtea.utils.DialogHelper.animateDialog(root); try { stage.showAndWait(); } finally { com.vtea.utils.DialogHelper.applyBlurBackground(false); }

            return controller;
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Lỗi", "Không thể mở màn hình khách hàng: " + e.getMessage());
            return null;
        }
    }

    /**
     * Mở màn hình xem trước hóa đơn.
     * Method này chỉ xử lý UI, dữ liệu bill được lấy thông qua BillService.
     */
    private void showBillPreview(int orderId) {
        try {
            BillDTO bill = billService.getBillByOrderId(orderId);

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/vtea/view/bill-preview.fxml")
            );

            Parent root = loader.load();

            BillPreviewController controller = loader.getController();
            controller.setBill(bill);

            Stage stage = new Stage();
            stage.setTitle("Xem trước hóa đơn");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            
            com.vtea.utils.DialogHelper.applyBlurBackground(true); com.vtea.utils.DialogHelper.animateDialog(root); try { stage.showAndWait(); } finally { com.vtea.utils.DialogHelper.applyBlurBackground(false); }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Lỗi", "Không thể mở bill preview: " + e.getMessage());
        }
    }

    // ==================== UI HELPERS ====================

    /**
     * Đổi trạng thái active cho nút danh mục đang được chọn.
     */
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

    /**
     * Ẩn hoặc hiện thanh danh mục.
     * Khi thêm topping thì ẩn category để chỉ hiển thị danh sách topping.
     */
    private void setCategoryVisible(boolean visible) {
        if (categoryScroll != null) {
            categoryScroll.setVisible(visible);
            categoryScroll.setManaged(visible);
            return;
        }

        if (categoryBar != null) {
            categoryBar.setVisible(visible);
            categoryBar.setManaged(visible);
        }
    }



    // ==================== ALERT HELPERS ====================

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

