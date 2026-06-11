package com.vtea.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InventoryTransactionDTO {
    private int transactionId;
    private int ingredientId;
    private int adminId;
    private String changeType; // IMPORT, EXPORT, DAMAGE
    private BigDecimal quantityChanged;
    private String note;
    private LocalDateTime createdAt;

    public InventoryTransactionDTO() {}

    public InventoryTransactionDTO(int ingredientId, int adminId, String changeType, BigDecimal quantityChanged, String note) {
        this.ingredientId = ingredientId;
        this.adminId = adminId;
        this.changeType = changeType;
        this.quantityChanged = quantityChanged;
        this.note = note;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public BigDecimal getQuantityChanged() {
        return quantityChanged;
    }

    public String getNote() {
        return note;
    }

    public int getIngredientId() {
        return ingredientId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getChangeType() {
        return changeType;
    }

    public int getAdminId() {
        return adminId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public void setQuantityChanged(BigDecimal quantityChanged) {
        this.quantityChanged = quantityChanged;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setIngredientId(int ingredientId) {
        this.ingredientId = ingredientId;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }
}