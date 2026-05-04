package com.vtea.controller;

import com.vtea.dao.ProductDAO;
import com.vtea.dao.OrderDAO;
import com.vtea.dto.ProductDTO;
import com.vtea.dto.OrderDetailDTO;
import com.vtea.model.Order;
import com.vtea.model.OrderDetail;
import com.vtea.service.OrderService;
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
    private ProductDAO productDAO = new ProductDAO();
    private OrderDAO orderDAO = new OrderDAO();

    // Giả sử lấy từ session/login
    private int currentUserId = 1;

    // FXML Components
    @FXML private FlowPane productGrid;
    @FXML private Button btnAll, btnTraSua, btnCafe, btnTra, btnDacBiet;
    @FXML private TableView<OrderDetailDTO> cartTableView;
    @FXML private Label lblTotalAmount;
    @FXML private ComboBox<String> cmbPaymentMethod;

    @FXML
    public void initialize() {
        System.out.println("✅ Initializing POS Controller...");

        try {
            // 1. Tải sản phẩm từ Database
            loadProductsFromDatabase();

            // 2. Khởi tạo payment methods
            cmbPaymentMethod.setItems(
                    FXCollections.observableArrayList("Tiền mặt", "Thẻ ghi nợ", "QR Pay")
            );
            cmbPaymentMethod.setValue("Tiền mặt");

        } catch (Exception e) {
            System.err.println("❌ Lỗi khi khởi tạo POS: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==========================================
    // 1. LOAD DỮ LIỆU TỪ DATABASE
    // ==========================================

    private void loadProductsFromDatabase() {
        try {
            List<ProductDTO> products = productDAO.getAllActiveProduct();
            displayProducts(products);
            System.out.println("✅ Tải " + products.size() + " sản phẩm từ Database thành công!");
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi tải sản phẩm từ Database: " + e.getMessage());
            showErrorAlert("Lỗi", "Không thể tải danh sách sản phẩm!");
        }
    }

    // ==========================================
    // 2. XỬ LÝ THÊM VÀO GIỎ
    // ==========================================

    public void handleAddToCart(int productId, String productName, BigDecimal price) {
        try {
            // Thêm vào service
            orderService.addToCart(productId, productName, price, 1);

            // Cập nhật giao diện
            updateCartDisplay();
            updateTotalAmount();

            showSuccessAlert("Thêm vào giỏ",
                    productName + " ✓\nGiá: " + formatPrice(price));

            System.out.println("✅ Đã thêm " + productName + " vào giỏ!");
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi thêm vào giỏ: " + e.getMessage());
        }
    }

    // ==========================================
    // 3. XỬ LÝ XÓA GIỎ
    // ==========================================

    @FXML
    private void handleClearCart(ActionEvent event) {
        if (orderService.getCartItems().isEmpty()) {
            showInfoAlert("Thông báo", "Giỏ hàng đã trống!");
            return;
        }

        if (showConfirmDialog("Xác nhận", "Bạn có chắc muốn xóa toàn bộ giỏ hàng?")) {
            orderService = new OrderService(); // Reset
            updateCartDisplay();
            updateTotalAmount();
            showInfoAlert("Thành công", "Giỏ hàng đã được xóa!");
        }
    }

    // ==========================================
    // 4. XỬ LÝ THANH TOÁN
    // ==========================================

    @FXML
    private void handleCheckout(ActionEvent event) {
        try {
            // Kiểm tra giỏ hàng
            if (orderService.getCartItems().isEmpty()) {
                showErrorAlert("Lỗi", "Giỏ hàng trống! Vui lòng thêm sản phẩm.");
                return;
            }

            // Chuẩn bị dữ liệu Order
            Order order = orderService.getCurrentOrder();
            order.setUserId(currentUserId);
            order.setStatus("PAID");
            order.setPaymentMethod(cmbPaymentMethod.getValue());

            // Lấy danh sách chi tiết
            List<OrderDetail> details = orderService.getDetailsForCheckout(0);

            // Lưu vào Database (Transaction)
            if (orderDAO.checkoutOrder(order, details)) {
                showSuccessAlert("✓ Thanh toán thành công!",
                        "Tổng tiền: " + formatPrice(order.getTotalAmount()));

                // Reset giỏ
                orderService = new OrderService();
                updateCartDisplay();
                updateTotalAmount();

                System.out.println("✅ Đơn hàng đã được lưu thành công!");
            } else {
                showErrorAlert("Lỗi thanh toán", "Có lỗi xảy ra khi lưu đơn hàng!");
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi thanh toán: " + e.getMessage());
            showErrorAlert("Lỗi", "Có lỗi xảy ra: " + e.getMessage());
        }
    }

    // ==========================================
    // 5. LỌC THEO DANH MỤC
    // ==========================================

    @FXML
    private void filterAll(ActionEvent event) {
        setActiveButton(btnAll);
        loadProductsFromDatabase();
    }

    @FXML
    private void filterTraSua(ActionEvent event) {
        setActiveButton(btnTraSua);
        filterByCategory(2); // Assuming categoryId = 2 for Trà sữa
    }

    @FXML
    private void filterCafe(ActionEvent event) {
        setActiveButton(btnCafe);
        filterByCategory(1); // categoryId = 1 for Cafe
    }

    @FXML
    private void filterTra(ActionEvent event) {
        setActiveButton(btnTra);
        filterByCategory(3); // categoryId = 3 for Trà
    }

    @FXML
    private void filterDacBiet(ActionEvent event) {
        setActiveButton(btnDacBiet);
        filterByCategory(4); // categoryId = 4 for Đặc biệt
    }

    private void filterByCategory(int categoryId) {
        try {
            List<ProductDTO> filtered = productDAO.getProductByCategory(categoryId);
            displayProducts(filtered);
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi lọc danh mục: " + e.getMessage());
        }
    }

    // ==========================================
    // 6. HIỂN THỊ GIAO DIỆN
    // ==========================================

    private void displayProducts(List<ProductDTO> products) {
        productGrid.getChildren().clear();
        for (ProductDTO p : products) {
            VBox card = loadProductCard(
                    p.getProductId(),
                    p.getName(),
                    p.getCategoryName(),
                    p.getPrice(),
                    p.getImageUrl()
            );
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

    // ==========================================
    // 7. LOAD PRODUCT CARD FXML
    // ==========================================

    private VBox loadProductCard(int productId, String name, String category,
                                 BigDecimal price, String imagePath) {
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

            // Xử lý khi click "Thêm vào giỏ"
            if (btnAdd != null) {
                btnAdd.setOnAction(e -> handleAddToCart(productId, name, price));
            }

            // Tải ảnh
            if (imagePath != null && !imagePath.isEmpty()) {
                try {
                    Image image = new Image(getClass().getResourceAsStream(imagePath));
                    imgProduct.setImage(image);
                } catch (Exception e) {
                    System.out.println("⚠️ Không tìm thấy ảnh: " + imagePath);
                }
            }

            return cardNode;
        } catch (IOException e) {
            System.err.println("❌ Lỗi load ProductCard FXML: " + e.getMessage());
            return null;
        }
    }

    // ==========================================
    // 8. HELPER METHODS
    // ==========================================

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