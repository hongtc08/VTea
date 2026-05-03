package com.vtea.dto;

import com.vtea.model.Product;

public class ProductDTO extends Product {
    private String categoryName;

    public ProductDTO() {
    }

    public String getCategoryName() { return categoryName; }

    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
}
