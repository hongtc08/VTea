package com.vtea.dto;

import java.math.BigDecimal;

//////// Dùng cho các thẻ Card ở trên cùng:
/// Tổng doanh thu, Số đơn, Khách, Cảnh báo kho
///
public class DashboardSummaryDTO {
    private BigDecimal totalRevenue;
    private int totalOrders;
    private int totalCustomers;
    private int lowStockIngredientCount;


    public DashboardSummaryDTO() {
    }

    public DashboardSummaryDTO(BigDecimal totalRevenue, int totalOrders, int totalCustomers, int lowStockIngredientCount){
        this.totalRevenue = totalRevenue;
        this.totalCustomers = totalCustomers;
        this.totalOrders = totalOrders;
        this.lowStockIngredientCount = lowStockIngredientCount;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public int getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(int newCustomers) {
        this.totalCustomers = newCustomers;
    }

    public int getLowStockIngredientCount() {
        return lowStockIngredientCount;
    }

    public void setLowStockIngredientCount(int lowStockIngredientCount) {
        this.lowStockIngredientCount = lowStockIngredientCount;
    }
}
