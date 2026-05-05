package com.vtea.service;

import com.vtea.dao.OrderDAO;
import com.vtea.dto.OrderDetailDTO;
import com.vtea.model.Order;
import com.vtea.model.OrderDetail;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OrderService {

    private Order currentOrder;
    private List<OrderDetailDTO> cartItems;
    private OrderDAO orderDAO;

    public OrderService() {
        this.currentOrder = new Order();
        this.cartItems = new ArrayList<>();
        this.orderDAO = new OrderDAO();
    }

    public void addToCart(int productId, String productName, BigDecimal price, int quantity) {
        boolean itemExists = false;
        for (OrderDetailDTO item : cartItems) {
            if (item.getProductId() == productId) {
                item.setQuantity(item.getQuantity() + quantity);
                itemExists = true;
                break;
            }
        }
        if (!itemExists) {
            OrderDetailDTO newItem = new OrderDetailDTO(productId, productName, quantity, price);
            cartItems.add(newItem);
        }
        calculateTotal();
    }

    private void calculateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderDetailDTO item : cartItems) {
            total = total.add(item.getSubTotal());
        }
        currentOrder.setTotalAmount(total);
    }

    public List<OrderDetailDTO> getCartItems() {
        return cartItems;
    }

    public Order getCurrentOrder() {
        return currentOrder;
    }

    public List<OrderDetail> getDetailsForCheckout(int savedOrderId) {
        List<OrderDetail> detailsForDB = new ArrayList<>();
        for (OrderDetailDTO dto : cartItems) {
            OrderDetail detail = new OrderDetail(savedOrderId, dto.getProductId(), dto.getQuantity(), dto.getUnitPrice());
            detailsForDB.add(detail);
        }
        return detailsForDB;
    }

    public boolean checkoutCurrentOrder() {
        List<OrderDetail> details = getDetailsForCheckout(0);
        return orderDAO.checkoutOrder(currentOrder, details);
    }
}