package com.vtea.dao;

import com.vtea.dto.CustomerDTO;
import com.vtea.dto.CustomerStatsDTO;
import com.vtea.model.Customer;
import com.vtea.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    /**
     * Tìm khách hàng theo Số điện thoại.
     */
    public CustomerDTO getCustomerByPhone(String phone) {
        CustomerDTO customer = null;
        String query = "SELECT c.*, t.tier_name, t.discount_percent FROM customer c " +
                "JOIN member_tier t ON c.tier_id = t.tier_id " +
                "WHERE c.phone_number = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, phone);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    customer = new CustomerDTO();
                    customer.setCustomerId(rs.getInt("customer_id"));
                    customer.setFullName(rs.getString("full_name"));
                    customer.setPhoneNumber(rs.getString("phone_number"));
                    customer.setRewardPoints(rs.getInt("reward_points"));
                    customer.setTotalAccumulatedPoints(rs.getInt("total_accumulated_points"));
                    customer.setTierId(rs.getInt("tier_id"));
                    customer.setTierName(rs.getString("tier_name"));
                    customer.setDiscountPercent(rs.getInt("discount_percent"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm khách hàng theo SDT: " + e.getMessage());
            e.printStackTrace();
        }

        return customer;
    }

    /**
     * Tìm kiếm khách hàng theo ID.
     * Sử dụng ngầm ở tầng Service để đối chiếu và xử lý logic tính điểm, trừ tiền.
     */
    public CustomerDTO getCustomerById(int customerId) {
        CustomerDTO customer = null;
        String query = "SELECT c.*, t.tier_name, t.discount_percent FROM customer c " +
                "JOIN member_tier t ON c.tier_id = t.tier_id " +
                "WHERE c.customer_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    customer = new CustomerDTO();
                    customer.setCustomerId(rs.getInt("customer_id"));
                    customer.setFullName(rs.getString("full_name"));
                    customer.setPhoneNumber(rs.getString("phone_number"));
                    customer.setRewardPoints(rs.getInt("reward_points"));
                    customer.setTotalAccumulatedPoints(rs.getInt("total_accumulated_points"));
                    customer.setTierId(rs.getInt("tier_id"));
                    customer.setTierName(rs.getString("tier_name"));
                    customer.setDiscountPercent(rs.getInt("discount_percent"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm khách hàng theo ID: " + e.getMessage());
            e.printStackTrace();
        }

        return customer;
    }

    /**
     * Thêm khách hàng mới.
     */
    public boolean insertCustomer(Customer customer) {
        // Cập nhật: Set cứng mặc định Hạng 1 (Đồng) và Điểm = 0
        String sql = "INSERT INTO customer (full_name, phone_number, reward_points, total_accumulated_points, tier_id) VALUES (?, ?, 0, 0, 1)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, customer.getFullName());
            ps.setString(2, customer.getPhoneNumber());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm khách hàng mới: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }


    // =========================================================================
    // Các phương thức liên quan đến tính năng tích điểm, trừ điểm và reset hạng
    // =========================================================================

    /**
     * 1. Trừ điểm (Khi khách dùng điểm lấy tiền giảm giá)
     * CHỈ trừ điểm dùng để đổi thưởng (reward_points), giữ nguyên thanh EXP (total_accumulated)
     */
    public boolean deductRewardPoints(Connection conn, int customerId, int pointsUsed) throws SQLException {
        String sql = "UPDATE customer SET reward_points = reward_points - ? WHERE customer_id = ? AND reward_points >= ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pointsUsed);
            ps.setInt(2, customerId);
            ps.setInt(3, pointsUsed); // Đảm bảo không trừ âm điểm

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * 2. Tích điểm & Lên hạng (Khi khách mua ly nước mới)
     * Vừa cộng tiền ví, vừa cộng thanh EXP, kết hợp subquery tự update Hạng.
     */
    public boolean addPointsAndUpgradeTier(Connection conn, int customerId, int pointsEarned) throws SQLException {
        String sql = "UPDATE customer " +
                "SET reward_points = reward_points + ?, " +
                "    total_accumulated_points = total_accumulated_points + ?, " +
                "    tier_id = (SELECT tier_id FROM member_tier " +
                "               WHERE required_points <= total_accumulated_points " +
                "               ORDER BY required_points DESC LIMIT 1) " +
                "WHERE customer_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pointsEarned); // Cộng điểm thưởng
            ps.setInt(2, pointsEarned); // Cộng điểm xét hạng
            ps.setInt(3, customerId);

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * 3. Tính năng Reset hạng cuối mùa giải (Dành cho Admin)
     */
    public boolean resetAllCustomerTiers() {
        String sql = "UPDATE customer SET total_accumulated_points = 0, tier_id = 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi reset hạng toàn hệ thống: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public List<CustomerDTO> getAllCustomers() {
        List<CustomerDTO> list = new ArrayList<>();
        String query = "SELECT c.*, t.tier_name, t.discount_percent, " +
                "(SELECT MAX(created_at) FROM `order` o WHERE o.customer_id = c.customer_id AND o.status = 'PAID') as last_purchase " +
                "FROM customer c " +
                "JOIN member_tier t ON c.tier_id = t.tier_id " +
                "ORDER BY last_purchase DESC, c.total_accumulated_points DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                CustomerDTO customer = new CustomerDTO();
                customer.setCustomerId(rs.getInt("customer_id"));
                customer.setFullName(rs.getString("full_name"));
                customer.setPhoneNumber(rs.getString("phone_number"));
                customer.setRewardPoints(rs.getInt("reward_points"));
                customer.setTotalAccumulatedPoints(rs.getInt("total_accumulated_points"));
                customer.setTierId(rs.getInt("tier_id"));
                customer.setTierName(rs.getString("tier_name"));
                customer.setDiscountPercent(rs.getInt("discount_percent"));
                
                Timestamp lastPurchaseTs = rs.getTimestamp("last_purchase");
                if (lastPurchaseTs != null) {
                    customer.setLastPurchase(lastPurchaseTs.toLocalDateTime());
                }

                list.add(customer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("SQL ERROR IN GET ALL CUSTOMERS: " + e.getMessage());
        }
        return list;
    }

    public com.vtea.dto.CustomerStatsDTO getCustomerStatistics() {
        String query = "SELECT " +
                "COUNT(*) as total_customers, " +
                "SUM(CASE WHEN t.tier_id = 4 THEN 1 ELSE 0 END) as diamond_count, " +
                "SUM(CASE WHEN t.tier_id = 3 THEN 1 ELSE 0 END) as gold_count, " +
                "AVG(c.total_accumulated_points) as avg_points " +
                "FROM customer c " +
                "JOIN member_tier t ON c.tier_id = t.tier_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return new CustomerStatsDTO(
                        rs.getInt("total_customers"),
                        rs.getInt("diamond_count"),
                        rs.getInt("gold_count"),
                        rs.getInt("avg_points")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new CustomerStatsDTO(0, 0, 0, 0);
    }

    public boolean updateCustomer(CustomerDTO customer) {
        String sql = "UPDATE customer SET full_name = ?, phone_number = ?, " +
                     "reward_points = ?, total_accumulated_points = ?, " +
                     "tier_id = (SELECT tier_id FROM member_tier WHERE required_points <= ? ORDER BY required_points DESC LIMIT 1) " +
                     "WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customer.getFullName());
            ps.setString(2, customer.getPhoneNumber());
            ps.setInt(3, customer.getRewardPoints());
            ps.setInt(4, customer.getTotalAccumulatedPoints());
            ps.setInt(5, customer.getTotalAccumulatedPoints());
            ps.setInt(6, customer.getCustomerId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteCustomer(int customerId) {
        String sql = "DELETE FROM customer WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}