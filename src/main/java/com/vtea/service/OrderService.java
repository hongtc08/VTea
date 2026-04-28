package com.vtea.service;

import com.vtea.model.Order;
import com.vtea.model.OrderDetail;
import java.math.BigDecimal;

public class OrderService {

    private Order currentOrder;

    public OrderService() {
        this.currentOrder = new Order();
    }

    public void addToCart(int productId, String productName, BigDecimal price, int quantity) {
        boolean itemExists = false;

        // Xem co trong gio chua
        for (OrderDetail item : currentOrder.getDetails()) {
            if (item.getProductId() == productId) {
                // Co roi thi + SL
                item.setQuantity(item.getQuantity() + quantity);
                itemExists = true;
                break;
            }
        }

        // Chua co -> tao moi
        if (!itemExists) {
            OrderDetail newItem = new OrderDetail(productId, productName, quantity, price);
            currentOrder.getDetails().add(newItem);
        }

        // Cap nhat tong tien
        calculateTotal();
    }

    private void calculateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderDetail item : currentOrder.getDetails()) {
            total = total.add(item.getSubTotal());
        }
        currentOrder.setTotalAmount(total);
    }

    public Order getCurrentOrder() {
        return currentOrder;
    }
}