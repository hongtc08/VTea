package com.vtea.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Ingredient {
    private int ingredientId;
    private String name;
    private String unit;
    private  BigDecimal stockQty;
    private boolean isAvailable;
    private BigDecimal minStock;
    private LocalDateTime lastUpdated;
    private Integer updatedBy;

    public Ingredient() {

    }

    public Ingredient(int ingredientId, String name, BigDecimal stockQty, String unit, boolean isAvailable, BigDecimal minStock, LocalDateTime lastUpdated, Integer updatedBy) {
        this.ingredientId = ingredientId;
        this.name = name;
        this.stockQty = stockQty;
        this.unit = unit;
        this.isAvailable = isAvailable;
        this.minStock = minStock;
        this.lastUpdated = lastUpdated;
        this.updatedBy = updatedBy;
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

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Integer getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Integer updatedBy) {
        this.updatedBy = updatedBy;
    }
}
