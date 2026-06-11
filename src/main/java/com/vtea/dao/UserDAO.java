package com.vtea.dao;

import com.vtea.model.User;
import com.vtea.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    /**
     * Dùng cho chức năng Đăng nhập
     */
    public User getUserByUsername(String username){
        String query = "SELECT * FROM `user` WHERE username=?";

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, username);

            try(ResultSet rs = ps.executeQuery()) {
                if(rs.next()){
                    return mapRowToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm User theo username: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Tìm kiếm nhân viên theo Id
     */
    public User getUserById(int userId) {
        String query = "SELECT * FROM `user` WHERE user_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm User theo ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Tìm danh sách nhân viên theo họ và tên (Tìm kiếm gần đúng)
     */
    public List<User> getUsersByName(String name) {
        List<User> userList = new ArrayList<>();
        String query = "SELECT * FROM `user` WHERE full_name LIKE ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, "%" + name + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    userList.add(mapRowToUser(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm User theo tên: " + e.getMessage());
            e.printStackTrace();
        }
        return userList;
    }

    /**
     * Tìm nhân viên theo Email
     */
    public User getUserByEmail(String email) {
        String query = "SELECT * FROM `user` WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm User theo email: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Tìm nhân viên theo Số điện thoại
     */
    public User getUserByPhone(String phone) {
        String query = "SELECT * FROM `user` WHERE phone = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, phone);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm User theo SĐT: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lấy danh sách tất cả nhân viên (Dành cho Admin)
     */
    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        String query = "SELECT * FROM `user`";

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery()) {

            while(rs.next()) {
                userList.add(mapRowToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách User: " + e.getMessage());
            e.printStackTrace();
        }
        return userList;
    }

    /**
     * Thêm nhân viên mới
     */
    public boolean insertUser(User user){
        String sql = "INSERT INTO `user` (username, password, full_name, role, status, email, phone, salary, start_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUserName());
            ps.setString(2, user.getPassWord());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getRole());
            ps.setString(5, user.getStatus());
            ps.setString(6, user.getEmail());
            ps.setString(7, user.getPhone());
            ps.setBigDecimal(8, user.getSalary());

            if (user.getStartDate() != null) {
                ps.setDate(9, java.sql.Date.valueOf(user.getStartDate()));
            } else {
                ps.setNull(9, java.sql.Types.DATE);
            }

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm User mới: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Cập nhật thông tin nhân viên (Chỉ cập nhật thông tin cơ bản)
     */
    public boolean updateUser(User user) {
        String sql = "UPDATE `user` SET full_name = ?, role = ?, email = ?, phone = ?, salary = ?, start_date = ? WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getFullName());
            ps.setString(2, user.getRole());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPhone());
            ps.setBigDecimal(5, user.getSalary());

            if (user.getStartDate() != null) {
                ps.setDate(6, java.sql.Date.valueOf(user.getStartDate()));
            } else {
                ps.setNull(6, java.sql.Types.DATE);
            }

            ps.setInt(7, user.getUserId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật User: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Dùng cho chức năng Khóa/Mở khóa tài khoản nhân viên của Admin.
     */
    public boolean updateStatus(int userId, String newStatus){
        String sql = "UPDATE `user` SET status = ? WHERE user_id = ?";

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật trạng thái user: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Cập nhật mật khẩu mới cho nhân viên.
     */
    public boolean updatePassword(int userId, String newPassword) {
        String sql = "UPDATE `user` SET password = ? WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newPassword);
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật mật khẩu: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // ==================== HELPER METHOD ====================

    /**
     * Hàm hỗ trợ map dữ liệu từ ResultSet sang User Object.
     */
    private User mapRowToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUserName(rs.getString("username"));
        user.setPassWord(rs.getString("password"));
        user.setFullName(rs.getString("full_name"));
        user.setRole(rs.getString("role"));
        user.setStatus(rs.getString("status"));

        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setSalary(rs.getBigDecimal("salary"));

        java.sql.Date sDate = rs.getDate("start_date");
        if (sDate != null) {
            user.setStartDate(sDate.toLocalDate());
        }

        java.sql.Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            user.setCreatedAt(ts.toLocalDateTime());
        }

        return user;
    }
}