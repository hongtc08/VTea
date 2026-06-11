package com.vtea.controller;

import com.vtea.dto.DashboardSummaryDTO;
import com.vtea.dto.IngredientDTO;
import com.vtea.dto.ProductSalesDTO;
import com.vtea.service.DashboardService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardController {

    private final DashboardService dashboardService = new DashboardService();

    @FXML
    private Label revenueTodayLabel;

    @FXML
    private Label ordersTodayLabel;

    @FXML
    private Label customersTodayLabel;

    @FXML
    private VBox vboxRecentOrders;

    @FXML
    private VBox vboxTopProducts;

    @FXML
    private VBox vboxLowStock;

    // Hàm khởi tạo dashboard
    @FXML
    public void initialize() {
        System.out.println("Init Dashboard Controller...");
        refreshDashboard();
    }

    // Refresh lại toàn bộ dữ liệu dashboard
    public void refreshDashboard() {
        loadTodaySummary();

        vboxRecentOrders.getChildren().clear();
        vboxTopProducts.getChildren().clear();
        vboxLowStock.getChildren().clear();

        // danh sách đơn hàng gần đây
        try {
            List<Map<String, Object>> recentOrders = dashboardService.getRecentOrders(5);
            for (Map<String, Object> order : recentOrders) {
                String orderId = (String) order.get("orderId");
                String customerName = (String) order.get("customerName");
                String productNames = (String) order.get("productNames");
                BigDecimal totalAmount = (BigDecimal) order.get("totalAmount");
                String time = (String) order.get("time");

                vboxRecentOrders.getChildren().add(
                        loadOrderItem(orderId, customerName, productNames,
                                formatCurrency(totalAmount), time)
                );
            }

            // Handle empty state
            if (recentOrders.isEmpty()) {
                Label emptyLabel = new Label("Không có đơn hàng nào");
                emptyLabel.setStyle("-fx-text-fill: #999;");
                vboxRecentOrders.getChildren().add(emptyLabel);
            }
        } catch (Exception e) {
            System.err.println("Error loading recent orders: " + e.getMessage());
            e.printStackTrace();
        }

        // top món bán chạy
        try {
            List<ProductSalesDTO> topProducts = dashboardService.getTopProductsForDashboard(5);
            int rank = 1;
            for (ProductSalesDTO product : topProducts) {
                vboxTopProducts.getChildren().add(
                        loadTopProductItem(
                                String.valueOf(rank++),
                                product.getProductName(),
                                String.valueOf(product.getTotalQuantitySold()),
                                formatCurrency(product.getTotalRevenue())
                        )
                );
            }

            // Handle empty state
            if (topProducts.isEmpty()) {
                Label emptyLabel = new Label("Chưa có dữ liệu");
                emptyLabel.setStyle("-fx-text-fill: #999;");
                vboxTopProducts.getChildren().add(emptyLabel);
            }
        } catch (Exception e) {
            System.err.println("Error loading top products: " + e.getMessage());
            e.printStackTrace();
        }

        // danh sách nguyên liệu sắp hết
        try {
            List<IngredientDTO> lowStockItems = dashboardService.getLowStockIngredients(5);
            for (IngredientDTO ingredient : lowStockItems) {
                String currentStock = ingredient.getStockQty() + " " + ingredient.getUnit();
                String minStock = ingredient.getMinStock() + " " + ingredient.getUnit();

                vboxLowStock.getChildren().add(
                        loadLowStockItem(ingredient.getName(), currentStock, minStock)
                );
            }

            // Handle empty state - show success message
            if (lowStockItems.isEmpty()) {
                Label emptyLabel = new Label("✓ Tất cả nguyên liệu đều đủ");
                emptyLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                vboxLowStock.getChildren().add(emptyLabel);
            }
        } catch (Exception e) {
            System.err.println("Error loading low stock ingredients: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Load dữ liệu thật cho các card tổng quan
    private void loadTodaySummary() {
        DashboardSummaryDTO summary = dashboardService.getTodaySummary();

        revenueTodayLabel.setText(formatCurrency(summary.getTotalRevenue()));
        ordersTodayLabel.setText(String.valueOf(summary.getTotalOrders()));
        customersTodayLabel.setText(String.valueOf(summary.getTotalCustomers()));
    }

    // Format tiền VND
    private String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }

        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(amount) + "đ";
    }

    // Hàm load thông tin vào OrderItem
    private HBox loadOrderItem(String id, String name, String items, String total, String time) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vtea/view/OrderItem.fxml"));
            HBox itemNode = loader.load();

            ((Label) itemNode.lookup("#lblOrderId")).setText(id);
            ((Label) itemNode.lookup("#lblCustomer")).setText("- " + name);
            ((Label) itemNode.lookup("#lblItems")).setText(items);
            ((Label) itemNode.lookup("#lblTotal")).setText(total);
            ((Label) itemNode.lookup("#lblTime")).setText(time);

            return itemNode;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Hàm load thông tin vào TopProductItem
    private HBox loadTopProductItem(String rank, String productName, String soldCount, String totalRevenue) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vtea/view/TopProductItem.fxml"));
            HBox itemNode = loader.load();

            ((Label) itemNode.lookup("#lblRank")).setText(rank);
            ((Label) itemNode.lookup("#lblProductName")).setText(productName);
            ((Label) itemNode.lookup("#lblSold")).setText(soldCount + " đã bán");
            ((Label) itemNode.lookup("#lblRevenue")).setText(totalRevenue);

            return itemNode;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Hàm load thông tin vào LowStockItem
    private HBox loadLowStockItem(String ingredientName, String currentStock, String minStock) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vtea/view/LowStockItem.fxml"));
            HBox itemNode = loader.load();

            ((Label) itemNode.lookup("#lblIngredientName")).setText(ingredientName);
            ((Label) itemNode.lookup("#lblStockInfo")).setText("Còn lại: " + currentStock + " (Tối thiểu: " + minStock + ")");

            return itemNode;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}