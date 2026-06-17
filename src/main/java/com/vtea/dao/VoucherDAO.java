package com.vtea.dao;

import com.vtea.model.Voucher;
import com.vtea.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VoucherDAO {

    /**
     * Tìm voucher theo mã (Chỉ bốc lên, không lọc điều kiện để Service tự bắt lỗi chi tiết)
     */
    public Voucher getVoucherByCode(String code) {
        String sql = "SELECT * FROM voucher WHERE code = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, code);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToVoucher(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm voucher theo mã: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lấy toàn bộ danh sách Voucher
     */
    public List<Voucher> getAllVouchers() {
        List<Voucher> list = new ArrayList<>();
        String sql = "SELECT * FROM voucher ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToVoucher(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách voucher: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Thêm mới một Voucher (Admin tạo mã mới)
     */
    public boolean insertVoucher(Voucher voucher) {
        String sql = "INSERT INTO voucher (code, discount_type, discount_value, min_order_value, " +
                "max_discount_amount, start_date, end_date, usage_limit, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, voucher.getCode());
            ps.setString(2, voucher.getDiscountType());
            ps.setBigDecimal(3, voucher.getDiscountValue());

            ps.setBigDecimal(4, voucher.getMinOrderValue() != null ? voucher.getMinOrderValue() : java.math.BigDecimal.ZERO);
            ps.setObject(5, voucher.getMaxDiscountAmount());

            ps.setTimestamp(6, voucher.getStartDate() != null ? Timestamp.valueOf(voucher.getStartDate()) : null);
            ps.setTimestamp(7, voucher.getEndDate() != null ? Timestamp.valueOf(voucher.getEndDate()) : null);

            ps.setInt(8, voucher.getUsageLimit());
            ps.setBoolean(9, voucher.isActive());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm mới voucher: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cập nhật thông tin Voucher (CHỈ CẬP NHẬT CÁC TRƯỜNG AN TOÀN)
     * Tuyệt đối không cho sửa: code, discount_type, discount_value
     */
    public boolean updateVoucher(Voucher voucher) {
        String sql = "UPDATE voucher SET min_order_value = ?, max_discount_amount = ?, " +
                "start_date = ?, end_date = ?, usage_limit = ?, is_active = ? " +
                "WHERE voucher_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBigDecimal(1, voucher.getMinOrderValue() != null ? voucher.getMinOrderValue() : java.math.BigDecimal.ZERO);
            ps.setObject(2, voucher.getMaxDiscountAmount());
            ps.setTimestamp(3, voucher.getStartDate() != null ? Timestamp.valueOf(voucher.getStartDate()) : null);
            ps.setTimestamp(4, voucher.getEndDate() != null ? Timestamp.valueOf(voucher.getEndDate()) : null);
            ps.setInt(5, voucher.getUsageLimit());
            ps.setBoolean(6, voucher.isActive());
            ps.setInt(7, voucher.getVoucherId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật voucher: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xóa mềm voucher (Admin vô hiệu hóa mã khẩn cấp)
     */
    public boolean deactivateVoucher(int voucherId) {
        String sql = "UPDATE voucher SET is_active = 0 WHERE voucher_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, voucherId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi vô hiệu hóa voucher: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Tăng số lượng đã sử dụng (Dùng trong Transaction chung với lưu Hóa đơn)
     */
    public void increaseUsedCount(int voucherId, Connection conn) throws SQLException {
        String sql = "UPDATE voucher SET used_count = used_count + 1 WHERE voucher_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, voucherId);
            pstmt.executeUpdate();
        }
    }

    // ==================== HELPER METHOD ====================

    /**
     * Hàm dùng chung để map ResultSet ra đối tượng Voucher chuẩn LocalDateTime
     */
    private Voucher mapResultSetToVoucher(ResultSet rs) throws SQLException {
        Voucher voucher = new Voucher();
        voucher.setVoucherId(rs.getInt("voucher_id"));
        voucher.setCode(rs.getString("code"));
        voucher.setDiscountType(rs.getString("discount_type"));
        voucher.setDiscountValue(rs.getBigDecimal("discount_value"));
        voucher.setMinOrderValue(rs.getBigDecimal("min_order_value"));
        voucher.setMaxDiscountAmount(rs.getBigDecimal("max_discount_amount"));
        Timestamp startTs = rs.getTimestamp("start_date");
        if (startTs != null) voucher.setStartDate(startTs.toLocalDateTime());

        Timestamp endTs = rs.getTimestamp("end_date");
        if (endTs != null) voucher.setEndDate(endTs.toLocalDateTime());

        Timestamp createdTs = rs.getTimestamp("created_at");
        if (createdTs != null) voucher.setCreatedAt(createdTs.toLocalDateTime());

        voucher.setUsageLimit(rs.getInt("usage_limit"));
        voucher.setUsedCount(rs.getInt("used_count"));
        voucher.setActive(rs.getBoolean("is_active"));

        return voucher;
    }
}