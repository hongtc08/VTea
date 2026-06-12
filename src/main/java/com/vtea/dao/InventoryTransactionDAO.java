package com.vtea.dao;

import com.vtea.dto.InventoryTransactionDTO;
import com.vtea.model.InventoryTransaction;
import com.vtea.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryTransactionDAO {

    /**
     * Ghi sổ lịch sử thay đổi kho chính thức (Sổ Cái)
     * Nhận Connection từ ngoài để chạy Transaction chung với luồng Duyệt phiếu.
     */
    public boolean insertTransaction(InventoryTransaction tx, Connection conn) throws SQLException {
        String sql = "INSERT INTO `inventory_transaction` (ingredient_id, admin_id, change_type, quantity_changed, note) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tx.getIngredientId());
            ps.setInt(2, tx.getAdminId());
            ps.setString(3, tx.getChangeType());
            ps.setBigDecimal(4, tx.getQuantityChanged());
            ps.setString(5, tx.getNote());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Lấy toàn bộ lịch sử biến động kho để hiển thị lên Tab Lịch sử của Admin (Sử dụng DTO)
     */
    public List<InventoryTransactionDTO> getAllTransactions() {
        List<InventoryTransactionDTO> list = new ArrayList<>();
        String sql = "SELECT t.*, i.name AS ingredient_name, u.full_name AS admin_name " +
                "FROM `inventory_transaction` t " +
                "JOIN `ingredient` i ON t.ingredient_id = i.ingredient_id " +
                "JOIN `user` u ON t.admin_id = u.user_id " +
                "ORDER BY t.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                InventoryTransactionDTO dto = new InventoryTransactionDTO();

                // Map dữ liệu gốc
                dto.setTransactionId(rs.getInt("transaction_id"));
                dto.setIngredientId(rs.getInt("ingredient_id"));
                dto.setAdminId(rs.getInt("admin_id"));
                dto.setChangeType(rs.getString("change_type"));
                dto.setQuantityChanged(rs.getBigDecimal("quantity_changed"));
                dto.setNote(rs.getString("note"));

                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) {
                    dto.setCreatedAt(ts.toLocalDateTime());
                }

                // Map dữ liệu mở rộng (Từ bảng JOIN)
                dto.setIngredientName(rs.getString("ingredient_name"));
                dto.setAdminName(rs.getString("admin_name"));

                list.add(dto);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy lịch sử giao dịch kho: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }
}