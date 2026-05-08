package com.vtea.service;

import com.vtea.dao.ProductDAO;
import com.vtea.dto.ProductDTO;
import com.vtea.model.Product;

import java.math.BigDecimal;
import java.util.List;

public class ProductService {

    // Gọi DAO lên làm việc
    private ProductDAO productDAO = new ProductDAO();

    // =========================================================
    // NHÓM 1: LẤY DỮ LIỆU (READ) - Ném thẳng DTO ra cho UI
    // =========================================================

    /**
     * 1. Lấy tất cả món đang bán (Dùng cho máy POS)
     */
    public List<ProductDTO> getAllActiveProducts() {
        return productDAO.getAllActiveProduct();
    }

    /**
     * 2. Lấy TOÀN BỘ món (Kể cả đã xóa/ngưng bán - Dùng cho Admin)
     */
    public List<ProductDTO> getAllProductsForAdmin() {
        return productDAO.getAllProductForAdmin();
    }

    /**
     * 3. Lấy món theo danh mục (Khi thu ngân bấm lọc theo Trà sữa, Cà phê...)
     */
    public List<ProductDTO> getProductsByCategory(int categoryId) {
        return productDAO.getProductByCategory(categoryId);
    }


    /**
     * 4. Thêm món nước mới
     */
    public void createProduct(ProductDTO dto) throws Exception {
        // VALIDATION
        if (dto.getCategoryId() <= 0) {
            throw new Exception("Lỗi: Vui lòng chọn danh mục cho món nước!");
        }
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new Exception("Lỗi: Tên món nước không được để trống!");
        }
        if (dto.getPrice() == null || dto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("Lỗi: Giá bán phải lớn hơn 0 đồng!");
        }

        // MAPPING DTO -> MODEL
        Product model = new Product();
        model.setCategoryId(dto.getCategoryId());
        model.setName(dto.getName().trim());
        model.setPrice(dto.getPrice());
        model.setImageUrl(dto.getImageUrl());


        // GỌI DAO VÀ XỬ LÝ KẾT QUẢ
        boolean isSuccess = productDAO.insertProduct(model);
        if (!isSuccess) {
            throw new Exception("Lỗi hệ thống: Không thể lưu món nước vào Database!");
        }
    }

    /**
     * 5. Cập nhật thông tin món (Sửa tên, giá, hình ảnh, trạng thái)
     */
    public void updateProduct(ProductDTO dto) throws Exception {
        // KIỂM DUYỆT
        if (dto.getProductId() <= 0) {
            throw new Exception("Lỗi: Không xác định được món nước cần sửa!");
        }
        if (dto.getCategoryId() <= 0) {
            throw new Exception("Lỗi: Vui lòng chọn danh mục hợp lệ!");
        }
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new Exception("Lỗi: Tên món nước không được để trống!");
        }
        if (dto.getPrice() == null || dto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("Lỗi: Giá bán phải lớn hơn 0 đồng!");
        }

        // MAPPING DTO -> MODEL
        Product model = new Product();
        model.setProductId(dto.getProductId()); // BẮT BUỘC PHẢI CÓ để UPDATE
        model.setCategoryId(dto.getCategoryId());
        model.setName(dto.getName().trim());
        model.setPrice(dto.getPrice());
        model.setImageUrl(dto.getImageUrl());
        model.setAvailable(dto.isAvailable()); // BẮT BUỘC PHẢI CÓ để sếp Khôi phục món đã xóa

        // GỌI DAO
        boolean isSuccess = productDAO.updateProduct(model);
        if (!isSuccess) {
            throw new Exception("Lỗi hệ thống: Không thể cập nhật món nước!");
        }
    }

    /**
     * 6. Xóa mềm món nước (Ngưng bán)
     */
    public void softDeleteProduct(int productId) throws Exception {
        if (productId <= 0) {
            throw new Exception("Lỗi: Không xác định được món nước cần xóa!");
        }

        boolean isSuccess = productDAO.deleteProduct(productId);
        if (!isSuccess) {
            throw new Exception("Lỗi hệ thống: Không thể xóa món nước này!");
        }
    }
}