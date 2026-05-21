package com.vtea.dao;

import com.vtea.model.Topping;
import com.vtea.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ToppingDAO {
    /**
     * Dành cho màn hình POS (Thu ngân):
     * Lấy danh sách các topping đang còn phục vụ (is_available = true)
     */
    public List<Topping> getAllActiveToppings() {
        List<Topping> toppingList = new ArrayList<>();
        String query = "SELECT * FROM topping WHERE is_available = true";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Topping topping = new Topping();
                topping.setToppingId(rs.getInt("topping_id"));
                topping.setName(rs.getString("name"));
                topping.setPrice(rs.getBigDecimal("price"));
                topping.setAvailable(rs.getBoolean("is_available"));

                toppingList.add(topping);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách Topping đang bán: " + e.getMessage());
            e.printStackTrace();
        }
        return toppingList;
    }

    /**Lấy Topping theo ID (Dùng cho việc tính tiền)
     *
     */
    public Topping getToppingById(int toppingId) {
        String query = "SELECT * FROM topping WHERE topping_id = ?";

        try (Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, toppingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Topping topping = new Topping();
                    topping.setToppingId(rs.getInt("topping_id"));
                    topping.setName(rs.getString("name"));
                    topping.setPrice(rs.getBigDecimal("price"));
                    topping.setAvailable(rs.getBoolean("is_available"));
                    return topping;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy Topping theo ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Dành cho màn hình Quản lý (Admin):
     * Lấy TOÀN BỘ danh sách topping (kể cả những món đã tạm ngưng)
     */
    public List<Topping> getAllToppingsForAdmin() {
        List<Topping> toppingList = new ArrayList<>();
        String query = "SELECT * FROM topping";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Topping topping = new Topping();
                topping.setToppingId(rs.getInt("topping_id"));
                topping.setName(rs.getString("name"));
                topping.setPrice(rs.getBigDecimal("price"));
                topping.setAvailable(rs.getBoolean("is_available"));

                toppingList.add(topping);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy toàn bộ danh sách Topping: " + e.getMessage());
            e.printStackTrace();
        }
        return toppingList;
    }

    /**
     * Thêm Topping mới
     */
    public boolean insertTopping(Topping topping) {
        String sql = "INSERT INTO topping (name, price, is_available) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, topping.getName());
            ps.setBigDecimal(2, topping.getPrice());
            ps.setBoolean(3, true); // Mặc định khi mới tạo là true

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm Topping mới: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Cập nhật thông tin Topping (Sửa tên, giá, trạng thái)
     */
    public boolean updateTopping(Topping topping) {
        String sql = "UPDATE topping SET name = ?, price = ?, is_available = ? WHERE topping_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, topping.getName());
            ps.setBigDecimal(2, topping.getPrice());
            ps.setBoolean(3, topping.getAvailable());
            ps.setInt(4, topping.getToppingId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật Topping: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Xóa Topping
     */
    public boolean deleteTopping(int toppingId) {
        String sql = "UPDATE topping SET is_available = false WHERE topping_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, toppingId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa Topping: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}
