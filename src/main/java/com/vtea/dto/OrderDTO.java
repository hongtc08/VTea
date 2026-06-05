package com.vtea.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderDTO {
    private int orderId;
    private int userId;
    private Integer customerId; // Dùng Integer vì có thể null
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private String status;
    private String paymentMethod;

    private String staffName;
    private String customerName;

    public OrderDTO() {}

    // --- GETTERS & SETTERS ---
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getStaffName() { return staffName; }
    public void setStaffName(String staffName) { this.staffName = staffName; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
}
