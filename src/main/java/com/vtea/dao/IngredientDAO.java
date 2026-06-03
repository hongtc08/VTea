package com.vtea.dao;

import com.vtea.dto.IngredientDTO;
import com.vtea.model.Ingredient;
import com.vtea.utils.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class IngredientDAO {
    /**
     * Lấy danh sách nguyên liệu đang sử dụng.
     * Dành cho màn hình Kiểm kho cuối ngày của Staff.
     */
    public List<Ingredient> getAllActiveIngredients(){
        List<Ingredient> list = new ArrayList<>();
        String sql = "SELECT * FROM ingredient WHERE is_available = true";

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            while (rs.next()){
                Ingredient item = new Ingredient();
                item.setIngredientId(rs.getInt("ingredient_id"));
                item.setName(rs.getString("name"));
                item.setUnit(rs.getString("unit"));
                item.setStockQty(rs.getBigDecimal("stock_qty"));
                item.setAvailable(rs.getBoolean("is_available"));
                item.setMinStock(rs.getBigDecimal("min_stock"));
                item.setLastUpdated(rs.getTimestamp("last_updated").toLocalDateTime());
                int updatedBy = rs.getInt("updated_by");
                if (!rs.wasNull()) {
                    item.setUpdatedBy(updatedBy);
                }

                list.add(item);
            }
        } catch (SQLException e){
            System.err.println("Lỗi lấy danh sách nguyên liệu: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Dành cho Màn hình Quản lý (Admin):
     * Lấy TOÀN BỘ danh sách nguyên liệu (kể cả đã xóa/ngưng sử dụng)
     * Kèm theo TÊN nhân viên đã chốt kho cuối cùng.
     */
    public List<IngredientDTO> getAllIngredientsForAdmin() {
        List<IngredientDTO> list = new ArrayList<>();

        String sql = "SELECT i.*, u.full_name AS staff_name " +
                "FROM ingredient i " +
                "LEFT JOIN `user` u ON i.updated_by = u.user_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                IngredientDTO item = new IngredientDTO();

                item.setIngredientId(rs.getInt("ingredient_id"));
                item.setName(rs.getString("name"));
                item.setUnit(rs.getString("unit"));
                item.setStockQty(rs.getBigDecimal("stock_qty"));
                item.setAvailable(rs.getBoolean("is_available"));
                item.setMinStock(rs.getBigDecimal("min_stock"));
                item.setLastUpdated(rs.getTimestamp("last_updated").toLocalDateTime());

                int updatedBy = rs.getInt("updated_by");
                if (!rs.wasNull()) {
                    item.setUpdatedBy(updatedBy);
                }

                item.setStaffName(rs.getString("staff_name"));

                list.add(item);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy danh sách nguyên liệu: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Thêm nguyên liệu mới (Dành cho Admin).
     */
    public boolean insertIngredient(Ingredient item, int adminId) {
        String sql = "INSERT INTO ingredient (name, unit, stock_qty, min_stock, is_available, updated_by, last_updated) " +
                "VALUES (?, ?, ?, ?, true, ?, CURRENT_TIMESTAMP)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, item.getName());
            ps.setString(2, item.getUnit());
            ps.setBigDecimal(3, item.getStockQty());
            ps.setBigDecimal(4, item.getMinStock());
            ps.setInt(5, adminId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi thêm nguyên liệu: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * SỬA THÔNG TIN CƠ BẢN (Dành cho Admin đổi tên hoặc đổi đơn vị tính).
     * Lưu ý: Không dùng hàm này để cập nhật số lượng tồn kho.
     */
    public boolean updateIngredientInfo(Ingredient item, int adminId) {
        String sql = "UPDATE ingredient SET name = ?, unit = ?, min_stock = ?, stock_qty = ?, updated_by = ?, last_updated = CURRENT_TIMESTAMP " +
                "WHERE ingredient_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, item.getName());
            ps.setString(2, item.getUnit());
            ps.setBigDecimal(3, item.getMinStock());
            ps.setBigDecimal(4, item.getStockQty());
            ps.setInt(5, adminId);
            ps.setInt(6, item.getIngredientId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật thông tin nguyên liệu: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * XÓA nguyên liệu
     */
    public boolean deleteIngredient(int ingredientId){
        String sql = "UPDATE ingredient SET is_available = false WHERE ingredient_id = ?";

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setInt(1, ingredientId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e){
            System.err.println("Lỗi xóa nguyên liệu: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * CẬP NHẬT TỒN KHO THỰC TẾ CUỐI NGÀY (Dành cho Staff/Quản lý).
     * Cách dùng: Staff đếm trong kho còn bao nhiêu thì nhập số đó vào,
     */
    public boolean updateActualQuantity(int ingredientId, BigDecimal quantity, int userId){
        String sql = "UPDATE ingredient SET stock_qty = ?, last_updated = CURRENT_TIMESTAMP, updated_by = ? WHERE ingredient_id = ?";

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setBigDecimal(1, quantity);
            ps.setInt(2, userId);
            ps.setInt(3, ingredientId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e){
            System.err.println("Lỗi cập nhật số lượng tồn kho: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Lấy danh sách nguyên liệu sắp hết
     * Dùng để hiển thị chuông thông báo cho Quản lý.
     */
    public List<Ingredient> getLowStockAlerts() {
        List<Ingredient> list = new ArrayList<>();
        String sql = "SELECT * FROM ingredient WHERE is_available = true AND stock_qty <= min_stock";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Ingredient item = new Ingredient();
                item.setIngredientId(rs.getInt("ingredient_id"));
                item.setName(rs.getString("name"));
                item.setUnit(rs.getString("unit"));
                item.setStockQty(rs.getBigDecimal("stock_qty"));
                item.setMinStock(rs.getBigDecimal("min_stock"));

                list.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
