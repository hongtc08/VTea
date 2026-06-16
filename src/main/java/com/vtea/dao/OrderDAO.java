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
     * Xử lý lưu hóa đơn và chi tiết hóa đơn trong cùng một transaction.
     * Nhận Connection từ Service để Service quản lý commit/rollback.
     *
     * @return order_id vừa được database tạo ra, dùng để mở bill preview sau thanh toán.
     */
    public int checkoutOrder(Connection conn, Order order, List<OrderDetail> details) throws SQLException {
    String insertOrderSQL = "INSERT INTO `order` " +
            "(user_id, customer_id, total_amount, tier_discount_amount, point_discount_amount, created_at, status, payment_method) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
      String insertDetailSQL = "INSERT INTO order_detail (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
        String insertToppingSQL = "INSERT INTO order_detail_topping (detail_id, topping_id, unit_price, quantity) VALUES (?, ?, (SELECT price FROM topping WHERE topping_id = ?), ?)";

        int generatedOrderId = -1;

        // 1. Lưu thông tin chính của hóa đơn vào bảng order.
        try (PreparedStatement psOrder = conn.prepareStatement(insertOrderSQL, Statement.RETURN_GENERATED_KEYS)) {
            psOrder.setInt(1, order.getUserId());

            if (order.getCustomerId() != null && order.getCustomerId() > 0) {
                psOrder.setInt(2, order.getCustomerId());
            } else {
                psOrder.setNull(2, java.sql.Types.INTEGER);
            }

            psOrder.setBigDecimal(3, order.getTotalAmount());

            psOrder.setBigDecimal(4, order.getTierDiscountAmount() != null ? order.getTierDiscountAmount() : java.math.BigDecimal.ZERO);
            psOrder.setBigDecimal(5, order.getPointDiscountAmount() != null ? order.getPointDiscountAmount() : java.math.BigDecimal.ZERO);

            psOrder.setObject(6, java.time.LocalDateTime.now());
            psOrder.setString(7, order.getStatus());
            psOrder.setString(8, order.getPaymentMethod());

            psOrder.executeUpdate();

            // Lấy order_id vừa được database tự tăng.
            try (ResultSet rs = psOrder.getGeneratedKeys()) {
                if (rs.next()) {
                    generatedOrderId = rs.getInt(1);
                } else {
                    throw new SQLException("Không thể lấy ID của Order vừa tạo.");
                }
            }
        }

        // 2. Lưu từng món trong hóa đơn vào bảng order_detail.
        // Sau mỗi order_detail, lấy detail_id để lưu topping tương ứng.
        try (
                PreparedStatement psDetail = conn.prepareStatement(insertDetailSQL, Statement.RETURN_GENERATED_KEYS);
                PreparedStatement psTopping = conn.prepareStatement(insertToppingSQL)
        ) {
            for (OrderDetail detail : details) {
                psDetail.setInt(1, generatedOrderId);
                psDetail.setInt(2, detail.getProductId());
                psDetail.setInt(3, detail.getQuantity());
                psDetail.setBigDecimal(4, detail.getUnitPrice());
                psDetail.executeUpdate();

                int generatedDetailId = -1;

                // Lấy detail_id vừa tạo để gắn topping cho đúng dòng món.
                try (ResultSet rsDetail = psDetail.getGeneratedKeys()) {
                    if (rsDetail.next()) {
                        generatedDetailId = rsDetail.getInt(1);
                    } else {
                        throw new SQLException("Không thể lấy ID của Order Detail vừa tạo.");
                    }
                }

                // 3. Lưu topping của dòng món nếu có.
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

}