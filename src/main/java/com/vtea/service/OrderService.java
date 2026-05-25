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

        OrderDetailDTO newItem = new OrderDetailDTO(
                productId,
                productName.trim(),
                quantity,
                price
        );

        newItem.setToppingQuantities(safeToppingQuantities);
        newItem.setToppingPrice(toppingPrice);

        cartItems.add(newItem);

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

    /*----------------------------------------------------------------------
    Nếu có 2 ly nước (cùng id) mà mỗi ly có topping khác nhau sẽ dễ gây lỗi topping
    Cần đưa dữ liệu vào là item thay vì chỉ dùng id
     ----------------------------------------------------------------------*/
    public void increaseQuantity(OrderDetailDTO item) {
        if (item != null && cartItems.contains(item)) {
            item.setQuantity(item.getQuantity() + 1);
            calculateTotal();
        }
    }

    public void decreaseQuantity(OrderDetailDTO item) {
        if (item != null && cartItems.contains(item)) {
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
            } else {
                cartItems.remove(item);
            }
            calculateTotal();
        }
    }

    /*
        Khi người dùng bấm nút xóa món trong giỏ hàng.
        Hoặc khi bấm - mà quantity của món đang là 1.
    */
    public void removeFromCart(OrderDetailDTO item) {
        if (item != null && cartItems.contains(item)) {
            cartItems.remove(item);
            calculateTotal();
        }
    }

    public void addToppingToItem(OrderDetailDTO item, int toppingId) {
        if (item == null || !cartItems.contains(item)) {
            throw new IllegalArgumentException("Không tìm thấy món trong giỏ!");
        }

        Map<Integer, Integer> map = item.getToppingQuantities();

        if (map == null) {
            map = new HashMap<>();
        } else {
            map = new HashMap<>(map);
        }

        int prev = map.getOrDefault(toppingId, 0);
        map.put(toppingId, prev + 1);

        item.setToppingQuantities(map);
        item.setToppingPrice(calculateToppingPrice(map));
        calculateTotal();
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

    // Public helper: lấy tất cả topping đang bán (dùng cho UI)
    public java.util.List<Topping> getAllActiveToppings() {
        return toppingDAO.getAllActiveToppings();
    }

    // Public helper: tìm topping đang bán theo id
    public Topping findActiveToppingById(int toppingId) {
        List<Topping> list = toppingDAO.getAllActiveToppings();
        for (Topping t : list) {
            if (t.getToppingId() == toppingId) return t;
        }
        return null;
    }

    // Thêm 1 topping (toppingId) cho món đã có trong giỏ (baseProductId)
    public void addToppingToItem(int baseProductId, int toppingId) {
        OrderDetailDTO item = findCartItemByProductId(baseProductId);
        if (item == null) {
            throw new IllegalArgumentException("Không tìm thấy món trong giỏ: " + baseProductId);
        }

        Map<Integer, Integer> map = item.getToppingQuantities();
        int prev = map.getOrDefault(toppingId, 0);
        map.put(toppingId, prev + 1);
        item.setToppingQuantities(map);

        BigDecimal toppingPrice = calculateToppingPrice(map);
        item.setToppingPrice(toppingPrice);
        calculateTotal();
    }

    // Thay đổi số lượng topping (delta có thể là +1 hoặc -1), nếu kết quả <=0 thì xóa topping
    public void changeToppingQuantity(int baseProductId, int toppingId, int delta) {
        OrderDetailDTO item = findCartItemByProductId(baseProductId);
        if (item == null) return;

        Map<Integer, Integer> map = item.getToppingQuantities();
        int prev = map.getOrDefault(toppingId, 0);
        int now = prev + delta;
        if (now <= 0) {
            map.remove(toppingId);
        } else {
            map.put(toppingId, now);
        }

        item.setToppingQuantities(map);
        BigDecimal toppingPrice = calculateToppingPrice(map);
        item.setToppingPrice(toppingPrice);
        calculateTotal();
    }

    public void removeToppingFromItem(int baseProductId, int toppingId) {
        OrderDetailDTO item = findCartItemByProductId(baseProductId);
        if (item == null) return;

        Map<Integer, Integer> map = item.getToppingQuantities();
        if (map.containsKey(toppingId)) {
            map.remove(toppingId);
            item.setToppingQuantities(map);
            BigDecimal toppingPrice = calculateToppingPrice(map);
            item.setToppingPrice(toppingPrice);
            calculateTotal();
        }
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