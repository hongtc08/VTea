package com.vtea.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InventoryCheck {
    private int logId;
    private int ingredientId;
    private int staffId;
    private BigDecimal systemQty;
    private BigDecimal actualQty;
    private BigDecimal difference;
    private String status;
    private LocalDateTime createdAt;
    private Integer approvedBy;

    public InventoryCheck() {
    }

    public InventoryCheck(int logId, int ingredientId, int staffId, BigDecimal systemQty, BigDecimal actualQty, BigDecimal difference, String status, LocalDateTime createdAt, Integer approvedBy) {
        this.logId = logId;
        this.ingredientId = ingredientId;
        this.staffId = staffId;
        this.systemQty = systemQty;
        this.actualQty = actualQty;
        this.difference = difference;
        this.status = status;
        this.createdAt = createdAt;
        this.approvedBy = approvedBy;
    }

    public BigDecimal getActualQty() {
        return actualQty;
    }

    public Integer getApprovedBy() {
        return approvedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public BigDecimal getDifference() {
        return difference;
    }

    public int getIngredientId() {
        return ingredientId;
    }

    public int getLogId() {
        return logId;
    }

    public int getStaffId() {
        return staffId;
    }

    public String getStatus() {
        return status;
    }

    public BigDecimal getSystemQty() {
        return systemQty;
    }

    public void setActualQty(BigDecimal actualQty) {
        this.actualQty = actualQty;
    }

    public void setApprovedBy(Integer approvedBy) {
        this.approvedBy = approvedBy;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setDifference(BigDecimal difference) {
        this.difference = difference;
    }

    public void setIngredientId(int ingredientId) {
        this.ingredientId = ingredientId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public void setStaffId(int staffId) {
        this.staffId = staffId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setSystemQty(BigDecimal systemQty) {
        this.systemQty = systemQty;
    }
}