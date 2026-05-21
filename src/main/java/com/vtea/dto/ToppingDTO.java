package com.vtea.dto;

import java.math.BigDecimal;

public class ToppingDTO {
    private int toppingId;
    private String name;
    private BigDecimal price;
    private boolean isAvailable;

    public ToppingDTO() {

    }
    public ToppingDTO(boolean isAvailable, String name, BigDecimal price, int toppingId) {
        this.isAvailable = isAvailable;
        this.name = name;
        this.price = price;
        this.toppingId = toppingId;
    }

    public int getToppingId() {
        return toppingId;
    }

    public void setToppingId(int toppingId) {
        this.toppingId = toppingId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public boolean getAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }
}
