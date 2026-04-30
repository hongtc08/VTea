package com.vtea.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private int orderId;
    private int userId;
    private int customerId;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private String status = "PENDING";
    private LocalDateTime createdAt;
    private String paymentMethod;

    // Danh sách các món trong giỏ
    private List<OrderDetail> details = new ArrayList<>();

    public Order() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters & Setters
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<OrderDetail> getDetails() { return details; }
    public void setDetails(List<OrderDetail> details) { this.details = details; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}