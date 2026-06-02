package com.vtea.dto;

import com.vtea.model.Ingredient;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class IngredientDTO extends Ingredient {
    private int ingredientId;
    private String name;
    private String unit;
    private BigDecimal stockQty;
    private boolean isAvailable;
    private BigDecimal minStock;
    private Timestamp lastUpdated;
    private Integer updatedBy;
    private String staffName;

    public IngredientDTO() {

    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public int getIngredientId() {
        return ingredientId;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getStockQty() {
        return stockQty;
    }

    public String getUnit() {
        return unit;
    }

    public void setIngredientId(int ingredientId) {
        this.ingredientId = ingredientId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStockQty(BigDecimal stockQty) {
        this.stockQty = stockQty;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { this.isAvailable = available; }

    public BigDecimal getMinStock() {
        return minStock;
    }

    public void setMinStock(BigDecimal minStock) {
        this.minStock = minStock;
    }

    public Timestamp getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Timestamp lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Integer getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Integer updatedBy) {
        this.updatedBy = updatedBy;
    }
}
