package com.vtea.model;

import java.math.BigDecimal;

public class Recipe {
    private int productId;
    private int ingredientId;
    private BigDecimal qtyRequired;

    Recipe() {

    }

    public Recipe(int ingredientId, int productId, BigDecimal qtyRequired) {
        this.ingredientId = ingredientId;
        this.productId = productId;
        this.qtyRequired = qtyRequired;
    }

    public int getIngredientId() {
        return ingredientId;
    }

    public int getProductId() {
        return productId;
    }

    public BigDecimal getQtyRequired() {
        return qtyRequired;
    }

    public void setIngredientId(int ingredientId) {
        this.ingredientId = ingredientId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public void setQtyRequired(BigDecimal qtyRequired) {
        this.qtyRequired = qtyRequired;
    }
}
