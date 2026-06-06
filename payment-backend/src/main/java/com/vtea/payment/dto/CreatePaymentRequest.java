package com.vtea.payment.dto;
/**
 * Request từ app JavaFX gửi lên backend để tạo giao dịch payOS.
 */
public class CreatePaymentRequest {
    private long amount;
    private String description;

    public CreatePaymentRequest() {
    }

    public CreatePaymentRequest(long amount, String description) {
        this.amount = amount;
        this.description = description;
    }

    public long getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}