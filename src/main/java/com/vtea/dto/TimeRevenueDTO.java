package com.vtea.dto;

import java.math.BigDecimal;

public class TimeRevenueDTO {
    private String timeLabel; // chứa chuỗi dạng "2026-06-01" hoặc "Tháng 01"
    private int totalOrders;
    private BigDecimal totalRevenue;

    public TimeRevenueDTO() {}

    public String getTimeLabel() {
        return timeLabel;
    }

    public void setTimeLabel(String timeLabel) {
        this.timeLabel = timeLabel;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}
