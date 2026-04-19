package com.vtea.model;

import java.math.BigDecimal;

public class Product {
    private int productId;
    private int categoryId;
    private String name;
    private BigDecimal price;
    private String imageUrl;
    private String isAvailable;

    public Product() {

    }

    public Product(int categoryId, String imageUrl, String isAvailable, String name, BigDecimal price, int productId) {
        this.categoryId = categoryId;
        this.imageUrl = imageUrl;
        this.isAvailable = isAvailable;
        this.name = name;
        this.price = price;
        this.productId = productId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getIsAvailable() {
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

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setIsAvailable(String isAvailable) {
        this.isAvailable = isAvailable;
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
}
