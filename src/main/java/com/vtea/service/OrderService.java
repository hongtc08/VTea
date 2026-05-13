package com.vtea.service;

import com.vtea.dao.OrderDAO;
import com.vtea.dto.OrderDetailDTO;
import com.vtea.model.Order;
import com.vtea.model.OrderDetail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrderService {

    private Order currentOrder;
    private final List<OrderDetailDTO> cartItems;
    private final OrderDAO orderDAO;

    public OrderService() {
        this.currentOrder = new Order();
        this.cartItems = new ArrayList<>();
        this.orderDAO = new OrderDAO();

        calculateTotal();
    }

    public void addToCart(int productId, String productName, BigDecimal price, int quantity) {
        validateCartItem(productId, productName, price, quantity);

        OrderDetailDTO existingItem = findCartItemByProductId(productId);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            OrderDetailDTO newItem = new OrderDetailDTO(
                    productId,
                    productName.trim(),
                    quantity,
                    price
            );

            cartItems.add(newItem);
        }

        calculateTotal();
    }

    public void clearCart() {
        cartItems.clear();
        currentOrder = new Order();
        calculateTotal();
    }

    public boolean isCartEmpty() {
        return cartItems.isEmpty();
    }

    public List<OrderDetailDTO> getCartItems() {
        return Collections.unmodifiableList(cartItems);
    }

    public Order getCurrentOrder() {
        return currentOrder;
    }

    public List<OrderDetail> getDetailsForCheckout(int savedOrderId) {
        List<OrderDetail> detailsForDB = new ArrayList<>();

        for (OrderDetailDTO dto : cartItems) {
            OrderDetail detail = new OrderDetail(
                    savedOrderId,
                    dto.getProductId(),
                    dto.getQuantity(),
                    dto.getUnitPrice()
            );

            detailsForDB.add(detail);
        }

        return detailsForDB;
    }

    public boolean checkoutCurrentOrder() {
        if (cartItems.isEmpty()) {
            return false;
        }

        calculateTotal();
        
        List<OrderDetail> details = getDetailsForCheckout(0);

        return orderDAO.checkoutOrder(currentOrder, details);
    }

    private OrderDetailDTO findCartItemByProductId(int productId) {
        for (OrderDetailDTO item : cartItems) {
            if (item.getProductId() == productId) {
                return item;
            }
        }

        return null;
    }

    private void calculateTotal() {
        BigDecimal total = BigDecimal.ZERO;

        for (OrderDetailDTO item : cartItems) {
            total = total.add(item.getSubTotal());
        }

        currentOrder.setTotalAmount(total);
    }

    private void validateCartItem(int productId, String productName, BigDecimal price, int quantity) {
        if (productId <= 0) {
            throw new IllegalArgumentException("Product ID không hợp lệ!");
        }

        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên sản phẩm không được để trống!");
        }

        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Giá sản phẩm phải lớn hơn 0!");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng sản phẩm phải lớn hơn 0!");
        }
    }
}