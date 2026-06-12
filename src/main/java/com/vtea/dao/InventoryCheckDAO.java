package com.vtea.dao;

import com.vtea.dto.InventoryCheckDTO;
import com.vtea.model.InventoryCheck;
import com.vtea.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryCheckDAO {

    /**
     * Thêm mới một phiếu kiểm kho ở trạng thái PENDING (Dành cho STAFF chốt kho)
     */
    public boolean insertPendingCheck(InventoryCheck check) {
        String sql = "INSERT INTO `inventory_check_log` (ingredient_id, staff_id, system_qty, actual_qty, difference, status) " +
                "VALUES (?, ?, ?, ?, ?, 'PENDING')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, check.getIngredientId());
            ps.setInt(2, check.getStaffId());
            ps.setBigDecimal(3, check.getSystemQty());
            ps.setBigDecimal(4, check.getActualQty());
            ps.setBigDecimal(5, check.getDifference());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm phiếu kiểm kho nháp: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Lấy danh sách các phiếu kiểm kho đang chờ duyệt (PENDING) kèm tên
     */
    public List<InventoryCheckDTO> getPendingChecks() {
        List<InventoryCheckDTO> list = new ArrayList<>();
        String sql = "SELECT c.*, i.name AS ingredient_name, u.full_name AS staff_name " +
                "FROM `inventory_check_log` c " +
                "JOIN `ingredient` i ON c.ingredient_id = i.ingredient_id " +
                "JOIN `user` u ON c.staff_id = u.user_id " +
                "WHERE c.status = 'PENDING' " +
                "ORDER BY c.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                InventoryCheckDTO dto = new InventoryCheckDTO();

                // Map dữ liệu gốc
                dto.setLogId(rs.getInt("log_id"));
                dto.setIngredientId(rs.getInt("ingredient_id"));
                dto.setStaffId(rs.getInt("staff_id"));
                dto.setSystemQty(rs.getBigDecimal("system_qty"));
                dto.setActualQty(rs.getBigDecimal("actual_qty"));
                dto.setDifference(rs.getBigDecimal("difference"));
                dto.setStatus(rs.getString("status"));

                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) {
                    dto.setCreatedAt(ts.toLocalDateTime());
                }

                int adminId = rs.getInt("approved_by");
                dto.setApprovedBy(rs.wasNull() ? null : adminId);

                // Map dữ liệu mở rộng (Từ các bảng JOIN)
                dto.setIngredientName(rs.getString("ingredient_name"));
                dto.setStaffName(rs.getString("staff_name"));

                list.add(dto);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách phiếu chờ duyệt: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Cập nhật trạng thái phiếu kiểm kho (APPROVED hoặc REJECTED)
     * Nhận Connection từ ngoài để chạy Transaction chung với tầng Service.
     */
    public boolean updateCheckStatus(int logId, String status, int adminId, Connection conn) throws SQLException {
        String sql = "UPDATE `inventory_check_log` SET status = ?, approved_by = ? WHERE log_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, adminId);
            ps.setInt(3, logId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Hàm hỗ trợ tìm phiếu theo ID (Trả về Model thuần túy để xử lý logic)
     */
    public InventoryCheck getCheckById(int logId) {
        String sql = "SELECT * FROM `inventory_check_log` WHERE log_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, logId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    InventoryCheck check = new InventoryCheck();
                    check.setLogId(rs.getInt("log_id"));
                    check.setIngredientId(rs.getInt("ingredient_id"));
                    check.setStaffId(rs.getInt("staff_id"));
                    check.setSystemQty(rs.getBigDecimal("system_qty"));
                    check.setActualQty(rs.getBigDecimal("actual_qty"));
                    check.setDifference(rs.getBigDecimal("difference"));
                    check.setStatus(rs.getString("status"));

                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) {
                        check.setCreatedAt(ts.toLocalDateTime());
                    }

                    int adminId = rs.getInt("approved_by");
                    check.setApprovedBy(rs.wasNull() ? null : adminId);
                    return check;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm phiếu kiểm kho theo ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}