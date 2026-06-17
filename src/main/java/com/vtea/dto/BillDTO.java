package com.vtea.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO lưu toàn bộ thông tin của một hóa đơn.
 * Dùng chung cho bill preview, xem chi tiết hóa đơn và xuất PDF.
 */
public class BillDTO {
    private int orderId;
    private LocalDateTime createdAt;

    private Integer userId;
    private String staffName;

    private Integer customerId;
    private String customerName;
    private String customerPhone;

    private String paymentMethod;
    private String status;
    private BigDecimal totalAmount;

    private BigDecimal tierDiscountAmount;
    private BigDecimal pointDiscountAmount;

    // --- Bổ sung Voucher ---
    private Integer voucherId;
    private String voucherCode;
    private BigDecimal voucherDiscountAmount;
    // -----------------------

    private String tierName;
    private int discountPercent;

    // Danh sách các món trong hóa đơn
    private List<BillItemDTO> items = new ArrayList<>();

    public BillDTO() {
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    /**
     * Nếu hóa đơn không gắn khách hàng thì hiển thị là khách vãng lai.
     */
    public String getCustomerName() {
        if (customerName == null || customerName.isBlank()) {
            return "Khách vãng lai";
        }
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Tránh lỗi null khi bill chưa có tổng tiền.
     */
    public BigDecimal getTotalAmount() {
        if (totalAmount == null) {
            return BigDecimal.ZERO;
        }
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<BillItemDTO> getItems() {
        return items;
    }

    public BigDecimal getTierDiscountAmount() {
        if (tierDiscountAmount == null) {
            return BigDecimal.ZERO;
        }
        return tierDiscountAmount;
    }

    public void setTierDiscountAmount(BigDecimal tierDiscountAmount) {
        this.tierDiscountAmount = tierDiscountAmount;
    }

    public BigDecimal getPointDiscountAmount() {
        if (pointDiscountAmount == null) {
            return BigDecimal.ZERO;
        }
        return pointDiscountAmount;
    }

    public void setPointDiscountAmount(BigDecimal pointDiscountAmount) {
        this.pointDiscountAmount = pointDiscountAmount;
    }

    public Integer getVoucherId() {
        return voucherId;
    }

    public void setVoucherId(Integer voucherId) {
        this.voucherId = voucherId;
    }

    public String getVoucherCode() {
        return voucherCode;
    }

    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }

    public BigDecimal getVoucherDiscountAmount() {
        if (voucherDiscountAmount == null) {
            return BigDecimal.ZERO;
        }
        return voucherDiscountAmount;
    }

    public void setVoucherDiscountAmount(BigDecimal voucherDiscountAmount) {
        this.voucherDiscountAmount = voucherDiscountAmount;
    }


    public String getTierName() {
        return tierName;
    }

    public void setTierName(String tierName) {
        this.tierName = tierName;
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(int discountPercent) {
        this.discountPercent = discountPercent;
    }
    /**
     * Thêm một dòng món vào hóa đơn.
     * Dùng khi BillDAO đọc danh sách order_detail.
     */
    public void addItem(BillItemDTO item) {
        this.items.add(item);
    }
}