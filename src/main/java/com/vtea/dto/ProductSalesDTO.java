package com.vtea.dto;

//// Dùng vẽ biểu đồ cột BarChart - Top 5 món bán chạy nhất
////  Cũng dùng cho danh sách sản phẩm bán chạy trên Dashboard
//
public class ProductSalesDTO {
    private String productName;
    private int totalQuantitySold;
    private java.math.BigDecimal totalRevenue;

    public ProductSalesDTO() {}

    public ProductSalesDTO(String productName, int totalQuantitySold) {
        this.productName = productName;
        this.totalQuantitySold = totalQuantitySold;
    }

    public ProductSalesDTO(String productName, int totalQuantitySold, java.math.BigDecimal totalRevenue) {
        this.productName = productName;
        this.totalQuantitySold = totalQuantitySold;
        this.totalRevenue = totalRevenue;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getTotalQuantitySold() {
        return totalQuantitySold;
    }

    public void setTotalQuantitySold(int totalQuantitySold) {
        this.totalQuantitySold = totalQuantitySold;
    }

    public java.math.BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(java.math.BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}
