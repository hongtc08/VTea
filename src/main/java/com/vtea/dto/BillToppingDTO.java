package com.vtea.dto;

import java.math.BigDecimal;

/**
 * DTO lưu thông tin topping trong hóa đơn.
 * Mỗi topping thuộc về một dòng món trong bill.
 */
public class BillToppingDTO {
    private int toppingId;
    private String toppingName;
    private int quantity;
    private BigDecimal unitPrice;

    public BillToppingDTO() {
    }

    public BillToppingDTO(int toppingId, String toppingName, int quantity, BigDecimal unitPrice) {
        this.toppingId = toppingId;
        this.toppingName = toppingName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getToppingName() {
        return toppingName;
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

    /**
     * Tính tổng tiền topping trên 1 dòng món.
     * Lưu ý: phần nhân với số lượng ly sẽ xử lý ở BillItemDTO.
     */
    public BigDecimal getTotalPrice() {
        if (unitPrice == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}