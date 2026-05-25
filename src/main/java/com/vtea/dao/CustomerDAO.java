package com.vtea.dao;

import com.vtea.model.Customer;
import com.vtea.utils.DBConnection;

import java.sql.*;

public class CustomerDAO {
    /**
     * Tìm khách hàng theo Số điện thoại.
     * tự động gọi hàm này khi thu ngân nhập đử 10 số. Nếu có, UI sẽ tự động
     * cập nhật tên và điểm tích lũy lên giao diện mà không cần nhập lại.
     */
    public Customer getCustomerByPhone(String phone) {
        Customer customer = null;
        String query = "SELECT * FROM customer WHERE phone_number = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, phone);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    customer = new Customer();
                    customer.setCustomerId(rs.getInt("customer_id"));
                    customer.setFullName(rs.getString("full_name"));
                    customer.setPhoneNumber(rs.getString("phone_number"));
                    customer.setRewardPoints(rs.getInt("reward_points"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm khách hàng theo SĐT: " + e.getMessage());
            e.printStackTrace();
        }

        return customer; // Trả về null nếu số điện thoại này chưa từng đăng ký
    }

    /**
     * Thêm khách hàng mới.
     * Mặc định khi mới tạo, điểm thưởng (reward_points) = 0.
     */
    public int insertCustomer(Customer customer) {
        String sql = "INSERT INTO customer (full_name, phone_number, reward_points) VALUES (?, ?, 0)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, customer.getFullName());
            ps.setString(2, customer.getPhoneNumber());

            if (ps.executeUpdate() > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm khách hàng mới: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Cập nhật điểm thưởng cho khách hàng.
     * - Khi cộng điểm (khách mua hàng): truyền pointsToAdd là số dương (vd: +5).
     * - Khi trừ điểm (khách đổi điểm lấy mã giảm giá): truyền pointsToAdd là số âm (vd: -10).
     */
    public boolean updateRewardPoints(int customerId, int pointsToAdd) {
        String sql = "UPDATE customer SET reward_points = reward_points + ? WHERE customer_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, pointsToAdd);
            ps.setInt(2, customerId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật điểm thưởng: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}
