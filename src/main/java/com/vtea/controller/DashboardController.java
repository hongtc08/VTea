package com.vtea.controller;

import com.vtea.model.Order;
import com.vtea.model.Product;
import com.vtea.utils.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    // Stat labels
    @FXML private Label revenueTodayLabel;
    @FXML private Label ordersTodayLabel;
    @FXML private Label customersTodayLabel;
    @FXML private Label growthLabel;

    // Recent orders table
    @FXML private TableView<OrderRow> recentOrdersTable;
    @FXML private TableColumn<OrderRow, String> colOrderId;
    @FXML private TableColumn<OrderRow, String> colCustomer;
    @FXML private TableColumn<OrderRow, String> colTotal;
    @FXML private TableColumn<OrderRow, String> colStatus;
    @FXML private TableColumn<OrderRow, String> colTime;

    // Top products list
    @FXML private ListView<String> topProductsList;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTableColumns();
        loadDashboardData();
    }

    private void setupTableColumns() {
        colOrderId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customer"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));

        // Style cột Status
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equalsIgnoreCase("completed") || item.equalsIgnoreCase("paid")) {
                        setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
                    } else if (item.equalsIgnoreCase("pending")) {
                        setStyle("-fx-text-fill: #d97706; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #6b7280;");
                    }
                }
            }
        });
    }

    private void loadDashboardData() {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                loadMockData();
                return;
            }
            loadStatsFromDB(conn);
            loadRecentOrdersFromDB(conn);
            loadTopProductsFromDB(conn);
        } catch (SQLException e) {
            System.out.println("⚠️ Không thể load dữ liệu DB, dùng mock data: " + e.getMessage());
            loadMockData();
        }
    }

    private void loadStatsFromDB(Connection conn) throws SQLException {
        String today = LocalDate.now().toString();

        // Doanh thu hôm nay
        String revSql = "SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE DATE(created_at) = ?";
        try (PreparedStatement ps = conn.prepareStatement(revSql)) {
            ps.setString(1, today);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                BigDecimal revenue = rs.getBigDecimal(1);
                revenueTodayLabel.setText(formatCurrency(revenue));
            }
        }

        // Số đơn hàng hôm nay
        String orderSql = "SELECT COUNT(*) FROM orders WHERE DATE(created_at) = ?";
        try (PreparedStatement ps = conn.prepareStatement(orderSql)) {
            ps.setString(1, today);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) ordersTodayLabel.setText(String.valueOf(rs.getInt(1)));
        }

        // Số khách hàng hôm nay
        String custSql = "SELECT COUNT(DISTINCT customer_id) FROM orders WHERE DATE(created_at) = ? AND customer_id IS NOT NULL";
        try (PreparedStatement ps = conn.prepareStatement(custSql)) {
            ps.setString(1, today);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) customersTodayLabel.setText(String.valueOf(rs.getInt(1)));
        }

        growthLabel.setText("+12.5%");
    }

    private void loadRecentOrdersFromDB(Connection conn) throws SQLException {
        String sql = """
                SELECT o.order_id, COALESCE(c.full_name, 'Khách lẻ') as customer_name,
                       o.total_amount, o.status, DATE_FORMAT(o.created_at, '%H:%i') as order_time
                FROM orders o
                LEFT JOIN customers c ON o.customer_id = c.customer_id
                ORDER BY o.created_at DESC LIMIT 8
                """;
        ObservableList<OrderRow> rows = FXCollections.observableArrayList();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                rows.add(new OrderRow(
                        "#" + rs.getInt("order_id"),
                        rs.getString("customer_name"),
                        formatCurrency(rs.getBigDecimal("total_amount")),
                        rs.getString("status"),
                        rs.getString("order_time")
                ));
            }
        }
        recentOrdersTable.setItems(rows);
    }

    private void loadTopProductsFromDB(Connection conn) throws SQLException {
        String sql = """
                SELECT p.name, SUM(od.quantity) as total_sold, SUM(od.quantity * od.unit_price) as revenue
                FROM order_details od
                JOIN products p ON od.product_id = p.product_id
                GROUP BY p.product_id, p.name
                ORDER BY total_sold DESC LIMIT 5
                """;
        ObservableList<String> items = FXCollections.observableArrayList();
        int rank = 1;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                items.add(String.format("%d.  %s   —   %d đã bán   |   %s",
                        rank++,
                        rs.getString("name"),
                        rs.getInt("total_sold"),
                        formatCurrency(rs.getBigDecimal("revenue"))
                ));
            }
        }
        topProductsList.setItems(items);
    }

    private void loadMockData() {
        revenueTodayLabel.setText("12,450,000₫");
        ordersTodayLabel.setText("156");
        customersTodayLabel.setText("89");
        growthLabel.setText("+12.5%");

        ObservableList<OrderRow> mockOrders = FXCollections.observableArrayList(
                new OrderRow("#001", "Nguyễn Văn A", "85,000₫", "completed", "10:30"),
                new OrderRow("#002", "Trần Thị B", "55,000₫", "completed", "10:45"),
                new OrderRow("#003", "Lê Văn C", "95,000₫", "pending", "11:00"),
                new OrderRow("#004", "Phạm Thị D", "25,000₫", "completed", "11:15"),
                new OrderRow("#005", "Khách lẻ", "110,000₫", "completed", "11:30")
        );
        recentOrdersTable.setItems(mockOrders);

        topProductsList.setItems(FXCollections.observableArrayList(
                "1.  Trà sữa trân châu   —   45 đã bán   |   2,250,000₫",
                "2.  Cafe sữa            —   38 đã bán   |   1,520,000₫",
                "3.  Matcha latte        —   32 đã bán   |   1,760,000₫",
                "4.  Trà đào cam sả      —   28 đã bán   |   1,400,000₫",
                "5.  Cafe đen đá         —   25 đã bán   |     625,000₫"
        ));
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0₫";
        return String.format("%,.0f₫", amount.doubleValue());
    }

    // ============================
    // Inner class — Row model cho TableView
    // ============================
    public static class OrderRow {
        private final String orderId;
        private final String customer;
        private final String total;
        private final String status;
        private final String time;

        public OrderRow(String orderId, String customer, String total, String status, String time) {
            this.orderId = orderId;
            this.customer = customer;
            this.total = total;
            this.status = status;
            this.time = time;
        }

        public String getOrderId() { return orderId; }
        public String getCustomer() { return customer; }
        public String getTotal() { return total; }
        public String getStatus() { return status; }
        public String getTime() { return time; }
    }
}
