package com.vtea.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InventoryTransaction {
    private int transactionId;
    private int ingredientId;
    private int adminId;
    private String changeType;
    private BigDecimal quantityChanged;
    private String note;
    private LocalDateTime createdAt;

    public InventoryTransaction() {
    }

    public InventoryTransaction(int transactionId, int ingredientId, int adminId, String changeType, BigDecimal quantityChanged, String note, LocalDateTime createdAt) {
        this.transactionId = transactionId;
        this.ingredientId = ingredientId;
        this.adminId = adminId;
        this.changeType = changeType;
        this.quantityChanged = quantityChanged;
        this.note = note;
        this.createdAt = createdAt;
    }

    public int getAdminId() {
        return adminId;
    }

    public String getChangeType() {
        return changeType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public int getIngredientId() {
        return ingredientId;
    }

    public String getNote() {
        return note;
    }

    public BigDecimal getQuantityChanged() {
        return quantityChanged;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setIngredientId(int ingredientId) {
        this.ingredientId = ingredientId;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setQuantityChanged(BigDecimal quantityChanged) {
        this.quantityChanged = quantityChanged;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }
}