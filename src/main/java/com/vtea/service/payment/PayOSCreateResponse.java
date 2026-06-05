package com.vtea.service.payment;
/**
 * DTO lưu thông tin backend trả về sau khi tạo thanh toán payOS.
 */
public class PayOSCreateResponse {
    private final long orderCode;
    private final String checkoutUrl;
    private final String status;

    public PayOSCreateResponse(long orderCode, String checkoutUrl, String status) {
        this.orderCode = orderCode;
        this.checkoutUrl = checkoutUrl;
        this.status = status;
    }

    public long getOrderCode() {
        return orderCode;
    }

    public String getCheckoutUrl() {
        return checkoutUrl;
    }

    public String getStatus() {
        return status;
    }
}