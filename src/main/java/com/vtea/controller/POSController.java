package com.vtea.controller;

import com.vtea.model.Category;
import com.vtea.model.Order;
import com.vtea.model.OrderDetail;
import com.vtea.model.Product;
import com.vtea.utils.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.*;
import java.util.Objects;

public class POSController implements Initializable {

    @FXML private TextField searchField;
    @FXML private HBox categoryBar;
    @FXML private FlowPane productGrid;
    @FXML private VBox cartItemsBox;
    @FXML private Label subtotalLabel;
    @FXML private Label taxLabel;
    @FXML private Label totalLabel;
    @FXML private Label cartEmptyLabel;
    @FXML private VBox checkoutSection;
    @FXML private Label cartCountLabel;

    private ObservableList<Product> allProducts = FXCollections.observableArrayList();
    private final Map<Integer, CartEntry> cartMap = new LinkedHashMap<>();
    private String selectedCategory = "Tất cả";
    private List<String> categories = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadProducts();
        setupSearch();
        updateCartDisplay();
    }

    private void loadProducts() {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null) {
                loadProductsFromDB(conn);
                loadCategoriesFromDB(conn);
            } else {
                loadMockProducts();
            }
        } catch (SQLException e) {
            System.out.println("⚠️ Dùng mock data POS: " + e.getMessage());
            loadMockProducts();
        }
        buildCategoryBar();
        refreshProductGrid();
    }

    private void loadProductsFromDB(Connection conn) throws SQLException {
        String sql = "SELECT p.*, c.name as category_name FROM products p " +
                     "LEFT JOIN categories c ON p.category_id = c.category_id " +
                     "WHERE p.is_available = 'true' OR p.is_available = '1' ORDER BY c.name, p.name";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Product p = new Product();
                p.setProductId(rs.getInt("product_id"));
                p.setName(rs.getString("name"));
                p.setPrice(rs.getBigDecimal("price"));
                p.setCategoryId(rs.getInt("category_id"));
                p.setIsAvailable(rs.getString("is_available"));
                // Lưu tên category vào imageUrl tạm thời (vì Product chưa có categoryName)
                p.setImageUrl(rs.getString("category_name"));
                allProducts.add(p);
            }
        }
    }

    private void loadCategoriesFromDB(Connection conn) throws SQLException {
        categories.clear();
        categories.add("Tất cả");
        String sql = "SELECT DISTINCT name FROM categories ORDER BY name";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) categories.add(rs.getString("name"));
        }
    }

    private void loadMockProducts() {
        categories = Arrays.asList("Tất cả", "Trà sữa", "Cafe", "Trà", "Đặc biệt");
        String[][] data = {
            {"1","Trà sữa trân châu","50000","Trà sữa"},
            {"2","Trà sữa ô long","55000","Trà sữa"},
            {"3","Trà sữa thái","55000","Trà sữa"},
            {"4","Trà sữa matcha","58000","Trà sữa"},
            {"5","Cafe sữa","40000","Cafe"},
            {"6","Cafe đen đá","25000","Cafe"},
            {"7","Bạc xỉu","35000","Cafe"},
            {"8","Cappuccino","45000","Cafe"},
            {"9","Matcha latte","55000","Đặc biệt"},
            {"10","Trà đào cam sả","50000","Trà"},
            {"11","Trà chanh","30000","Trà"},
            {"12","Trà vải","35000","Trà"},
        };
        for (String[] row : data) {
            Product p = new Product();
            p.setProductId(Integer.parseInt(row[0]));
            p.setName(row[1]);
            p.setPrice(new BigDecimal(row[2]));
            p.setImageUrl(row[3]); // dùng imageUrl làm category name
            allProducts.add(p);
        }
    }

    private void buildCategoryBar() {
        categoryBar.getChildren().clear();
        for (String cat : categories) {
            Button btn = new Button(cat);
            btn.getStyleClass().add(cat.equals(selectedCategory) ? "category-btn-active" : "category-btn");
            btn.setOnAction(e -> {
                selectedCategory = cat;
                buildCategoryBar();
                refreshProductGrid();
            });
            categoryBar.getChildren().add(btn);
        }
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> refreshProductGrid());
    }

    private void refreshProductGrid() {
        productGrid.getChildren().clear();
        String query = searchField.getText().toLowerCase().trim();

        for (Product p : allProducts) {
            String catName = p.getImageUrl() != null ? p.getImageUrl() : "";
            boolean matchCat = selectedCategory.equals("Tất cả") || catName.equals(selectedCategory);
            boolean matchSearch = p.getName().toLowerCase().contains(query);
            if (matchCat && matchSearch) {
                productGrid.getChildren().add(buildProductCard(p));
            }
        }
    }

    private VBox buildProductCard(Product product) {
        VBox card = new VBox(8);
        card.getStyleClass().add("product-card");
        card.setPrefWidth(160);
        card.setPadding(new Insets(12));

        // Icon emoji area
        Label icon = new Label("☕");
        icon.setStyle("-fx-font-size: 36px; -fx-background-color: #fdf2ee; -fx-background-radius: 10; -fx-padding: 15 20;");
        icon.setMaxWidth(Double.MAX_VALUE);
        icon.setAlignment(Pos.CENTER);

        Label name = new Label(product.getName());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1c1917; -fx-wrap-text: true;");
        name.setMaxWidth(Double.MAX_VALUE);

        String catName = product.getImageUrl() != null ? product.getImageUrl() : "";
        Label cat = new Label(catName);
        cat.setStyle("-fx-font-size: 11px; -fx-text-fill: #78716c;");

        Label price = new Label(formatCurrency(product.getPrice()));
        price.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #78350f;");

        card.getChildren().addAll(icon, name, cat, price);
        card.setOnMouseClicked(e -> addToCart(product));
        return card;
    }

    private void addToCart(Product product) {
        int id = product.getProductId();
        if (cartMap.containsKey(id)) {
            cartMap.get(id).quantity++;
        } else {
            cartMap.put(id, new CartEntry(product, 1));
        }
        updateCartDisplay();
    }

    private void updateCartDisplay() {
        cartItemsBox.getChildren().clear();

        if (cartMap.isEmpty()) {
            cartEmptyLabel.setVisible(true);
            checkoutSection.setVisible(false);
            cartCountLabel.setText("0");
            return;
        }

        cartEmptyLabel.setVisible(false);
        checkoutSection.setVisible(true);

        BigDecimal subtotal = BigDecimal.ZERO;
        int totalItems = 0;

        for (CartEntry entry : cartMap.values()) {
            cartItemsBox.getChildren().add(buildCartRow(entry));
            subtotal = subtotal.add(entry.product.getPrice().multiply(BigDecimal.valueOf(entry.quantity)));
            totalItems += entry.quantity;
        }

        BigDecimal tax = subtotal.multiply(new BigDecimal("0.10"));
        BigDecimal total = subtotal.add(tax);

        subtotalLabel.setText(formatCurrency(subtotal));
        taxLabel.setText(formatCurrency(tax));
        totalLabel.setText(formatCurrency(total));
        cartCountLabel.setText(String.valueOf(totalItems));
    }

    private HBox buildCartRow(CartEntry entry) {
        HBox row = new HBox(8);
        row.getStyleClass().add("cart-row");
        row.setPadding(new Insets(8, 10, 8, 10));
        row.setAlignment(Pos.CENTER_LEFT);

        // Tên + giá
        VBox info = new VBox(2);
        Label name = new Label(entry.product.getName());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #1c1917;");
        name.setMaxWidth(110);
        name.setWrapText(true);
        Label unitPrice = new Label(formatCurrency(entry.product.getPrice()));
        unitPrice.setStyle("-fx-font-size: 11px; -fx-text-fill: #78716c;");
        info.getChildren().addAll(name, unitPrice);
        HBox.setHgrow(info, Priority.ALWAYS);

        // Nút -
        Button minus = new Button("−");
        minus.getStyleClass().add("qty-btn");
        minus.setOnAction(e -> { entry.quantity--; if (entry.quantity <= 0) cartMap.remove(entry.product.getProductId()); updateCartDisplay(); });

        Label qty = new Label(String.valueOf(entry.quantity));
        qty.setStyle("-fx-font-weight: bold; -fx-min-width: 24; -fx-alignment: CENTER; -fx-text-fill: #1c1917;");

        // Nút +
        Button plus = new Button("+");
        plus.getStyleClass().add("qty-btn");
        plus.setOnAction(e -> { entry.quantity++; updateCartDisplay(); });

        // Tổng
        Label lineTotal = new Label(formatCurrency(entry.product.getPrice().multiply(BigDecimal.valueOf(entry.quantity))));
        lineTotal.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #78350f; -fx-min-width: 80; -fx-alignment: CENTER_RIGHT;");

        // Xóa
        Button remove = new Button("✕");
        remove.getStyleClass().add("remove-btn");
        remove.setOnAction(e -> { cartMap.remove(entry.product.getProductId()); updateCartDisplay(); });

        row.getChildren().addAll(info, minus, qty, plus, lineTotal, remove);
        return row;
    }

    @FXML
    private void handleClearCart() {
        cartMap.clear();
        updateCartDisplay();
    }

    @FXML
    private void handleCheckout() {
        if (cartMap.isEmpty()) return;

        BigDecimal subtotal = cartMap.values().stream()
                .map(e -> e.product.getPrice().multiply(BigDecimal.valueOf(e.quantity)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tax = subtotal.multiply(new BigDecimal("0.10"));
        BigDecimal total = subtotal.add(tax);

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận thanh toán");
        confirm.setHeaderText("Xác nhận đơn hàng?");
        confirm.setContentText(String.format(
                "Tạm tính: %s\nVAT (10%%): %s\n\nTổng cộng: %s",
                formatCurrency(subtotal), formatCurrency(tax), formatCurrency(total)
        ));
        confirm.getDialogPane().getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/com/vtea/css/styles.css")).toExternalForm()
        );

        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                saveOrderToDB(total);
                cartMap.clear();
                updateCartDisplay();
                showSuccessAlert(total);
            }
        });
    }

    private void saveOrderToDB(BigDecimal total) {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return;
            String sql = "INSERT INTO orders (user_id, total_amount, status, payment_method, created_at) VALUES (?, ?, 'completed', 'cash', NOW())";
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                int userId = (LoginController.currentUser != null) ? LoginController.currentUser.getUserId() : 1;
                ps.setInt(1, userId);
                ps.setBigDecimal(2, total);
                ps.executeUpdate();

                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) {
                    int orderId = keys.getInt(1);
                    saveOrderDetails(conn, orderId);
                }
            }
        } catch (SQLException e) {
            System.out.println("⚠️ Không lưu được đơn hàng vào DB: " + e.getMessage());
        }
    }

    private void saveOrderDetails(Connection conn, int orderId) throws SQLException {
        String sql = "INSERT INTO order_details (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (CartEntry entry : cartMap.values()) {
                ps.setInt(1, orderId);
                ps.setInt(2, entry.product.getProductId());
                ps.setInt(3, entry.quantity);
                ps.setBigDecimal(4, entry.product.getPrice());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void showSuccessAlert(BigDecimal total) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thanh toán thành công");
        alert.setHeaderText("✅ Đơn hàng đã được thanh toán!");
        alert.setContentText("Tổng tiền: " + formatCurrency(total) + "\nCảm ơn quý khách!");
        alert.showAndWait();
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0₫";
        return String.format("%,.0f₫", amount.doubleValue());
    }

    // CartEntry inner class
    private static class CartEntry {
        Product product;
        int quantity;
        CartEntry(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }
    }
}
