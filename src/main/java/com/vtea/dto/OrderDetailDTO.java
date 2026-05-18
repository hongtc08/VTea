package com.vtea.dto;

import java.math.BigDecimal;
import java.util.List;

public class OrderDetailDTO {

    private int productId;
    private String productName;
    private int quantity;
    private BigDecimal unitPrice;
    private List<String> toppingList;
    private BigDecimal toppingPrice; // Tổng tiền các topping của ly nước này

    public OrderDetailDTO() {
    }

    public OrderDetailDTO(int productId, String productName, int quantity, BigDecimal unitPrice) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public BigDecimal getSubTotal() {
        if (unitPrice == null || quantity <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal currentToppingPrice = (toppingPrice != null) ? toppingPrice : BigDecimal.ZERO;
        BigDecimal totalUnitPrice = unitPrice.add(currentToppingPrice);
        return totalUnitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Số lượng không được âm!");
        }

        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        if (unitPrice != null && unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Đơn giá không được âm!");
        }

        this.unitPrice = unitPrice;
    }

    public List<String> getToppingList() { return toppingList; }
    public void setToppingList(List<String> toppingList) { this.toppingList = toppingList; }

    public BigDecimal getToppingPrice() { return toppingPrice; }
    public void setToppingPrice(BigDecimal toppingPrice) { this.toppingPrice = toppingPrice; }
}