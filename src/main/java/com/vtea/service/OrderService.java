package com.vtea.service;

import com.vtea.dao.OrderDAO;
import com.vtea.dao.ToppingDAO;
import com.vtea.dto.OrderDetailDTO;
import com.vtea.model.Order;
import com.vtea.model.OrderDetail;
import com.vtea.model.Topping;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class OrderService {

    private Order currentOrder;
    private final List<OrderDetailDTO> cartItems;
    private final OrderDAO orderDAO;
    private final ToppingDAO toppingDAO;

    public OrderService() {
        this.currentOrder = new Order();
        this.cartItems = new ArrayList<>();
        this.orderDAO = new OrderDAO();
        this.toppingDAO = new ToppingDAO();

        calculateTotal();
    }

    // ==================== CART METHODS ====================

    public void addToCart(int productId, String productName, BigDecimal price, int quantity) {
        addToCart(productId, productName, price, quantity, Collections.emptyMap());
    }

    public void addToCart(
            int productId,
            String productName,
            BigDecimal price,
            int quantity,
            Map<Integer, Integer> toppingQuantities
    ) {
        validateCartItem(productId, productName, price, quantity);

        Map<Integer, Integer> safeToppingQuantities = normalizeToppingQuantities(toppingQuantities);
        BigDecimal toppingPrice = calculateToppingPrice(safeToppingQuantities);

        OrderDetailDTO existingItem = findCartItemByProductIdAndToppings(productId, safeToppingQuantities);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            OrderDetailDTO newItem = new OrderDetailDTO(
                    productId,
                    productName.trim(),
                    quantity,
                    price
            );

            newItem.setToppingQuantities(safeToppingQuantities);
            newItem.setToppingPrice(toppingPrice);

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

    /*
        Tăng số lượng của món trong giỏ hàng theo productId.
        Nếu tìm thấy món thì cộng quantity lên 1 và cập nhật lại tổng tiền.
    */
    public void increaseQuantity(int productId) {
        OrderDetailDTO item = findCartItemByProductId(productId);

        if (item != null) {
            item.setQuantity(item.getQuantity() + 1);
            calculateTotal();
        }
    }

    // Giảm số lượng của món trong giỏ hàng theo productId.
    public void decreaseQuantity(int productId) {
        OrderDetailDTO item = findCartItemByProductId(productId);

        if (item != null && item.getQuantity() > 1) {
            item.setQuantity(item.getQuantity() - 1);
            calculateTotal();
        } else if (item != null && item.getQuantity() == 1) {
            cartItems.remove(item);
            calculateTotal();
        }
    }

    /*
        Khi người dùng bấm nút xóa món trong giỏ hàng.
        Hoặc khi bấm - mà quantity của món đang là 1.
    */
    public void removeFromCart(int productId) {
        OrderDetailDTO item = findCartItemByProductId(productId);

        if (item != null) {
            cartItems.remove(item);
            calculateTotal();
        }
    }

    // ==================== CHECKOUT METHODS ====================

    public List<OrderDetail> getDetailsForCheckout(int savedOrderId) {
        List<OrderDetail> detailsForDB = new ArrayList<>();

        for (OrderDetailDTO dto : cartItems) {
            OrderDetail detail = new OrderDetail(
                    savedOrderId,
                    dto.getProductId(),
                    dto.getQuantity(),
                    dto.getUnitPrice()
            );

            detail.setToppingQuantities(dto.getToppingQuantities());

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

    // ==================== FIND CART ITEM METHODS ====================

    private OrderDetailDTO findCartItemByProductId(int productId) {
        for (OrderDetailDTO item : cartItems) {
            if (item.getProductId() == productId) {
                return item;
            }
        }

        return null;
    }

    //Xu li order co topping rieng
    private OrderDetailDTO findCartItemByProductIdAndToppings(
            int productId,
            Map<Integer, Integer> toppingQuantities
    ) {
        for (OrderDetailDTO item : cartItems) {
            if (item.getProductId() == productId
                    && Objects.equals(item.getToppingQuantities(), toppingQuantities)) {
                return item;
            }
        }

        return null;
    }

    // ==================== TOPPING METHODS ====================

    private Map<Integer, Integer> normalizeToppingQuantities(Map<Integer, Integer> toppingQuantities) {
        Map<Integer, Integer> normalized = new HashMap<>();

        if (toppingQuantities == null || toppingQuantities.isEmpty()) {
            return normalized;
        }

        for (Map.Entry<Integer, Integer> entry : toppingQuantities.entrySet()) {
            Integer toppingId = entry.getKey();
            Integer quantity = entry.getValue();

            if (toppingId == null || toppingId <= 0) {
                throw new IllegalArgumentException("Topping ID không hợp lệ!");
            }

            if (quantity == null || quantity <= 0) {
                throw new IllegalArgumentException("Số lượng topping phải lớn hơn 0!");
            }

            normalized.put(toppingId, quantity);
        }

        return normalized;
    }

    private BigDecimal calculateToppingPrice(Map<Integer, Integer> toppingQuantities) {
        if (toppingQuantities == null || toppingQuantities.isEmpty()) {
            return BigDecimal.ZERO;
        }

        List<Topping> activeToppings = toppingDAO.getAllActiveToppings();
        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<Integer, Integer> entry : toppingQuantities.entrySet()) {
            int toppingId = entry.getKey();
            int quantity = entry.getValue();

            Topping topping = findActiveToppingById(activeToppings, toppingId);

            if (topping == null) {
                throw new IllegalArgumentException("Topping không tồn tại hoặc đã ngừng bán: " + toppingId);
            }

            total = total.add(topping.getPrice().multiply(BigDecimal.valueOf(quantity)));
        }

        return total;
    }

    private Topping findActiveToppingById(List<Topping> toppings, int toppingId) {
        for (Topping topping : toppings) {
            if (topping.getToppingId() == toppingId) {
                return topping;
            }
        }

        return null;
    }

    // ==================== CALCULATE / VALIDATE METHODS ====================

    private void calculateTotal() {
        BigDecimal subtotal = BigDecimal.ZERO;

        for (OrderDetailDTO item : cartItems) {
            subtotal = subtotal.add(item.getSubTotal());
        }

        BigDecimal vat = subtotal.multiply(new BigDecimal("0.10"));
        BigDecimal total = subtotal.add(vat);

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