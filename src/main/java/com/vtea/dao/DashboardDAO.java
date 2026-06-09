package com.vtea.dao;

import com.vtea.dto.CategoryRevenueDTO;
import com.vtea.dto.DashboardSummaryDTO;
import com.vtea.dto.ProductSalesDTO;
import com.vtea.utils.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
