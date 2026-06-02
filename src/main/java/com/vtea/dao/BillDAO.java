package com.vtea.dao;

import com.vtea.dto.BillDTO;
import com.vtea.dto.BillItemDTO;
import com.vtea.dto.BillToppingDTO;
import com.vtea.dto.OrderHistoryDTO;
import com.vtea.utils.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO xử lý truy vấn dữ liệu hóa đơn.
 * File này chỉ phụ trách lấy dữ liệu từ database, không xử lý UI.
 */
public class BillDAO {

    /**
     * Lấy toàn bộ thông tin của một hóa đơn theo order_id.
     * Dữ liệu này dùng chung cho bill preview, xem chi tiết hóa đơn và xuất PDF.
     */
    public BillDTO getBillByOrderId(int orderId) {
        BillDTO bill = getBillHeader(orderId);

        if (bill == null) {
            return null;
        }

        List<BillItemDTO> items = getBillItems(orderId);

        for (BillItemDTO item : items) {
            List<BillToppingDTO> toppings = getToppingsByDetailId(item.getDetailId());
            item.setToppings(toppings);
            bill.addItem(item);
        }

        return bill;
    }

    /**
     * Lấy thông tin chung của hóa đơn:
     * mã hóa đơn, thời gian, nhân viên, khách hàng, phương thức thanh toán, tổng tiền.
     */
    private BillDTO getBillHeader(int orderId) {
        String sql = """
                SELECT 
                    o.order_id,
                    o.created_at,
                    o.user_id,
                    u.full_name AS staff_name,
                    o.customer_id,
                    c.full_name AS customer_name,
                    c.phone_number AS customer_phone,
                    o.payment_method,
                    o.status,
                    o.total_amount
                FROM `order` o
                LEFT JOIN user u ON o.user_id = u.user_id
                LEFT JOIN customer c ON o.customer_id = c.customer_id
                WHERE o.order_id = ?
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BillDTO bill = new BillDTO();

                    bill.setOrderId(rs.getInt("order_id"));

                    if (rs.getTimestamp("created_at") != null) {
                        bill.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    }

                    int userId = rs.getInt("user_id");
                    if (!rs.wasNull()) {
                        bill.setUserId(userId);
                    }

                    bill.setStaffName(rs.getString("staff_name"));

                    int customerId = rs.getInt("customer_id");
                    if (!rs.wasNull()) {
                        bill.setCustomerId(customerId);
                    }

                    bill.setCustomerName(rs.getString("customer_name"));
                    bill.setCustomerPhone(rs.getString("customer_phone"));
                    bill.setPaymentMethod(rs.getString("payment_method"));
                    bill.setStatus(rs.getString("status"));
                    bill.setTotalAmount(rs.getBigDecimal("total_amount"));

                    return bill;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy thông tin hóa đơn theo order_id: " + orderId, e);
        }

        return null;
    }

    /**
     * Lấy danh sách món trong hóa đơn.
     * Mỗi dòng trong order_detail sẽ tương ứng với một BillItemDTO.
     */
    private List<BillItemDTO> getBillItems(int orderId) {
        List<BillItemDTO> items = new ArrayList<>();

        String sql = """
                SELECT 
                    od.detail_id,
                    od.product_id,
                    p.name AS product_name,
                    od.quantity,
                    od.unit_price
                FROM order_detail od
                JOIN product p ON od.product_id = p.product_id
                WHERE od.order_id = ?
                ORDER BY od.detail_id
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BillItemDTO item = new BillItemDTO(
                            rs.getInt("detail_id"),
                            rs.getInt("product_id"),
                            rs.getString("product_name"),
                            rs.getInt("quantity"),
                            rs.getBigDecimal("unit_price")
                    );

                    items.add(item);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy danh sách món của hóa đơn: " + orderId, e);
        }

        return items;
    }

    /**
     * Lấy danh sách topping của một dòng món.
     * Topping được lưu theo detail_id trong bảng order_detail_topping.
     */
    private List<BillToppingDTO> getToppingsByDetailId(int detailId) {
        List<BillToppingDTO> toppings = new ArrayList<>();

        String sql = """
                SELECT 
                    odt.topping_id,
                    t.name AS topping_name,
                    odt.quantity,
                    odt.unit_price
                FROM order_detail_topping odt
                JOIN topping t ON odt.topping_id = t.topping_id
                WHERE odt.detail_id = ?
                ORDER BY t.name
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, detailId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BillToppingDTO topping = new BillToppingDTO(
                            rs.getInt("topping_id"),
                            rs.getString("topping_name"),
                            rs.getInt("quantity"),
                            rs.getBigDecimal("unit_price")
                    );

                    toppings.add(topping);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy topping của detail_id: " + detailId, e);
        }

        return toppings;
    }

    /**
     * Lấy danh sách hóa đơn để hiển thị ở màn hình lịch sử hóa đơn.
     * Hàm này chưa lọc nâng cao, dùng để load danh sách ban đầu.
     */
    public List<OrderHistoryDTO> getOrderHistory() {
        List<OrderHistoryDTO> histories = new ArrayList<>();

        String sql = """
                SELECT 
                    o.order_id,
                    o.created_at,
                    u.full_name AS staff_name,
                    c.full_name AS customer_name,
                    c.phone_number AS customer_phone,
                    o.total_amount,
                    o.payment_method,
                    o.status
                FROM `order` o
                LEFT JOIN user u ON o.user_id = u.user_id
                LEFT JOIN customer c ON o.customer_id = c.customer_id
                ORDER BY o.created_at DESC
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                OrderHistoryDTO history = mapOrderHistory(rs);
                histories.add(history);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy lịch sử hóa đơn", e);
        }

        return histories;
    }

    /**
     * Lấy lịch sử hóa đơn theo khoảng ngày.
     * Dùng cho chức năng lọc hóa đơn theo ngày/tháng.
     */
    public List<OrderHistoryDTO> getOrderHistoryByDateRange(LocalDate fromDate, LocalDate toDate) {
        List<OrderHistoryDTO> histories = new ArrayList<>();

        String sql = """
                SELECT 
                    o.order_id,
                    o.created_at,
                    u.full_name AS staff_name,
                    c.full_name AS customer_name,
                    c.phone_number AS customer_phone,
                    o.total_amount,
                    o.payment_method,
                    o.status
                FROM `order` o
                LEFT JOIN user u ON o.user_id = u.user_id
                LEFT JOIN customer c ON o.customer_id = c.customer_id
                WHERE DATE(o.created_at) BETWEEN ? AND ?
                ORDER BY o.created_at DESC
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setDate(1, Date.valueOf(fromDate));
            ps.setDate(2, Date.valueOf(toDate));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderHistoryDTO history = mapOrderHistory(rs);
                    histories.add(history);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lọc lịch sử hóa đơn theo ngày", e);
        }

        return histories;
    }

    /**
     * Tìm kiếm hóa đơn theo mã hóa đơn, tên khách hàng, số điện thoại hoặc tên nhân viên.
     */
    public List<OrderHistoryDTO> searchOrderHistory(String keyword) {
        List<OrderHistoryDTO> histories = new ArrayList<>();

        String sql = """
                SELECT 
                    o.order_id,
                    o.created_at,
                    u.full_name AS staff_name,
                    c.full_name AS customer_name,
                    c.phone_number AS customer_phone,
                    o.total_amount,
                    o.payment_method,
                    o.status
                FROM `order` o
                LEFT JOIN user u ON o.user_id = u.user_id
                LEFT JOIN customer c ON o.customer_id = c.customer_id
                WHERE 
                    CAST(o.order_id AS CHAR) LIKE ?
                    OR c.full_name LIKE ?
                    OR c.phone_number LIKE ?
                    OR u.full_name LIKE ?
                ORDER BY o.created_at DESC
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            String searchValue = "%" + keyword + "%";

            ps.setString(1, searchValue);
            ps.setString(2, searchValue);
            ps.setString(3, searchValue);
            ps.setString(4, searchValue);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderHistoryDTO history = mapOrderHistory(rs);
                    histories.add(history);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi tìm kiếm lịch sử hóa đơn với từ khóa: " + keyword, e);
        }

        return histories;
    }

    /**
     * Chuyển dữ liệu ResultSet thành OrderHistoryDTO.
     * Tách riêng method này để tránh lặp code ở các hàm lấy lịch sử hóa đơn.
     */
    private OrderHistoryDTO mapOrderHistory(ResultSet rs) throws SQLException {
        return new OrderHistoryDTO(
                rs.getInt("order_id"),
                rs.getTimestamp("created_at") == null
                        ? null
                        : rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getString("staff_name"),
                rs.getString("customer_name"),
                rs.getString("customer_phone"),
                rs.getBigDecimal("total_amount"),
                rs.getString("payment_method"),
                rs.getString("status")
        );
    }
}