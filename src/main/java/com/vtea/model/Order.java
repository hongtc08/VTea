package com.vtea.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Order {
    private int orderId;
    private int userId;
    private Integer customerId;
    private BigDecimal totalAmount;
    private Timestamp createdAt;
    private String status;
    private String paymentMethod;

    public Order() {

    }

    public Order(Timestamp createdAt, Integer customerId, int orderId, String paymentMethod, String status, BigDecimal totalAmount, int userId) {
        this.createdAt = createdAt;
        this.customerId = customerId;
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.totalAmount = totalAmount;
        this.userId = userId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public int getOrderId() {
        return orderId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public int getUserId() {
        return userId;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
