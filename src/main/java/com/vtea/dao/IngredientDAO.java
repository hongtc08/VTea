package com.vtea.dao;

import com.vtea.model.Ingredient;
import com.vtea.utils.DBConnection;

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
                item.setStockQty(rs.getDouble("stock_qty"));
                item.setAvailable(rs.getBoolean("is_available"));
                list.add(item);
            }
        } catch (SQLException e){
            System.err.println("Lỗi lấy danh sách nguyên liệu: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Thêm nguyên liệu mới (Dành cho Admin).
     * Mặc định khi mới tạo, số lượng bằng 0.
     */
    public boolean insertIngredient(Ingredient item){
        String sql = "INSERT INTO ingredient (name, unit, stock_qty, is_available) VALUES (?,?,0,true)";

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setString(1, item.getName());
            ps.setString(2, item.getUnit());
            return ps.executeUpdate() > 0;
        } catch (SQLException e){
            System.err.println("Lỗi thêm nguyên liệu: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * SỬA THÔNG TIN CƠ BẢN (Dành cho Admin đổi tên hoặc đổi đơn vị tính).
     * Lưu ý: Không dùng hàm này để cập nhật số lượng tồn kho.
     */
    public boolean updateIngredient(Ingredient item){
        String sql = "UPDATE ingredient SET name = ?, unit = ? WHERE ingredient_id = ?";

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setString(1, item.getName());
            ps.setString(2, item.getUnit());
            ps.setInt(3, item.getIngredientId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e){
            System.err.println("Lỗi cập nhật thông tin nguyên liệu: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * XÓA nguyên liệu (Dành cho Admin khi quán không xài loại đồ này nữa).
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
     * Database sẽ ghi đè trực tiếp lên số cũ.
     */
    public boolean updateActualQuantity(int ingredientId, double quantity){
        String sql = "UPDATE ingredient SET stock_qty = ? WHERE ingredient_id = ?";

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setDouble(1, quantity);
            ps.setInt(2, ingredientId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e){
            System.err.println("Lỗi cập nhật số lượng tồn kho: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}
