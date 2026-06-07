package com.vtea.dto;

//// Dùng vẽ biểu đồ cột BarChart - Top 5 món bán chạy nhất
///
public class ProductSalesDTO {
    private String productName;
    private int totalQuantitySold;

    public ProductSalesDTO() {}

    public ProductSalesDTO(String productName, int totalQuantitySold) {
        this.productName = productName;
        this.totalQuantitySold = totalQuantitySold;
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
}
