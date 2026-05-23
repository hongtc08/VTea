package com.vtea.model;

import java.math.BigDecimal;

public class Ingredient {
    private int ingredientId;
    private String name;
    private String unit;
    private  double stockQty;
    private boolean isAvailable;

    public Ingredient() {

    }

    public Ingredient(int ingredientId, String name, double stockQty, String unit, boolean isAvailable) {
        this.ingredientId = ingredientId;
        this.name = name;
        this.stockQty = stockQty;
        this.unit = unit;
        this.isAvailable = isAvailable;
    }

    public int getIngredientId() {
        return ingredientId;
    }

    public String getName() {
        return name;
    }

    public double getStockQty() {
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

    public void setStockQty(double stockQty) {
        this.stockQty = stockQty;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { this.isAvailable = available; }
}
