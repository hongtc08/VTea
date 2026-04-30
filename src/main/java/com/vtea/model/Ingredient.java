package com.vtea.model;

import java.math.BigDecimal;

public class Ingredient {
    private int ingredientId;
    private String name;
    private String unit;
    private  BigDecimal stockQty;

    public Ingredient() {

    }

    public Ingredient(int ingredientId, String name, BigDecimal stockQty, String unit) {
        this.ingredientId = ingredientId;
        this.name = name;
        this.stockQty = stockQty;
        this.unit = unit;
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
}
