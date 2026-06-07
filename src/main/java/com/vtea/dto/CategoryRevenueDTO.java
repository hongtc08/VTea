package com.vtea.dto;

import java.math.BigDecimal;

public class CategoryRevenueDTO {
    private String categoryName;
    private BigDecimal totalRevenue;

    public CategoryRevenueDTO() {}

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}
