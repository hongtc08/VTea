package com.vtea.dao;

import com.vtea.model.Order;
import com.vtea.model.OrderDetail;
import com.vtea.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {
    /**
     * Xử lý lưu hóa đơn và chi tiết hóa đơn (Transaction)
     * Trả về true nếu lưu thành công, false nếu thất bại
     */
    public boolean checkoutOrder(Order order, List<OrderDetail> details){
        String insertOrderSQL = "INSERT INTO 'order' (user_id, customer_id, total_amount, created_at, status, payment_method) VALUES (?, ?, ?, CURRENT_TIMESTAMP, ?, ?)";
        String insertDetailSQL = "INSERT INTO order_detail (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";

        Connection conn = null;

        try{
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            int generatedOrderId = -1;

            // 1. INSERT VÀO BẢNG ORDER
            // Thêm tham số Statement.RETURN_GENERATED_KEYS để lấy ID vừa tạo
            try(PreparedStatement psOrder = conn.prepareStatement(insertOrderSQL, Statement.RETURN_GENERATED_KEYS)) {
                psOrder.setInt(1, order.getUserId());

                // Xử lý nếu khách hàng có ID hợp lệ (lớn hơn 0)
                if (order.getCustomerId() > 0) {
                    psOrder.setInt(2, order.getCustomerId());
                } else {
                    psOrder.setNull(2, java.sql.Types.INTEGER);
                }

                psOrder.setBigDecimal(3, order.getTotalAmount());
                psOrder.setString(4, order.getStatus()); // Ví dụ: "PAID"
                psOrder.setString(5, order.getPaymentMethod());

                psOrder.executeUpdate();

                // Lấy order_id vừa được database tự động tạo ra
                try (ResultSet rs = psOrder.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedOrderId = rs.getInt(1);
                    } else {
                        throw new SQLException("Không thể lấy ID của Order vừa tạo.");
                    }
                }
            }

            // 2. INSERT VÀO BẢNG ORDER_DETAIL
            try (PreparedStatement psDetail = conn.prepareStatement(insertDetailSQL)) {
                for (OrderDetail detail : details) {
                    psDetail.setInt(1, generatedOrderId); // Dùng ID vừa lấy được ở trên
                    psDetail.setInt(2, detail.getProductId());
                    psDetail.setInt(3, detail.getQuantity());
                    psDetail.setBigDecimal(4, detail.getUnitPrice());

                    psDetail.addBatch(); // Đưa vào hàng chờ để chạy 1 lần
                }
                psDetail.executeBatch(); // Thực thi lưu toàn bộ chi tiết
            }

            // NẾU TẤT CẢ ĐỀU ỔN -> XÁC NHẬN LƯU
            conn.commit();
            return true;

        } catch (SQLException e) {
            // NẾU CÓ BẤT KỲ LỖI NÀO Ở ORDER HAY DETAIL -> HỦY BỎ TẤT CẢ (ROLLBACK)
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("Transaction bị lỗi, đã Rollback an toàn!");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;

        } finally {
            // TRẢ LẠI TRẠNG THÁI CŨ VÀ ĐÓNG KẾT NỐI
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    //////////////// CÁC HÀM DÙNG CHO THỐNG KÊ ////////////////////
    /**
     * Lấy danh sách các món trong một hóa đơn cụ thể (Order Details).
     */
    public List<OrderDetail> getOrderDetailsByOrderId(int orderId) {
        List<OrderDetail> details = new ArrayList<>();
        String query = "SELECT od.*, p.name AS product_name " +
                "FROM order_detail od " +
                "JOIN product p ON od.product_id = p.product_id " +
                "WHERE od.order_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderDetail detail = new OrderDetail();
                    detail.setDetailId(rs.getInt("detail_id"));
                    detail.setOrderId(rs.getInt("order_id"));
                    detail.setProductId(rs.getInt("product_id"));
                    detail.setQuantity(rs.getInt("quantity"));
                    detail.setUnitPrice(rs.getBigDecimal("unit_price"));

                    detail.setProductName(rs.getString("product_name"));

                    details.add(detail);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy chi tiết hóa đơn: " + e.getMessage());
            e.printStackTrace();
        }
        return details;
    }

    /**
     * Truy xuất lịch sử đơn hàng để tra cứu theo khoảng thời gian.
     */
    public List<Order> getOrderHistory(Date startDate, Date endDate){
        List<Order> orderList = new ArrayList<>();
        String query = "SELECT * FROM 'order' WHERE DATE(created_at) BETWEEN ? AND ? ORDER BY created_at DESC";

        try(Connection conn = DBConnection.getConnection();
           PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setDate(1, new java.sql.Date(startDate.getTime()));
            ps.setDate(2, new java.sql.Date(endDate.getTime()));

            try(ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order();
                    order.setOrderId(rs.getInt("order_id"));
                    order.setUserId(rs.getInt("user_id"));

                    // Xử lý customer_id có thể null
                    int customerId = rs.getInt("customer_id");
                    if (!rs.wasNull()) {
                        order.setCustomerId(customerId);
                    }

                    order.setTotalAmount(rs.getBigDecimal("total_amount"));
                    order.setCreatedAt(rs.getTimestamp("created_at"));
                    order.setStatus(rs.getString("status"));
                    order.setPaymentMethod(rs.getString("payment_method"));

                    orderList.add(order);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy lịch sử hóa đơn: " + e.getMessage());
            e.printStackTrace();
        }

        return orderList;
    }

    /**
     * Tính tổng doanh thu từ các hóa đơn đã thanh toán (PAID) trong khoảng thời gian.
     */
    public double getRevenue(Date startDate, Date endDate) {
        double totalRevenue = 0.0;
        // Chỉ tính tiền những hóa đơn có trạng thái PAID
        String sql = "SELECT SUM(total_amount) AS revenue FROM `order` WHERE status = 'PAID' AND DATE(created_at) BETWEEN ? AND ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, new java.sql.Date(startDate.getTime()));
            ps.setDate(2, new java.sql.Date(endDate.getTime()));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    totalRevenue = rs.getDouble("revenue");
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tính doanh thu: " + e.getMessage());
            e.printStackTrace();
        }
        return totalRevenue;
    }
}
