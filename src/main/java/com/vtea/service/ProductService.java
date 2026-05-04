package com.vtea.service;

import com.vtea.dao.ProductDAO;
import com.vtea.dto.ProductDTO;
import java.util.List;

public class ProductService {

    private ProductDAO productDAO;

    public ProductService() {
        this.productDAO = new ProductDAO();
    }

    public List<ProductDTO> getAllActiveProducts() {
        return productDAO.getAllActiveProduct();
    }

    public List<ProductDTO> getProductsByCategory(int categoryId) {
        return productDAO.getProductByCategory(categoryId);
    }
}