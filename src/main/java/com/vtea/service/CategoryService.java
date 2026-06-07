package com.vtea.service;

import com.vtea.dao.CategoryDAO;
import com.vtea.dao.CategoryHibernateDAO;
import com.vtea.dao.ProductDAO;
import com.vtea.dto.CategoryDTO;
import com.vtea.dto.ProductDTO;
import com.vtea.model.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryService {
    //private CategoryDAO categoryDAO = new CategoryDAO();
    private CategoryHibernateDAO categoryHibernateDAO = new CategoryHibernateDAO();
    private ProductDAO productDAO = new ProductDAO(); // Cần để check điều kiện xóa

    // 1. Lấy danh sách đang bán (Có Mapping từ Model -> DTO)
    public List<CategoryDTO> getAllActiveCategories() {
        List<Category> modelList = categoryHibernateDAO.getAllActiveCategories();
        //List<Category> modelList = categoryDAO.getActiveCategories();
        List<CategoryDTO> dtoList = new ArrayList<>();

        for (Category model : modelList) {
            CategoryDTO dto = new CategoryDTO();
            dto.setCategoryId(model.getCategoryId());
            dto.setName(model.getName());
            dto.setDescription(model.getDescription());
            dto.setAvailable(model.getAvailable());
            dtoList.add(dto);
        }
        return dtoList;
    }

    // Lấy TOÀN BỘ (kể cả đã xóa mềm) để Admin quản lý
    public List<CategoryDTO> getAllCategories() {
        List<Category> modelList = categoryHibernateDAO.getAllCategories();
        List<CategoryDTO> dtoList = new ArrayList<>();

        for (Category model : modelList) {
            CategoryDTO dto = new CategoryDTO();
            dto.setCategoryId(model.getCategoryId());
            dto.setName(model.getName());
            dto.setDescription(model.getDescription());
            dto.setAvailable(model.getAvailable());
            dtoList.add(dto);
        }
        return dtoList;
    }

    // 2. Thêm mới danh mục
    public void createCategory(CategoryDTO dto) throws Exception {
        // Kiểm duyệt dữ liệu (Validation)
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new Exception("Tên danh mục không được để trống!");
        }

        // Biến hình DTO thành Model để đẩy xuống DAO
        Category model = new Category();
        model.setName(dto.getName().trim());
        model.setDescription(dto.getDescription());

        boolean isSuccess = categoryHibernateDAO.insertCategory(model);
        if (!isSuccess) {
            throw new Exception("Lỗi hệ thống: Không thể thêm danh mục vào Database!");
        }
    }

    // 3. Cập nhật thông tin danh mục
    public void updateCategory(CategoryDTO dto) throws Exception {
        // 1. Kiểm duyệt (Validation)
        if (dto.getCategoryId() <= 0) {
            throw new Exception("Lỗi: Không tìm thấy ID danh mục cần sửa!");
        }
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new Exception("Tên danh mục không được để trống!");
        }

        // 2. Biến hình DTO -> Model
        Category model = new Category();
        model.setCategoryId(dto.getCategoryId()); // Phải có cái này thì DB mới sửa đúng chỗ
        model.setName(dto.getName().trim());
        model.setDescription(dto.getDescription());
        model.setAvailable(dto.getAvailable()); // Cho phép sếp sửa cả trạng thái (Ẩn/Hiện)

        // 3. Gọi DAO
        boolean isSuccess = categoryHibernateDAO.updateCategory(model);
        if (!isSuccess) {
            throw new Exception("Lỗi hệ thống: Không thể cập nhật danh mục!");
        }
    }

    // 4. Xóa mềm (CỰC KỲ QUAN TRỌNG: Ràng buộc nghiệp vụ)
    public void softDeleteCategory(int categoryId) throws Exception {
        // Ràng buộc: Kiểm tra xem danh mục này có còn món nước nào đang bán không?
        List<ProductDTO> productsInCat = productDAO.getProductByCategory(categoryId);
        if (!productsInCat.isEmpty()) {
            throw new Exception("Không thể xóa! Danh mục này vẫn đang chứa " + productsInCat.size() + " món nước.");
        }

        // Nếu qua được cửa ải trên thì mới gọi DAO để xóa mềm
        boolean isSuccess = categoryHibernateDAO.softDeleteCategory(categoryId);
        if (!isSuccess) {
            throw new Exception("Lỗi hệ thống: Không thể xóa danh mục!");
        }
    }
}