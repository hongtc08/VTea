package com.vtea.payment.dto;

public class PaymentStatusResponse {
    private long orderCode;
    private String status;

    public PaymentStatusResponse(long orderCode, String status) {
        this.orderCode = orderCode;
        this.status = status;
    }

    public long getOrderCode() {
        return orderCode;
    }

    public String getStatus() {
        return status;
    }
}