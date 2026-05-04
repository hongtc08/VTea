package com.vtea.dao;

import com.vtea.dto.ProductDTO;
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
    public List<ProductDTO> getAllActiveProduct(){
        List<ProductDTO> productList = new ArrayList<>();

        String query = "SELECT p.*, c.name AS category_name " +
                "FROM product p " +
                "JOIN category c ON p.category_id = c.category_id " +
                "WHERE p.is_available = true";

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery()) {
            while(rs.next()){
                ProductDTO product = new ProductDTO();
                product.setProductId(rs.getInt("product_id"));
                product.setCategoryId(rs.getInt("category_id"));
                product.setName(rs.getString("name"));
                product.setPrice(rs.getBigDecimal("price"));
                product.setImageUrl(rs.getString("image_url"));
                product.setAvailable(rs.getBoolean("is_available"));

                product.setCategoryName(rs.getString("category_name"));

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
    public List<ProductDTO> getAllProductForAdmin() {
        List<ProductDTO> productList = new ArrayList<>();
        // Query lấy hết, dùng JOIN lấy tên danh mục
        String query = "SELECT p.*, c.name AS category_name " +
                "FROM product p " +
                "JOIN category c ON p.category_id = c.category_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ProductDTO product = new ProductDTO();
                product.setProductId(rs.getInt("product_id"));
                product.setCategoryId(rs.getInt("category_id"));
                product.setName(rs.getString("name"));
                product.setPrice(rs.getBigDecimal("price"));
                product.setImageUrl(rs.getString("image_url"));
                product.setAvailable(rs.getBoolean("is_available"));

                product.setCategoryName(rs.getString("category_name"));

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
    public List<ProductDTO> getProductByCategory(int categoryId){
        List<ProductDTO> productList = new ArrayList<>();
        String query = "SELECT p.*, c.name AS category_name " +
                "FROM product p " +
                "JOIN category c ON p.category_id = c.category_id " +
                "WHERE p.category_id = ? AND p.is_available = true";

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(query)){
            ps.setInt(1, categoryId);

            try(ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    ProductDTO product = new ProductDTO();
                    product.setProductId(rs.getInt("product_id"));
                    product.setCategoryId(rs.getInt("category_id"));
                    product.setName(rs.getString("name"));
                    product.setPrice(rs.getBigDecimal("price"));
                    product.setImageUrl(rs.getString("image_url"));
                    product.setAvailable(rs.getBoolean("is_available"));

                    product.setCategoryName(rs.getString("category_name"));

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
