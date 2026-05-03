package com.vtea.dao;

import com.vtea.model.Category;
import com.vtea.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class CategoryDAO {
    /**
     * Lấy danh sách các phân loại đang active
     * Dùng cho màn hình POS
     */
    public List<Category>  getActiveCategories(){
        List<Category> categoryList = new ArrayList<>();
        String query = "SELECT * FROM category WHERE is_available = true";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()){
            while(rs.next()){
                Category cat = new Category();
                cat.setCategoryId(rs.getInt("category_id"));
                cat.setName(rs.getString("name"));
                cat.setDescription(rs.getString("description"));
                cat.setAvailable(rs.getBoolean("is_available"));

                categoryList.add(cat);
            }
        } catch (SQLException e){
            System.err.println("Lỗi khi lấy danh sách Category: " + e.getMessage());
            e.printStackTrace();;
        }
        return categoryList;
    }

    /**
     * Lấy danh sách TẤT CẢ các phân loại
     * Dùng cho màn hình của ADMIN
     */
    public List<Category>  getAllCategories(){
        List<Category> categoryList = new ArrayList<>();
        String query = "SELECT * FROM category";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()){
            while(rs.next()){
                Category cat = new Category();
                cat.setCategoryId(rs.getInt("category_id"));
                cat.setName(rs.getString("name"));
                cat.setDescription(rs.getString("description"));
                cat.setAvailable(rs.getBoolean("is_available"));

                categoryList.add(cat);
            }
        } catch (SQLException e){
            System.err.println("Lỗi khi lấy danh sách Category: " + e.getMessage());
            e.printStackTrace();;
        }
        return categoryList;
    }

    // Thêm phân loại
    public boolean insertCategory(Category category){
        String sql = "INSERT INTO category (name, description, is_available) VALUES (?, ?, ?)";

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            ps.setBoolean(3, true);

            // executeUpdate() trả về số dòng bị ảnh hưởng, > 0 nghĩa là insert thành công
            return ps.executeUpdate() > 0;
        } catch (SQLException e){
            System.err.println("Lỗi khi thêm phân loại: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Cập nhật phân loại
    public Boolean updateCategory(Category category){
        String sql = "UPDATE category SET name = ?, description = ?, is_available = ? WHERE category_id = ?";

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            ps.setBoolean(3, category.getAvailable());
            ps.setInt(4, category.getCategoryId());

            return ps.executeUpdate() > 0;
        } catch(SQLException e){
            System.err.println("Lỗi khi cập nhật phân loại: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Xóa phân loại
    public boolean delateCategory(int categoryId){
        String sql = "UPDATE category SET is_available = false WHERE category_id = ?";

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1, categoryId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e){
            System.err.println("Lỗi khi xóa phân loại: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

}
