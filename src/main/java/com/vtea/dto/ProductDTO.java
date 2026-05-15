package com.vtea.dto;

import java.math.BigDecimal;

public class ProductDTO {
    private int productId;
    private int categoryId;
    private String categoryName; // Tên danh mục để hiển thị
    private String name;
    private BigDecimal price;
    private String imageUrl;
    private boolean isAvailable;
    private boolean inStock;

    public ProductDTO() {

    }

    public ProductDTO(int productId, int categoryId, String categoryName, String name, BigDecimal price, String imageUrl, boolean isAvailable) {
        this.productId = productId;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.isAvailable = isAvailable;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getProductId() {
        return productId;
    }

    public boolean isInStock() {
        return inStock;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public void setInStock(boolean inStock) {
        this.inStock = inStock;
    }
}
