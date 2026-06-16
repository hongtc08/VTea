package com.vtea.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO lưu thông tin một dòng món trong hóa đơn.
 * Một dòng món có thể có nhiều topping đi kèm.
 */
public class BillItemDTO {
    private int detailId;
    private int productId;
    private String productName;
    private int quantity;
    private BigDecimal unitPrice;

    // Danh sách topping của dòng món này
    private List<BillToppingDTO> toppings = new ArrayList<>();

    public BillItemDTO() {
    }

    public BillItemDTO(int detailId, int productId, String productName, int quantity, BigDecimal unitPrice) {
        this.detailId = detailId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public int getDetailId() {
        return detailId;
    }


    public int getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public List<BillToppingDTO> getToppings() {
        return toppings;
    }

    public void setToppings(List<BillToppingDTO> toppings) {
        this.toppings = toppings;
    }


    /**
     * Tính tiền món chính, chưa bao gồm topping.
     */
    public BigDecimal getProductTotal() {
        if (unitPrice == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}