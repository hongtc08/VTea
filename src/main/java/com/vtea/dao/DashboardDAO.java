package com.vtea.dao;

import com.vtea.dto.CategoryRevenueDTO;
import com.vtea.dto.DashboardSummaryDTO;
import com.vtea.dto.ProductSalesDTO;
import com.vtea.dto.IngredientDTO;
import com.vtea.utils.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardDAO {
    /**
     * 1. Lấy dữ liệu tổng quan cho các thẻ số liệu (Cards)
     */
    public DashboardSummaryDTO getDashBoardSummary(LocalDateTime startDate, LocalDateTime endDate){
        DashboardSummaryDTO summary = new DashboardSummaryDTO(BigDecimal.ZERO, 0, 0, 0);

        // Query 1: Tính tổng doanh thu và tổng số đơn hàng đã thanh toán (PAID)
        String revenueSql = """
        SELECT COALESCE(SUM(total_amount), 0) AS total_revenue,
               COUNT(order_id) AS total_orders
        FROM `order`
        WHERE status = 'PAID'
          AND created_at BETWEEN ? AND ?
        """;

        // Query 2: Đếm lượng khách hàng định danh đến mua hàng (Loại bỏ trùng lặp)
        String customerSql = """
        SELECT COUNT(DISTINCT customer_id) AS total_customer
        FROM `order`
        WHERE status = 'PAID'
          AND customer_id IS NOT NULL
          AND created_at BETWEEN ? AND ?
        """;
        // Query 3: Đếm số lượng nguyên liệu chạm mốc cảnh báo đỏ (Không phụ thuộc thời gian)
        String stockSql = "SELECT COUNT(*) AS low_stock_count FROM ingredient WHERE is_available = true AND stock_qty <= min_stock";

        try(Connection conn = DBConnection.getConnection()){

            // Query 1
            try (PreparedStatement ps = conn.prepareStatement(revenueSql)) {
                ps.setObject(1, startDate);
                ps.setObject(2, endDate);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        BigDecimal rev = rs.getBigDecimal("total_revenue");
                        summary.setTotalRevenue(rev != null ? rev : BigDecimal.ZERO);
                        summary.setTotalOrders(rs.getInt("total_orders"));
                    }
                }
            }

            // Query 2
            try (PreparedStatement ps = conn.prepareStatement(customerSql)) {
                ps.setObject(1, startDate);
                ps.setObject(2, endDate);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        summary.setTotalCustomers(rs.getInt("total_customer"));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // Query 3
            try (PreparedStatement ps = conn.prepareStatement(stockSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    summary.setLowStockIngredientCount(rs.getInt("low_stock_count"));
                }
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi tải dữ liệu Dashboard Summary: " + e.getMessage());
            e.printStackTrace();
        }

        return summary;
    }

    /**
     * 2. Lấy dữ liệu Top 5 món bán chạy nhất (Dùng vẽ BarChart)
     */
    public List<ProductSalesDTO> getTopSellingProducts(LocalDate startDate, LocalDate endDate) {
        List<ProductSalesDTO> list = new ArrayList<>();
        String sql = "SELECT p.name AS product_name, SUM(od.quantity) AS total_sold " +
                "FROM order_detail od " +
                "JOIN product p ON od.product_id = p.product_id " +
                "JOIN `order` o ON od.order_id = o.order_id " +
                "WHERE o.status = 'PAID' AND DATE(o.created_at) BETWEEN ? AND ? " +
                "GROUP BY p.product_id, p.name " +
                "ORDER BY total_sold DESC " +
                "LIMIT 5";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, startDate);
            ps.setObject(2, endDate);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProductSalesDTO dto = new ProductSalesDTO();
                    dto.setProductName(rs.getString("product_name"));
                    dto.setTotalQuantitySold(rs.getInt("total_sold"));
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
     * 4. Lấy danh sách đơn hàng gần đây nhất (Dùng hiển thị trên Dashboard)
     * Chỉ lấy các đơn hàng đã thanh toán (PAID)
     * Kèm danh sách sản phẩm trong mỗi đơn hàng
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
        WHERE o.status = 'PAID'
        GROUP BY o.order_id, o.created_at, c.full_name, o.total_amount
        ORDER BY o.created_at DESC
        LIMIT ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> orderMap = new java.util.HashMap<>();
                    orderMap.put("orderId", "#" + String.format("%03d", rs.getInt("order_id")));
                    orderMap.put("customerName", rs.getString("customer_name"));
                    orderMap.put("productNames", rs.getString("product_names"));
                    orderMap.put("totalAmount", rs.getBigDecimal("total_amount"));

                    LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
                    orderMap.put("time", createdAt.format(DateTimeFormatter.ofPattern("HH:mm")));

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
     * 5. Lấy danh sách sản phẩm bán chạy - Kèm doanh thu
     * Hiển thị số lượng bán và tổng doanh thu từng sản phẩm
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
        WHERE o.status = 'PAID' 
        GROUP BY p.product_id, p.name
        ORDER BY total_revenue DESC
        LIMIT ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);

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
     * 6. Lấy danh sách nguyên liệu sắp hết
     */
    public List<IngredientDTO> getLowStockIngredients(int limit) {
        List<IngredientDTO> list = new ArrayList<>();

        String sql = """
        SELECT i.*, u.full_name AS staff_name
        FROM ingredient i
        LEFT JOIN `user` u ON i.updated_by = u.user_id
        WHERE i.is_available = true AND i.stock_qty <= i.min_stock
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
     * 3. Lấy doanh thu theo từng Danh mục (Dùng vẽ PieChart)
     */
    public List<CategoryRevenueDTO> getRevenueByCategory(LocalDate startDate, LocalDate endDate) {
        List<CategoryRevenueDTO> list = new ArrayList<>();
        String sql = "SELECT c.name AS category_name, SUM(od.quantity * od.unit_price) AS total_revenue " +
                "FROM order_detail od " +
                "JOIN product p ON od.product_id = p.product_id " +
                "JOIN category c ON p.category_id = c.category_id " +
                "JOIN `order` o ON od.order_id = o.order_id " +
                "WHERE o.status = 'PAID' AND DATE(o.created_at) BETWEEN ? AND ? " +
                "GROUP BY c.category_id, c.name " +
                "ORDER BY total_revenue DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, startDate);
            ps.setObject(2, endDate);

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
}
