package com.vtea.controller;

import com.vtea.service.OrderService;
import java.math.BigDecimal;

public class POSController {

    private OrderService orderService = new OrderService();

    public void handleAddToCart(int productId, String productName, double price) {
        orderService.addToCart(productId, productName, BigDecimal.valueOf(price), 1);

        System.out.println("Đã thêm " + productName + " vào giỏ!");
        System.out.println("Tổng tiền hiện tại: " + orderService.getCurrentOrder().getTotalAmount());
        
    }
}