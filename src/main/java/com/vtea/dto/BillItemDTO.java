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

    public void setDetailId(int detailId) {
        this.detailId = detailId;
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
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public List<BillToppingDTO> getToppings() {
        return toppings;
    }

    public void setToppings(List<BillToppingDTO> toppings) {
        this.toppings = toppings;
    }

    /**
     * Thêm topping vào dòng món hiện tại.
     * Dùng khi BillDAO đọc dữ liệu từ bảng order_detail_topping.
     */
    public void addTopping(BillToppingDTO topping) {
        this.toppings.add(topping);
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

    /**
     * Tính tổng tiền topping của dòng món.
     * Vì topping đi theo từng ly nên cần nhân thêm số lượng món.
     */
    public BigDecimal getToppingTotal() {
        BigDecimal total = BigDecimal.ZERO;

        for (BillToppingDTO topping : toppings) {
            total = total.add(topping.getTotalPrice().multiply(BigDecimal.valueOf(quantity)));
        }

        return total;
    }

    /**
     * Tính tổng tiền của cả dòng món.
     * Bao gồm tiền món chính và tiền topping.
     */
    public BigDecimal getLineTotal() {
        return getProductTotal().add(getToppingTotal());
    }
}