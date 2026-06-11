package com.vtea.dao;

import com.vtea.dto.CategoryRevenueDTO;
import com.vtea.dto.DashboardSummaryDTO;
import com.vtea.dto.IngredientDTO;
import com.vtea.dto.ProductSalesDTO;
import com.vtea.utils.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardDAO {

    private static final String PAID_STATUS = "PAID";

    /**
     * Lấy dữ liệu tổng quan cho các thẻ số liệu trên Dashboard:
     * doanh thu, số đơn, số khách, số nguyên liệu sắp hết.
     */
    public DashboardSummaryDTO getDashBoardSummary(LocalDateTime startDate, LocalDateTime endDate) {
        DashboardSummaryDTO summary = new DashboardSummaryDTO(BigDecimal.ZERO, 0, 0, 0);

        String revenueSql = """
                SELECT COALESCE(SUM(total_amount), 0) AS total_revenue,
                       COUNT(order_id) AS total_orders
                FROM `order`
                WHERE status = ?
                  AND created_at BETWEEN ? AND ?
                """;

        String customerSql = """
                SELECT COUNT(DISTINCT customer_id) AS total_customer
                FROM `order`
                WHERE status = ?
                  AND customer_id IS NOT NULL
                  AND created_at BETWEEN ? AND ?
                """;

        String stockSql = """
                SELECT COUNT(*) AS low_stock_count
                FROM ingredient
                WHERE is_available = true
                  AND stock_qty <= min_stock
                """;

        try (Connection conn = DBConnection.getConnection()) {

            try (PreparedStatement ps = conn.prepareStatement(revenueSql)) {
                ps.setString(1, PAID_STATUS);
                ps.setTimestamp(2, Timestamp.valueOf(startDate));
                ps.setTimestamp(3, Timestamp.valueOf(endDate));

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        BigDecimal revenue = rs.getBigDecimal("total_revenue");
                        summary.setTotalRevenue(revenue != null ? revenue : BigDecimal.ZERO);
                        summary.setTotalOrders(rs.getInt("total_orders"));
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(customerSql)) {
                ps.setString(1, PAID_STATUS);
                ps.setTimestamp(2, Timestamp.valueOf(startDate));
                ps.setTimestamp(3, Timestamp.valueOf(endDate));

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        summary.setTotalCustomers(rs.getInt("total_customer"));
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(stockSql);
                 ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    summary.setLowStockIngredientCount(rs.getInt("low_stock_count"));
                }
            } catch (SQLException e) {
                // Không để lỗi cảnh báo kho làm hỏng phần doanh thu/số đơn/số khách
                System.err.println("Bỏ qua cảnh báo kho trên Dashboard: " + e.getMessage());
                summary.setLowStockIngredientCount(0);
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi tải dữ liệu Dashboard Summary: " + e.getMessage());
            e.printStackTrace();
        }

        return summary;
    }

    /**
     * Lấy danh sách đơn hàng gần đây nhất.
     */
    public List<Map<String, Object>> getRecentOrders(int limit) {
        List<Map<String, Object>> list = new ArrayList<>();

        String sql = """
                SELECT
                    o.order_id,
                    o.created_at,
                    COALESCE(c.full_name, 'Khách vãng lai') AS customer_name,
                    o.total_amount,
                    GROUP_CONCAT(p.name SEPARATOR ', ') AS product_names
                FROM `order` o
                LEFT JOIN customer c ON o.customer_id = c.customer_id
                JOIN order_detail od ON o.order_id = od.order_id
                JOIN product p ON od.product_id = p.product_id
                WHERE o.status = ?
                GROUP BY o.order_id, o.created_at, c.full_name, o.total_amount
                ORDER BY o.created_at DESC
                LIMIT ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, PAID_STATUS);
            ps.setInt(2, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> orderMap = new HashMap<>();

                    orderMap.put("orderId", "#" + String.format("%03d", rs.getInt("order_id")));
                    orderMap.put("customerName", rs.getString("customer_name"));
                    orderMap.put("productNames", rs.getString("product_names"));
                    orderMap.put("totalAmount", rs.getBigDecimal("total_amount"));

                    Timestamp createdAtTimestamp = rs.getTimestamp("created_at");
                    if (createdAtTimestamp != null) {
                        LocalDateTime createdAt = createdAtTimestamp.toLocalDateTime();
                        orderMap.put("time", createdAt.format(DateTimeFormatter.ofPattern("HH:mm")));
                    } else {
                        orderMap.put("time", "");
                    }

                    list.add(orderMap);
                }
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách đơn hàng gần đây: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Lấy danh sách sản phẩm bán chạy nhất để hiển thị trên Dashboard.
     * Không lọc ngày, lấy trên toàn bộ hóa đơn đã thanh toán.
     */
    public List<ProductSalesDTO> getTopProductsForDashboard(int limit) {
        List<ProductSalesDTO> list = new ArrayList<>();

        String sql = """
                SELECT
                    p.name AS product_name,
                    SUM(od.quantity) AS total_sold,
                    SUM(od.quantity * od.unit_price) AS total_revenue
                FROM order_detail od
                JOIN product p ON od.product_id = p.product_id
                JOIN `order` o ON od.order_id = o.order_id
                WHERE o.status = ?
                GROUP BY p.product_id, p.name
                ORDER BY total_sold DESC
                LIMIT ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, PAID_STATUS);
            ps.setInt(2, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProductSalesDTO dto = new ProductSalesDTO();

                    dto.setProductName(rs.getString("product_name"));
                    dto.setTotalQuantitySold(rs.getInt("total_sold"));
                    dto.setTotalRevenue(rs.getBigDecimal("total_revenue"));

                    list.add(dto);
                }
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách sản phẩm bán chạy: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Lấy danh sách nguyên liệu sắp hết.
     */
    public List<IngredientDTO> getLowStockIngredients(int limit) {
        List<IngredientDTO> list = new ArrayList<>();

        String sql = """
                SELECT i.*, u.full_name AS staff_name
                FROM ingredient i
                LEFT JOIN `user` u ON i.updated_by = u.user_id
                WHERE i.is_available = true
                  AND i.stock_qty <= i.min_stock
                ORDER BY i.stock_qty ASC
                LIMIT ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    IngredientDTO dto = new IngredientDTO();

                    dto.setIngredientId(rs.getInt("ingredient_id"));
                    dto.setName(rs.getString("name"));
                    dto.setUnit(rs.getString("unit"));
                    dto.setStockQty(rs.getBigDecimal("stock_qty"));
                    dto.setAvailable(rs.getBoolean("is_available"));
                    dto.setMinStock(rs.getBigDecimal("min_stock"));

                    Timestamp lastUpdated = rs.getTimestamp("last_updated");
                    if (lastUpdated != null) {
                        dto.setLastUpdated(lastUpdated.toLocalDateTime());
                    }

                    int updatedBy = rs.getInt("updated_by");
                    if (!rs.wasNull()) {
                        dto.setUpdatedBy(updatedBy);
                    }

                    dto.setStaffName(rs.getString("staff_name"));

                    list.add(dto);
                }
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách nguyên liệu sắp hết: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Lấy top món bán chạy trong một khoảng ngày.
     */
    public List<ProductSalesDTO> getTopSellingProducts(LocalDate startDate, LocalDate endDate) {
        List<ProductSalesDTO> list = new ArrayList<>();

        String sql = """
                SELECT
                    p.name AS product_name,
                    SUM(od.quantity) AS total_sold,
                    SUM(od.quantity * od.unit_price) AS total_revenue
                FROM order_detail od
                JOIN product p ON od.product_id = p.product_id
                JOIN `order` o ON od.order_id = o.order_id
                WHERE o.status = ?
                  AND DATE(o.created_at) BETWEEN ? AND ?
                GROUP BY p.product_id, p.name
                ORDER BY total_sold DESC
                LIMIT 5
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, PAID_STATUS);
            ps.setDate(2, Date.valueOf(startDate));
            ps.setDate(3, Date.valueOf(endDate));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProductSalesDTO dto = new ProductSalesDTO();

                    dto.setProductName(rs.getString("product_name"));
                    dto.setTotalQuantitySold(rs.getInt("total_sold"));
                    dto.setTotalRevenue(rs.getBigDecimal("total_revenue"));

                    list.add(dto);
                }
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy top món bán chạy theo ngày: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Lấy doanh thu theo từng danh mục trong một khoảng ngày.
     */
    public List<CategoryRevenueDTO> getRevenueByCategory(LocalDate startDate, LocalDate endDate) {
        List<CategoryRevenueDTO> list = new ArrayList<>();

        String sql = """
                SELECT
                    c.name AS category_name,
                    SUM(od.quantity * od.unit_price) AS total_revenue
                FROM order_detail od
                JOIN product p ON od.product_id = p.product_id
                JOIN category c ON p.category_id = c.category_id
                JOIN `order` o ON od.order_id = o.order_id
                WHERE o.status = ?
                  AND DATE(o.created_at) BETWEEN ? AND ?
                GROUP BY c.category_id, c.name
                ORDER BY total_revenue DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, PAID_STATUS);
            ps.setDate(2, Date.valueOf(startDate));
            ps.setDate(3, Date.valueOf(endDate));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CategoryRevenueDTO dto = new CategoryRevenueDTO();

                    dto.setCategoryName(rs.getString("category_name"));
                    dto.setTotalRevenue(rs.getBigDecimal("total_revenue"));

                    list.add(dto);
                }
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy doanh thu theo danh mục: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Đếm số hóa đơn đã thanh toán trong một ngày.
     */
    public int countPaidOrdersByDate(LocalDate date) {
        String sql = """
                SELECT COUNT(order_id) AS total_orders
                FROM `order`
                WHERE status = ?
                  AND DATE(created_at) = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, PAID_STATUS);
            ps.setDate(2, Date.valueOf(date));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total_orders");
                }
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi đếm hóa đơn theo ngày: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Đếm số hóa đơn đã thanh toán trong một tháng.
     */
    public int countPaidOrdersByMonth(int year, int month) {
        String sql = """
                SELECT COUNT(order_id) AS total_orders
                FROM `order`
                WHERE status = ?
                  AND YEAR(created_at) = ?
                  AND MONTH(created_at) = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, PAID_STATUS);
            ps.setInt(2, year);
            ps.setInt(3, month);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total_orders");
                }
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi đếm hóa đơn theo tháng: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    public int countCompletedOrdersByDate(LocalDate date) {
        return countPaidOrdersByDate(date);
    }

    public int countCompletedOrdersByMonth(int year, int month) {
        return countPaidOrdersByMonth(year, month);
    }
}