package com.vtea.payment.dto;

public class CreatePaymentResponse {
    private long orderCode;
    private String checkoutUrl;
    private String status;

    public CreatePaymentResponse(long orderCode, String checkoutUrl, String status) {
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