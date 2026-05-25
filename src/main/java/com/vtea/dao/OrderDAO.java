package com.vtea.dao;

import com.vtea.dto.OrderDTO;
import com.vtea.dto.OrderDetailDTO;
import com.vtea.model.Order;
import com.vtea.model.OrderDetail;
import com.vtea.utils.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.Date;

public class OrderDAO {
    /**
     * Xử lý lưu hóa đơn và chi tiết hóa đơn (Transaction)
     * Trả về true nếu lưu thành công, false nếu thất bại
     */
    public boolean checkoutOrder(Order order, List<OrderDetail> details){
        String insertOrderSQL = "INSERT INTO `order` (user_id, customer_id, total_amount, created_at, status, payment_method) VALUES (?, ?, ?, CURRENT_TIMESTAMP, ?, ?)";
        String insertDetailSQL = "INSERT INTO order_detail (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
        String insertToppingSQL = "INSERT INTO order_detail_topping (detail_id, topping_id, unit_price, quantity) VALUES (?, ?, (SELECT price FROM topping WHERE topping_id = ?), ?)";

        Connection conn = null;

        try{
            conn = DBConnection.getConnection();
            if (conn == null) {
                return false;
            }
            conn.setAutoCommit(false);

            int generatedOrderId = -1;

            // 1. INSERT VÀO BẢNG ORDER
            // Thêm tham số Statement.RETURN_GENERATED_KEYS để lấy ID vừa tạo
            try(PreparedStatement psOrder = conn.prepareStatement(insertOrderSQL, Statement.RETURN_GENERATED_KEYS)) {
                psOrder.setInt(1, order.getUserId());

                // Xử lý nếu khách hàng có ID hợp lệ (lớn hơn 0)
                if (order.getCustomerId() != null && order.getCustomerId() > 0) {
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

            // 2. INSERT VÀO BẢNG ORDER_DETAIL VÀ ORDER_DETAIL_TOPPING
            try (PreparedStatement psDetail = conn.prepareStatement(insertDetailSQL, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psTopping = conn.prepareStatement(insertToppingSQL)) {

                for (OrderDetail detail : details) {
                    psDetail.setInt(1, generatedOrderId); // Dùng ID vừa lấy được ở trên
                    psDetail.setInt(2, detail.getProductId());
                    psDetail.setInt(3, detail.getQuantity());
                    psDetail.setBigDecimal(4, detail.getUnitPrice());
                    psDetail.executeUpdate();

                    int generatedDetailId = -1;
                    try (ResultSet rsDetail = psDetail.getGeneratedKeys()) {
                        if (rsDetail.next())
                            generatedDetailId = rsDetail.getInt(1);
                    }

                    // 3. NẾU LY NƯỚC CÓ TOPPING -> INSERT VÀO BẢNG ORDER_DETAIL_TOPPING
                    if (detail.getToppingQuantities() != null && !detail.getToppingQuantities().isEmpty()) {
                        // Duyệt qua từng cặp (Topping ID - Số lượng) trong Map
                        for (Map.Entry<Integer, Integer> entry : detail.getToppingQuantities().entrySet()) {
                            int toppingId = entry.getKey();
                            int toppingQty = entry.getValue();

                            psTopping.setInt(1, generatedDetailId);
                            psTopping.setInt(2, toppingId);
                            psTopping.setInt(3, toppingId); // Truyền lần 2 cho câu subquery lấy giá
                            psTopping.setInt(4, toppingQty);

                            psTopping.addBatch();
                        }
                        psTopping.executeBatch();
                    }
                }
            }

            // NẾU TẤT CẢ ĐỀU ỔN -> XÁC NHẬN LƯU
            conn.commit();
            return true;

        } catch (SQLException e) {
            rollbackQuietly(conn);
            e.printStackTrace();
            return false;

        } finally {
            closeQuietly(conn);
        }
    }

    /**
     * Thanh toán đơn hàng và cập nhật điểm khách hàng trong cùng một transaction.
     */
    public boolean checkoutOrderWithRewardPoints(Order order, List<OrderDetail> details, int rewardPointsDelta) {
        if (rewardPointsDelta == 0) {
            return checkoutOrder(order, details);
        }

        Integer customerId = order.getCustomerId();
        if (customerId == null || customerId <= 0) {
            return checkoutOrder(order, details);
        }

        String insertOrderSQL = "INSERT INTO `order` (user_id, customer_id, total_amount, created_at, status, payment_method) VALUES (?, ?, ?, CURRENT_TIMESTAMP, ?, ?)";
        String insertDetailSQL = "INSERT INTO order_detail (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
        String insertToppingSQL = "INSERT INTO order_detail_topping (detail_id, topping_id, unit_price, quantity) VALUES (?, ?, (SELECT price FROM topping WHERE topping_id = ?), ?)";
        String updatePointsSQL = "UPDATE customer SET reward_points = reward_points + ? WHERE customer_id = ? AND reward_points + ? >= 0";

        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            if (conn == null) {
                return false;
            }
            conn.setAutoCommit(false);

            int generatedOrderId = insertOrderAndDetails(conn, order, details, insertOrderSQL, insertDetailSQL, insertToppingSQL);
            if (generatedOrderId < 0) {
                throw new SQLException("Không thể tạo đơn hàng.");
            }

            try (PreparedStatement psPoints = conn.prepareStatement(updatePointsSQL)) {
                psPoints.setInt(1, rewardPointsDelta);
                psPoints.setInt(2, customerId);
                psPoints.setInt(3, rewardPointsDelta);

                int updated = psPoints.executeUpdate();
                if (updated == 0) {
                    throw new SQLException("Không thể cập nhật điểm khách hàng (có thể không đủ điểm).");
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            rollbackQuietly(conn);
            e.printStackTrace();
            return false;

        } finally {
            closeQuietly(conn);
        }
    }

    private int insertOrderAndDetails(
            Connection conn,
            Order order,
            List<OrderDetail> details,
            String insertOrderSQL,
            String insertDetailSQL,
            String insertToppingSQL
    ) throws SQLException {
        int generatedOrderId = -1;

        try (PreparedStatement psOrder = conn.prepareStatement(insertOrderSQL, Statement.RETURN_GENERATED_KEYS)) {
            psOrder.setInt(1, order.getUserId());

            if (order.getCustomerId() != null && order.getCustomerId() > 0) {
                psOrder.setInt(2, order.getCustomerId());
            } else {
                psOrder.setNull(2, Types.INTEGER);
            }

            psOrder.setBigDecimal(3, order.getTotalAmount());
            psOrder.setString(4, order.getStatus());
            psOrder.setString(5, order.getPaymentMethod());
            psOrder.executeUpdate();

            try (ResultSet rs = psOrder.getGeneratedKeys()) {
                if (rs.next()) {
                    generatedOrderId = rs.getInt(1);
                } else {
                    throw new SQLException("Không thể lấy ID của Order vừa tạo.");
                }
            }
        }

        try (PreparedStatement psDetail = conn.prepareStatement(insertDetailSQL, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement psTopping = conn.prepareStatement(insertToppingSQL)) {

            for (OrderDetail detail : details) {
                psDetail.setInt(1, generatedOrderId);
                psDetail.setInt(2, detail.getProductId());
                psDetail.setInt(3, detail.getQuantity());
                psDetail.setBigDecimal(4, detail.getUnitPrice());
                psDetail.executeUpdate();

                int generatedDetailId = -1;
                try (ResultSet rsDetail = psDetail.getGeneratedKeys()) {
                    if (rsDetail.next()) {
                        generatedDetailId = rsDetail.getInt(1);
                    }
                }

                if (detail.getToppingQuantities() != null && !detail.getToppingQuantities().isEmpty()) {
                    for (Map.Entry<Integer, Integer> entry : detail.getToppingQuantities().entrySet()) {
                        int toppingId = entry.getKey();
                        int toppingQty = entry.getValue();

                        psTopping.setInt(1, generatedDetailId);
                        psTopping.setInt(2, toppingId);
                        psTopping.setInt(3, toppingId);
                        psTopping.setInt(4, toppingQty);
                        psTopping.addBatch();
                    }
                    psTopping.executeBatch();
                }
            }
        }

        return generatedOrderId;
    }

    private void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
                System.err.println("Transaction bị lỗi, đã Rollback an toàn!");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void closeQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    //////////////// CÁC HÀM DÙNG CHO THỐNG KÊ ////////////////////
    /**
     * Lấy danh sách các món trong một hóa đơn cụ thể (Order Details).
     * Đã bao gồm việc lấy tên Topping và cộng dồn giá Topping
     */
    public List<OrderDetailDTO> getOrderDetailsByOrderId(int orderId) {
        List<OrderDetailDTO> details = new ArrayList<>();

        //// phần topping trả về chuỗi tên, số lượng, tổng tiền topping đó
        //// vd: ["Trân châu đen (x3) +15000"]
        String query = "SELECT od.detail_id, od.product_id, p.name AS product_name, od.quantity, od.unit_price, " +
                "GROUP_CONCAT(CONCAT(t.name, ' (x', odt.quantity, ') +', (odt.unit_price * odt.quantity)) SEPARATOR ', ') AS topping_names, " +
                "SUM(odt.unit_price * odt.quantity) AS total_topping_price " +
                "FROM order_detail od " +
                "JOIN product p ON od.product_id = p.product_id " +
                "LEFT JOIN order_detail_topping odt ON od.detail_id = odt.detail_id " +
                "LEFT JOIN topping t ON odt.topping_id = t.topping_id " +
                "WHERE od.order_id = ? " +
                "GROUP BY od.detail_id, od.product_id, p.name, od.quantity, od.unit_price";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderDetailDTO dto = new OrderDetailDTO();

                    dto.setProductId(rs.getInt("product_id"));
                    dto.setProductName(rs.getString("product_name"));
                    dto.setQuantity(rs.getInt("quantity"));
                    dto.setUnitPrice(rs.getBigDecimal("unit_price"));

                    String toppingsString = rs.getString("topping_names");
                    // TÁCH CHUỖI THÀNH LIST
                    if (toppingsString != null && !toppingsString.trim().isEmpty()) {
                        // Dùng hàm split() để cắt chuỗi tại dấu phẩy và khoảng trắng, sau đó chuyển thành List
                        dto.setToppingList(new ArrayList<>(Arrays.asList(toppingsString.split(",\\s*"))));
                    } else {
                        // Nếu ly nước không có topping, trả về List rỗng để không bị lỗi Null
                        dto.setToppingList(new ArrayList<>());
                    }

                    BigDecimal tPrice = rs.getBigDecimal("total_topping_price");
                    dto.setToppingPrice(tPrice != null ? tPrice : BigDecimal.ZERO);

                    details.add(dto);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy chi tiết hóa đơn (DTO): " + e.getMessage());
            e.printStackTrace();
        }
        return details;
    }

    /**
     * Truy xuất lịch sử đơn hàng để tra cứu theo khoảng thời gian.
     * * Trả về danh sách chứa tên nhân viên lập bill và tên khách hàng (nếu có)
     */
    public List<OrderDTO> getOrderHistory(Date startDate, Date endDate){
        List<OrderDTO> orderList = new ArrayList<>();
        String query = "SELECT o.*, u.full_name AS staff_name, c.full_name AS customer_name " +
                "FROM `order` o " +
                "JOIN `user` u ON o.user_id = u.user_id " +
                "LEFT JOIN customer c ON o.customer_id = c.customer_id " +
                "WHERE DATE(o.created_at) BETWEEN ? AND ? " +
                "ORDER BY o.created_at DESC";

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setDate(1, new java.sql.Date(startDate.getTime()));
            ps.setDate(2, new java.sql.Date(endDate.getTime()));

            try(ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderDTO order = new OrderDTO();

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

                    order.setStaffName(rs.getString("staff_name"));
                    // Tên khách hàng có thể bị null nếu là khách vãng lai
                    order.setCustomerName(rs.getString("customer_name"));

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

    public int countPaidOrdersToday() {
        String sql = "SELECT COUNT(*) AS cnt FROM `order` WHERE status = 'PAID' AND DATE(created_at) = CURDATE()";
        return querySingleInt(sql);
    }

    public int countDistinctCustomersToday() {
        String sql = "SELECT COUNT(DISTINCT customer_id) AS cnt FROM `order` " +
                "WHERE status = 'PAID' AND DATE(created_at) = CURDATE() AND customer_id IS NOT NULL";
        return querySingleInt(sql);
    }

    public List<Object[]> getTopSellingProductsToday(int limit) {
        List<Object[]> results = new ArrayList<>();
        String sql = "SELECT p.name, SUM(od.quantity) AS sold_qty, SUM(od.quantity * od.unit_price) AS revenue " +
                "FROM order_detail od " +
                "JOIN product p ON od.product_id = p.product_id " +
                "JOIN `order` o ON od.order_id = o.order_id " +
                "WHERE o.status = 'PAID' AND DATE(o.created_at) = CURDATE() " +
                "GROUP BY p.product_id, p.name " +
                "ORDER BY sold_qty DESC " +
                "LIMIT ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (conn == null) {
                return results;
            }

            ps.setInt(1, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(new Object[]{
                            rs.getString("name"),
                            rs.getInt("sold_qty"),
                            rs.getBigDecimal("revenue")
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy sản phẩm bán chạy: " + e.getMessage());
            e.printStackTrace();
        }
        return results;
    }

    private int querySingleInt(String sql) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (conn != null && rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn thống kê: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
}