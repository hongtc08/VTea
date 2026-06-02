package com.vtea.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO dùng cho màn hình lịch sử hóa đơn.
 * Chỉ lưu thông tin tóm tắt để hiển thị danh sách hóa đơn.
 */
public class OrderHistoryDTO {
    private int orderId;
    private LocalDateTime createdAt;
    private String staffName;
    private String customerName;
    private String customerPhone;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private String status;

    public OrderHistoryDTO() {
    }

    public OrderHistoryDTO(
            int orderId,
            LocalDateTime createdAt,
            String staffName,
            String customerName,
            String customerPhone,
            BigDecimal totalAmount,
            String paymentMethod,
            String status
    ) {
        this.orderId = orderId;
        this.createdAt = createdAt;
        this.staffName = staffName;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.status = status;
    }

    public int getOrderId() {
        return orderId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getStaffName() {
        return staffName;
    }

    /**
     * Nếu hóa đơn không có khách hàng thì hiển thị khách vãng lai.
     */
    public String getCustomerName() {
        if (customerName == null || customerName.isBlank()) {
            return "Khách vãng lai";
        }
        return customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getStatus() {
        return status;
    }
}