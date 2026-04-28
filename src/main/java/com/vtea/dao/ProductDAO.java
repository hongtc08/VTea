package com.vtea.dao;

import com.vtea.model.Product;
import com.vtea.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    /**
     * Dành cho Màn hình POS:
     * Lấy tất cả các món đang còn bán (is_available = true)
     */
    public List<Product> getAllActiveProduct(){
        List<Product> productList = new ArrayList<>();
        String query = "SELECT * FROM product WHERE is_available = true";

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery()) {
            while(rs.next()){
                Product product = new Product();
                product.setProductId(rs.getInt("product_id"));
                product.setCategoryId(rs.getInt("category_id"));
                product.setName(rs.getString("name"));
                product.setPrice(rs.getBigDecimal("price"));
                product.setImageUrl(rs.getString("image_url"));
                product.setAvailable(rs.getBoolean("is_available"));

                productList.add(product);
            }
        } catch(SQLException e){
            System.err.println("Lỗi khi lấy danh sách tất cả món đang bán: " + e.getMessage());
            e.printStackTrace();
        }
        return productList;
    }

    /**
     * Dành cho Màn hình Quản lý của Admin:
     * Lấy TOÀN BỘ món nước (kể cả những món đã bị xóa/tạm ngưng)
     */
    public List<Product> getAllProductForAdmin() {
        List<Product> productList = new ArrayList<>();
        // Query lấy hết, không lọc trạng thái
        String query = "SELECT * FROM product";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Product product = new Product();
                product.setProductId(rs.getInt("product_id"));
                product.setCategoryId(rs.getInt("category_id"));
                product.setName(rs.getString("name"));
                product.setPrice(rs.getBigDecimal("price"));
                product.setImageUrl(rs.getString("image_url"));
                product.setAvailable(rs.getBoolean("is_available"));

                productList.add(product);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy toàn bộ danh sách sản phẩm: " + e.getMessage());
            e.printStackTrace();
        }
        return productList;
    }

    /**
     *  Lấy món theo phân loại
     */
    public List<Product> getProductByCategory(int categoryId){
        List<Product> productList = new ArrayList<>();
        String query = "SELECT * FROM product WHERE category_id = ? AND is_available = true";

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(query)){
            ps.setInt(1, categoryId);

            try(ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    Product product = new Product();
                    product.setProductId(rs.getInt("product_id"));
                    product.setCategoryId(rs.getInt("category_id"));
                    product.setName(rs.getString("name"));
                    product.setPrice(rs.getBigDecimal("price"));
                    product.setImageUrl(rs.getString("image_url"));
                    product.setAvailable(rs.getBoolean("is_available"));

                    productList.add(product);
                }
            }
        } catch (SQLException e){
            System.err.println("Lỗi khi lấy danh sách product theo Category: " + e.getMessage());
            e.printStackTrace();
        }

        return productList;
    }

    /**
     * Thêm món nước mới
     */
    public boolean insertProduct(Product product) {
        String sql = "INSERT INTO product (category_id, name, price, image_url, is_available) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, product.getCategoryId());
            ps.setString(2, product.getName());
            ps.setBigDecimal(3, product.getPrice());
            ps.setString(4, product.getImageUrl());
            ps.setBoolean(5, true);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm sản phẩm: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Cập nhật thông tin món nước
     */
    public boolean updateProduct(Product product) {
        String sql = """
                    UPDATE product 
                    SET category_id = ?, name = ?, price = ?, image_url = ?, is_available = ? 
                    WHERE product_id = ?
                    """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, product.getCategoryId());
            ps.setString(2, product.getName());
            ps.setBigDecimal(3, product.getPrice());
            ps.setString(4, product.getImageUrl());
            ps.setBoolean(5, product.getAvailable());
            ps.setInt(6, product.getProductId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật sản phẩm: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Xóa món nước
     */
    public boolean deleteProduct(int productId) {
        String sql = "UPDATE product SET is_available = false WHERE product_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa mềm sản phẩm: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}
