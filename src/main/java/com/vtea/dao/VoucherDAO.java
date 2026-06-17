package com.vtea.dao;

import com.vtea.model.Voucher;
import com.vtea.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VoucherDAO {
    // tìm voucher theo mã
    public Voucher getValidVoucherByCode(String code) {
        Voucher voucher = null;
        String sql = "SELECT * FROM voucher " +
                "WHERE code = ? " +
                "AND is_active = 1 " +
                "AND used_count < usage_limit " +
                "AND NOW() BETWEEN start_date AND end_date";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, code);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                voucher = new Voucher();
                voucher.setVoucherId(rs.getInt("voucher_id"));
                voucher.setCode(rs.getString("code"));
                voucher.setDiscountType(rs.getString("discount_type"));
                voucher.setDiscountValue(rs.getBigDecimal("discount_value"));
                voucher.setMinOrderValue(rs.getBigDecimal("min_order_value"));
                voucher.setMaxDiscountAmount(rs.getBigDecimal("max_discount_amount"));
                voucher.setUsageLimit(rs.getInt("usage_limit"));
                voucher.setUsedCount(rs.getInt("used_count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return voucher;
    }

    // Lấy toàn bộ danh sách Voucher
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
            e.printStackTrace();
        }
        return list;
    }

    // Thêm mới một Voucher (Admin tạo mã mới)
    public boolean insertVoucher(Voucher voucher) {
        String sql = "INSERT INTO voucher (code, discount_type, discount_value, min_order_value, max_discount_amount, start_date, end_date, usage_limit, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, voucher.getCode());
            ps.setString(2, voucher.getDiscountType());
            ps.setBigDecimal(3, voucher.getDiscountValue());
            ps.setBigDecimal(4, voucher.getMinOrderValue());
            ps.setBigDecimal(5, voucher.getMaxDiscountAmount());
            ps.setTimestamp(6, voucher.getStartDate());
            ps.setTimestamp(7, voucher.getEndDate());
            ps.setInt(8, voucher.getUsageLimit());
            ps.setBoolean(9, voucher.isActive());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Cập nhật thông tin Voucher
    public boolean updateVoucher(Voucher voucher) {
        String sql = "UPDATE voucher SET code = ?, discount_type = ?, discount_value = ?, min_order_value = ?, max_discount_amount = ?, start_date = ?, end_date = ?, usage_limit = ?, is_active = ? WHERE voucher_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, voucher.getCode());
            ps.setString(2, voucher.getDiscountType());
            ps.setBigDecimal(3, voucher.getDiscountValue());
            ps.setBigDecimal(4, voucher.getMinOrderValue());
            ps.setBigDecimal(5, voucher.getMaxDiscountAmount());
            ps.setTimestamp(6, voucher.getStartDate());
            ps.setTimestamp(7, voucher.getEndDate());
            ps.setInt(8, voucher.getUsageLimit());
            ps.setBoolean(9, voucher.isActive());
            ps.setInt(10, voucher.getVoucherId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // xoa voucher
    public boolean deactivateVoucher(int voucherId) {
        String sql = "UPDATE voucher SET is_active = 0 WHERE voucher_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, voucherId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // +1 sl đã sử dụng
    public void increaseUsedCount(int voucherId, Connection conn) throws SQLException {
        String sql = "UPDATE voucher SET used_count = used_count + 1 WHERE voucher_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, voucherId);
            pstmt.executeUpdate();
        }
    }


    private Voucher mapResultSetToVoucher(ResultSet rs) throws SQLException {
        Voucher voucher = new Voucher();
        voucher.setVoucherId(rs.getInt("voucher_id"));
        voucher.setCode(rs.getString("code"));
        voucher.setDiscountType(rs.getString("discount_type"));
        voucher.setDiscountValue(rs.getBigDecimal("discount_value"));
        voucher.setMinOrderValue(rs.getBigDecimal("min_order_value"));
        voucher.setMaxDiscountAmount(rs.getBigDecimal("max_discount_amount"));
        voucher.setStartDate(rs.getTimestamp("start_date"));
        voucher.setEndDate(rs.getTimestamp("end_date"));

        voucher.setUsageLimit(rs.getInt("usage_limit"));
        voucher.setUsedCount(rs.getInt("used_count"));
        voucher.setActive(rs.getBoolean("is_active"));
        return voucher;
    }
}
