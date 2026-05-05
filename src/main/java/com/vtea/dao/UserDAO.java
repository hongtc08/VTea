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
     * Chỉ tìm User theo username, BE tự dùng thư viện mã hóa để kiểm tra password.
     */
    public User getUserByUsername(String username){
        User user = null;

        String query = "SELECT * FROM `user` WHERE username=?";

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, username);

            try(ResultSet rs = ps.executeQuery()) {
                if(rs.next()){
                    user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUserName(rs.getString("username"));
                    user.setPassWord(rs.getString("password"));
                    user.setFullName(rs.getString("full_name"));
                    user.setRole(rs.getString("role"));
                    user.setStatus(rs.getString("status"));
                    user.setCreatedAt(rs.getTimestamp("created_at"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm User theo username: " + e.getMessage());
            e.printStackTrace();
        }
        return user;
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
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUserName(rs.getString("username"));
                user.setPassWord(rs.getString("password"));
                user.setFullName(rs.getString("full_name"));
                user.setRole(rs.getString("role"));
                user.setStatus(rs.getString("status"));
                user.setCreatedAt(rs.getTimestamp("created_at"));

                userList.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách User: " + e.getMessage());
            e.printStackTrace();
        }
        return userList;
    }

    /**
     * Thêm nhân viên mới
     * Lưu ý: Mật khẩu truyền vào qua object User phải là mật khẩu đã được BE mã hóa.
     */
    public boolean insertUser(User user){
        String sql = "INSERT INTO `user` (username, password, full_name, role, status, created_at) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUserName());
            ps.setString(2, user.getPassWord());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getRole());
            ps.setString(5, "Active");

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm User mới: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Dùng cho chức năng Khóa/Mở khóa tài khoản nhân viên của Admin.
     * Cập nhật cột status thành 'Active' hoặc 'Locked'.
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
     * Cập nhật thông tin nhân viên (Chỉ cập nhật Tên và Quyền)
     * Đổi mật khẩu hoặc đổi status tách thành hàm riêng để dễ quản lý.
     */
    public boolean updateUser(User user) {
        String sql = "UPDATE `user` SET full_name = ?, role = ? WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getFullName());
            ps.setString(2, user.getRole());
            ps.setInt(3, user.getUserId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật User: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Cập nhật mật khẩu mới cho nhân viên.
     * Lưu ý cho BE: Chuỗi newPassword truyền vào hàm này BẮT BUỘC
     * phải là chuỗi đã được mã hóa, không truyền mật khẩu thô.
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
}