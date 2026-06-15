package com.vtea.service;

import com.vtea.dao.CustomerDAO;
import com.vtea.dao.OrderDAO;
import com.vtea.dao.ToppingDAO;
import com.vtea.dto.CustomerDTO;
import com.vtea.dto.OrderDetailDTO;
import com.vtea.model.Order;
import com.vtea.model.OrderDetail;
import com.vtea.model.Topping;
import com.vtea.utils.DBConnection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Logic đơn hàng
 * Thao tác giỏ hàng, tổng tiền, áp dụng điểm thưởng/giảm giá, và thanh toán
 */
public class OrderService {

    private Order currentOrder;
    private final List<OrderDetailDTO> cartItems;
    private final OrderDAO orderDAO;
    private final ToppingDAO toppingDAO;
    private final CustomerDAO customerDAO;
    private List<Topping> cachedActiveToppings;

    // Thông tin khách hàng và điểm thưởng
    private int currentCustomerId = 0;
    private CustomerDTO currentCustomerInfo = null; // Cache thông tin khách để tạm tính
    private int pointsToUse = 0;
    private int lastEarnedPoints = 0;
    private BigDecimal discountAmount = BigDecimal.ZERO;

    public static final BigDecimal POINT_CONVERSION_RATE = new BigDecimal("1000"); //quy đổi điểm

    public OrderService() {
        this.currentOrder = new Order();
        this.cartItems = new ArrayList<>();
        this.orderDAO = new OrderDAO();
        this.toppingDAO = new ToppingDAO();
        this.customerDAO = new CustomerDAO();
        this.cachedActiveToppings = toppingDAO.getAllActiveToppings();
        calculateTotal();
    }

    // ==================== THAO TÁC GIỎ ====================

    /**
     * Thêm món
     */
    public void addToCart(int productId, String productName, BigDecimal price, int quantity) {
        //Gọi hàm dưới với topping=empty
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

        //Loại topping null, <=0
        Map<Integer, Integer> safeToppingQuantities = normalizeToppingQuantities(toppingQuantities);

        BigDecimal toppingPrice = calculateToppingPrice(safeToppingQuantities);

        //Tạo DTO và gán giá trị
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

    /**
     * Tăng số lượng của một món trong giỏ
     */
    public void increaseQuantity(OrderDetailDTO item) {
        if (item != null && cartItems.contains(item)) {
            item.setQuantity(item.getQuantity() + 1);
            calculateTotal();
        }
    }

    /**
     * Giảm số lượng của một món trong giỏ (xóa nếu bằng 0)
     */
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

    /**
     * Xóa một món khỏi giỏ hàng
     */
    public void removeFromCart(OrderDetailDTO item) {
        if (item != null && cartItems.contains(item)) {
            cartItems.remove(item);
            calculateTotal();
        }
    }

    /**
     * Xóa toàn bộ giỏ hàng
     */
    public void clearCart() {
        cartItems.clear();
        currentOrder = new Order();
        currentCustomerId = 0;
        currentCustomerInfo = null;
        pointsToUse = 0;
        lastEarnedPoints = 0;
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

    // ==================== TOPPING ====================
    /**
     * Thêm topping
     */
    public void addToppingToItem(OrderDetailDTO item, int toppingId) {
        if (item == null || !cartItems.contains(item)) {
            throw new IllegalArgumentException("Khong tim thay mon trong gio!");
        }

        //Danh sách topping
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

    /**
     * Xóa topping
     */
    public void removeToppingFromItem(OrderDetailDTO item, int toppingId) {
        if (item == null || !cartItems.contains(item)) {
            return;
        }

        //Danh sách topping
        Map<Integer, Integer> map = item.getToppingQuantities();
        if (map == null || !map.containsKey(toppingId)) {
            return;
        }

        map = new HashMap<>(map);
        map.remove(toppingId);

        item.setToppingQuantities(map);
        //Tính lại tiền
        item.setToppingPrice(calculateToppingPrice(map));
        calculateTotal();
    }

    /**
     * Thay đổi số lượng topping
     */
    public void changeToppingQuantity(OrderDetailDTO item, int toppingId, int delta) {
        if (item == null || !cartItems.contains(item)) {
            return;
        }

        //Danh sách topping
        Map<Integer, Integer> map = item.getToppingQuantities();
        if (map == null) {
            map = new HashMap<>();
        } else {
            map = new HashMap<>(map);
        }

        //Lấy số lượng hiện tại rồi + delta
        int prev = map.getOrDefault(toppingId, 0);
        int now = prev + delta;

        if (now <= 0) {
            map.remove(toppingId);
        } else {
            map.put(toppingId, now);
        }

        item.setToppingQuantities(map);
        //Tính lại tiền
        item.setToppingPrice(calculateToppingPrice(map));
        calculateTotal();
    }


    public java.util.List<Topping> getAllActiveToppings() {
        return toppingDAO.getAllActiveToppings();
    }

    public Topping findActiveToppingById(int toppingId) {
        List<Topping> list = toppingDAO.getAllActiveToppings();
        for (Topping t : list) {
            if (t.getToppingId() == toppingId) return t;
        }
        return null;
    }

    // ==================== 3. KHÁCH HÀNG & ĐIỂM THƯỞNG ====================

    /**
     * Gắn khách hàng vào đơn hiện tại để áp dụng ưu đãi
     */
    public void setCustomer(int customerId) {
        if (customerId > 0) {
            this.currentCustomerId = customerId;
            this.currentOrder.setCustomerId(customerId);
            this.currentCustomerInfo = customerDAO.getCustomerById(customerId);
        } else {
            this.currentCustomerId = 0;
            this.currentOrder.setCustomerId(null);
            this.currentCustomerInfo = null;
        }

        this.pointsToUse = 0;
        calculateTotal();
    }

    /**
     * Sử dụng điểm thưởng để giảm giá đơn hàng
     */
    public void applyRewardPoints(int points) throws Exception {
        if (currentCustomerId <= 0 || currentCustomerInfo == null) {
            throw new Exception("Loi: Vui long chon khach hang thanh vien truoc!");
        }

        if (currentCustomerInfo.getRewardPoints() < points) {
            throw new Exception("Loi: Khach hang khong du diem!");
        }

        this.pointsToUse = points;
        calculateTotal();
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public int getLastEarnedPoints() {
        return lastEarnedPoints;
    }

    // ==================== 4. XỬ LÝ THANH TOÁN ====================

    /**
     * Thực hiện thanh toán: lưu DB, trừ/cộng điểm và reset giỏ
     */
    public boolean checkoutCurrentOrder() {
        if (cartItems.isEmpty()) {
            return false;
        }

        if (currentOrder.getCustomerId() != null && currentOrder.getCustomerId() > 0) {
            currentCustomerId = currentOrder.getCustomerId();
        } else {
            currentOrder.setCustomerId(null);
            currentCustomerId = 0;
            currentCustomerInfo = null;
        }

        List<OrderDetail> details = getDetailsForCheckout(0);
        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            if (conn == null) {
                throw new java.sql.SQLException("Khong the ket noi den co so du lieu.");
            }
            conn.setAutoCommit(false);

            // Tính tiền lần cuối trước khi lưu
            calculateTotal();

            int savedOrderId = orderDAO.checkoutOrder(conn, currentOrder, details);

            if (savedOrderId <= 0) {
                throw new Exception("Loi he thong: Khong the tao hoa don!");
            }

            currentOrder.setOrderId(savedOrderId);

            if (currentCustomerId > 0) {
                if (pointsToUse > 0) {
                    boolean deducted = customerDAO.deductRewardPoints(conn, currentCustomerId, pointsToUse);
                    if (!deducted) {
                        throw new java.sql.SQLException("Khach hang khong du diem de su dung hoac thong tin da thay doi.");
                    }
                }

                int pointsEarned = currentOrder.getTotalAmount()
                        .divide(new BigDecimal("10000"), RoundingMode.DOWN)
                        .intValue();

                if (pointsEarned > 0) {
                    boolean added = customerDAO.addPointsAndUpgradeTier(conn, currentCustomerId, pointsEarned);
                    if (!added) {
                        throw new java.sql.SQLException("Khong the cong diem cho khach hang.");
                    }
                }

                lastEarnedPoints = pointsEarned;
            }

            conn.commit();

            currentCustomerId = 0;
            currentCustomerInfo = null;
            pointsToUse = 0;

            return true;

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println(">>> TRANSACTION FAILED: Da Rollback giao dich an toan!");
                } catch (java.sql.SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;

        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (java.sql.SQLException e) {
                    e.printStackTrace();
                }
            }
        }
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

            detail.setToppingQuantities(dto.getToppingQuantities());
            detailsForDB.add(detail);
        }

        return detailsForDB;
    }

    // ==================== 5. TIỆN ÍCH NỘI BỘ ====================

    /**
     * Tính toán tổng tiền: tổng món, topping, chiết khấu hạng, điểm thưởng, VAT
     */
    private void calculateTotal() {
        BigDecimal subtotal = BigDecimal.ZERO;

        for (OrderDetailDTO item: cartItems) {
            subtotal = subtotal.add(item.getSubTotal());
        }

        BigDecimal tierDiscountAmount = BigDecimal.ZERO;

        // 1. Ap dung giam gia hang thanh vien tu cache
        if (currentCustomerInfo != null) {
            BigDecimal discountPercent = BigDecimal.valueOf(currentCustomerInfo.getDiscountPercent());
            tierDiscountAmount = subtotal.multiply(discountPercent).divide(new BigDecimal("100"), RoundingMode.DOWN);
        }

        BigDecimal amountAfterTierDiscount = subtotal.subtract(tierDiscountAmount);
        if (amountAfterTierDiscount.compareTo(BigDecimal.ZERO) < 0) {
            amountAfterTierDiscount = BigDecimal.ZERO;
        }

        // 2. Tinh toan tien giam gia tu diem thuong va ap dung luat TOI DA 50% don hang
        BigDecimal maxAllowedPointDiscount = amountAfterTierDiscount.multiply(new BigDecimal("0.50"));
        BigDecimal requestedPointDiscount = new BigDecimal(this.pointsToUse).multiply(POINT_CONVERSION_RATE);

        // Neu khach yeu cau dung diem vuot qua 50% gia tri don hang (sau khi giam hang)
        if (requestedPointDiscount.compareTo(maxAllowedPointDiscount) > 0) {
            requestedPointDiscount = maxAllowedPointDiscount;
            // Tinh nguoc lai so diem hop le toi da khach duoc phep dung
            this.pointsToUse = requestedPointDiscount.divide(POINT_CONVERSION_RATE, RoundingMode.DOWN).intValue();
            // Tinh lai chinh xac so tien duoc giam dua tren diem hop le nguyen con
            requestedPointDiscount = new BigDecimal(this.pointsToUse).multiply(POINT_CONVERSION_RATE);
        }

        BigDecimal amountAfterDiscount = amountAfterTierDiscount.subtract(requestedPointDiscount);
        if (amountAfterDiscount.compareTo(BigDecimal.ZERO) < 0) {
            amountAfterDiscount = BigDecimal.ZERO;
        }

        // 3. Ap dung thue VAT len so tien cuoi cung
        BigDecimal vat = amountAfterDiscount.multiply(new BigDecimal("0.10"));
        BigDecimal finalTotal = amountAfterDiscount.add(vat);

        this.discountAmount = tierDiscountAmount.add(requestedPointDiscount);

        currentOrder.setTierDiscountAmount(tierDiscountAmount);
        currentOrder.setPointDiscountAmount(requestedPointDiscount);
        currentOrder.setTotalAmount(finalTotal);
    }

    private void validateCartItem(int productId, String productName, BigDecimal price, int quantity) {
        if (productId <= 0) {
            throw new IllegalArgumentException("Product ID khong hop le!");
        }

        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("Ten san pham khong duoc de trong!");
        }

        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Gia san pham phai lon hon 0!");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("So luong san pham phai lon hon 0!");
        }
    }

    private Map<Integer, Integer> normalizeToppingQuantities(Map<Integer, Integer> toppingQuantities) {
        Map<Integer, Integer> normalized = new HashMap<>();

        if (toppingQuantities == null || toppingQuantities.isEmpty()) {
            return normalized;
        }

        for (Map.Entry<Integer, Integer> entry : toppingQuantities.entrySet()) {
            Integer toppingId = entry.getKey();
            Integer quantity = entry.getValue();

            if (toppingId == null || toppingId <= 0) {
                throw new IllegalArgumentException("Topping ID khong hop le!");
            }

            if (quantity == null || quantity <= 0) {
                throw new IllegalArgumentException("So luong topping phai lon hon 0!");
            }

            normalized.put(toppingId, quantity);
        }

        return normalized;
    }

    private BigDecimal calculateToppingPrice(Map<Integer, Integer> toppingQuantities) {
        if (toppingQuantities == null || toppingQuantities.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<Integer, Integer> entry : toppingQuantities.entrySet()) {
            int toppingId = entry.getKey();
            int quantity = entry.getValue();

            Topping topping = findActiveToppingById(this.cachedActiveToppings, toppingId);

            if (topping == null) {
                throw new IllegalArgumentException("Topping khong ton tai hoac da ngung ban: " + toppingId);
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
}