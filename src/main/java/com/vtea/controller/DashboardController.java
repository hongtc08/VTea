package com.vtea.controller;

import com.vtea.dto.OrderDTO;
import com.vtea.service.DashboardService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;

public class DashboardController {

    @FXML private Label revenueTodayLabel;
    @FXML private Label ordersTodayLabel;
    @FXML private Label customersTodayLabel;
    @FXML private Label growthLabel;
    @FXML private VBox vboxRecentOrders;
    @FXML private VBox vboxTopProducts;

    private final DashboardService dashboardService = new DashboardService();

    @FXML
    public void initialize() {
        loadDashboardData();
    }

    private void loadDashboardData() {
        BigDecimal revenue = dashboardService.getTodayRevenue();
        int orders = dashboardService.getTodayOrderCount();
        int customers = dashboardService.getTodayCustomerCount();

        if (revenueTodayLabel != null) {
            revenueTodayLabel.setText(formatMoney(revenue));
        }
        if (ordersTodayLabel != null) {
            ordersTodayLabel.setText(String.valueOf(orders));
        }
        if (customersTodayLabel != null) {
            customersTodayLabel.setText(String.valueOf(customers));
        }
        if (growthLabel != null) {
            growthLabel.setText(orders > 0 ? "Hoạt động" : "—");
        }

        loadRecentOrders();
        loadTopProducts();
    }

    private void loadRecentOrders() {
        if (vboxRecentOrders == null) {
            return;
        }
        vboxRecentOrders.getChildren().clear();

        List<OrderDTO> orders = dashboardService.getRecentOrdersToday(10);
        if (orders.isEmpty()) {
            vboxRecentOrders.getChildren().add(new Label("Chưa có đơn hàng hôm nay"));
            return;
        }

        SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm");
        for (OrderDTO order : orders) {
            String id = "#" + String.format("%03d", order.getOrderId());
            String customer = order.getCustomerName() != null ? order.getCustomerName() : "Khách vãng lai";
            String items = order.getPaymentMethod() != null ? order.getPaymentMethod() : "Đơn hàng";
            String total = formatMoney(order.getTotalAmount());
            String time = order.getCreatedAt() != null ? timeFmt.format(order.getCreatedAt()) : "—";

            HBox row = loadOrderItem(id, customer, items, total, time);
            if (row != null) {
                vboxRecentOrders.getChildren().add(row);
            }
        }
    }

    private void loadTopProducts() {
        if (vboxTopProducts == null) {
            return;
        }
        vboxTopProducts.getChildren().clear();

        List<Object[]> topProducts = dashboardService.getTopProductsToday(5);
        if (topProducts.isEmpty()) {
            vboxTopProducts.getChildren().add(new Label("Chưa có dữ liệu bán hàng hôm nay"));
            return;
        }

        int rank = 1;
        for (Object[] row : topProducts) {
            String name = (String) row[0];
            int sold = (Integer) row[1];
            BigDecimal revenue = row[2] instanceof BigDecimal b ? b : BigDecimal.ZERO;

            HBox item = loadTopProductItem(
                    String.valueOf(rank),
                    name,
                    String.valueOf(sold),
                    formatMoney(revenue)
            );
            if (item != null) {
                vboxTopProducts.getChildren().add(item);
            }
            rank++;
        }
    }

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

    private String formatMoney(BigDecimal amount) {
        if (amount == null) {
            return "0đ";
        }
        return String.format("%,.0fđ", amount);
    }
}
