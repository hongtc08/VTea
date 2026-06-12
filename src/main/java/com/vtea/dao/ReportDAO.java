package com.vtea.dao;

import com.vtea.dto.*;
import com.vtea.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {
    /**
     * Lấy dữ liệu Top 5 món bán chạy nhất (Dùng vẽ BarChart)
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
     * Lấy doanh thu theo từng Danh mục (Dùng vẽ PieChart)
     */
    public List<CategoryRevenueDTO> getRevenueByCategory(LocalDate startDate, LocalDate endDate) {
        List<CategoryRevenueDTO> list = new ArrayList<>();
        String sql = """
                SELECT c.name AS category_name, SUM(od.quantity * od.unit_price) AS total_revenue 
                FROM order_detail od 
                JOIN product p ON od.product_id = p.product_id 
                JOIN category c ON p.category_id = c.category_id 
                JOIN `order` o ON od.order_id = o.order_id 
                WHERE o.status = 'PAID' AND DATE(o.created_at) BETWEEN ? AND ? 
                GROUP BY c.category_id, c.name 
                ORDER BY total_revenue DESC
                """;

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

    /**
     * Thống kê Doanh thu theo từng Nhân viên lập bill
     */
    public List<StaffSalesDTO> getRevenueByStaff(LocalDate startDate, LocalDate endDate) {
        List<StaffSalesDTO> list = new ArrayList<>();

        String sql = """
                SELECT u.full_name, COUNT(o.order_id) AS total_orders, SUM(o.total_amount) AS total_revenue
                FROM `order` o
                JOIN `user` u ON o.user_id = u.user_id
                WHERE o.status = 'PAID' AND DATE(o.created_at) BETWEEN ? AND ?
                GROUP BY u.user_id, u.full_name
                ORDER BY total_revenue DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, startDate);
            ps.setObject(2, endDate);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StaffSalesDTO dto = new StaffSalesDTO();
                    dto.setStaffName(rs.getString("full_name"));
                    dto.setTotalOrders(rs.getInt("total_orders"));
                    dto.setTotalRevenue(rs.getBigDecimal("total_revenue"));
                    list.add(dto);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy doanh thu theo nhân viên: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Thống kê Doanh thu theo Phương thức thanh toán
     */
    public List<PaymentMethodRevenueDTO> getRevenueByPaymentMethod(LocalDate startDate, LocalDate endDate) {
        List<PaymentMethodRevenueDTO> list = new ArrayList<>();

        String sql = """
                SELECT o.payment_method, COUNT(o.order_id) AS total_orders, SUM(o.total_amount) AS total_revenue
                FROM `order` o        
                WHERE o.status = 'PAID' AND o.created_at BETWEEN ? AND ?
                GROUP BY o.payment_method
                ORDER BY total_revenue DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, startDate);
            ps.setObject(2, endDate);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PaymentMethodRevenueDTO dto = new PaymentMethodRevenueDTO();
                    dto.setPaymentMethod(rs.getString("payment_method"));
                    dto.setTotalOrders(rs.getInt("total_orders"));
                    dto.setTotalRevenue(rs.getBigDecimal("total_revenue"));
                    list.add(dto);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy doanh thu theo phương thức thanh toán: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * doanh thu trong một khoảng thời gian
     */
    public List<TimeRevenueDTO> getRevenueByDate(LocalDate startDate, LocalDate endDate){
        List<TimeRevenueDTO> list = new ArrayList<>();

        String sql = """
                SELECT DATE(created_at) AS order_date, COUNT(o.order_id) AS total_orders, SUM(o.total_amount) AS total_revenue
                FROM `order` o 
                WHERE status = 'PAID' AND DATE(created_at) BETWEEN ? AND ?
                GROUP BY DATE(created_at)
                ORDER BY order_date ASC
                """;

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setObject(1, startDate);
            ps.setObject(2, endDate);

            try(ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TimeRevenueDTO dto = new TimeRevenueDTO();
                    dto.setTimeLabel(rs.getString("order_date"));
                    dto.setTotalOrders(rs.getInt("total_orders"));
                    dto.setTotalRevenue(rs.getBigDecimal("total_revenue"));
                    list.add(dto);
                }
            }
        } catch(SQLException e){
            System.err.println("Lỗi thống kê doanh thu theo ngày: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * doanh thu theo TỪNG THÁNG trong một năm cụ thể
     */
    public List<TimeRevenueDTO> getRevenueByMonth(int year){
        List<TimeRevenueDTO> list = new ArrayList<>();

        String sql = """
                SELECT MONTH(created_at) AS month, COUNT(order_id) AS total_orders, SUM(total_amount) AS total_revenue
                FROM `order` o 
                WHERE status = 'PAID' AND YEAR(created_at) = ?
                GROUP BY MONTH(created_at)
                ORDER BY month ASC
                """;

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setInt(1, year);

            try(ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    TimeRevenueDTO dto = new TimeRevenueDTO();
                    int m = rs.getInt("month");
                    dto.setTimeLabel("Tháng " + (m < 10 ? "0" + m : m));
                    dto.setTotalOrders(rs.getInt("total_orders"));
                    dto.setTotalRevenue(rs.getBigDecimal("total_revenue"));
                    list.add(dto);
                }
            }
        } catch (SQLException e){
            System.err.println("Lỗi thống kê doanh thu theo tháng: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }
}

