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
import java.sql.Connection;
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
    private final CustomerDAO customerDAO;
    private List<Topping> cachedActiveToppings;

    // Thông tin liên quan đến khách hàng và điểm thưởng
    private int currentCustomerId = 0; // 0 nghĩa là Khách vãng lai (Không có thẻ)
    private int pointsToUse = 0;       // Số điểm khách muốn dùng để trừ tiền
    private int lastEarnedPoints = 0;  // Số điểm đã cộng ở lần checkout gần nhất.
    private BigDecimal discountAmount = BigDecimal.ZERO; // Tiền được giảm
    // Hằng số quy đổi điểm thưởng -> tiền: 1 điểm thưởng tương ứng với 1000 VND.
    public static final BigDecimal POINT_CONVERSION_RATE = new BigDecimal("1000");

    public OrderService() {
        this.currentOrder = new Order();
        this.cartItems = new ArrayList<>();
        this.orderDAO = new OrderDAO();
        this.toppingDAO = new ToppingDAO();
        this.customerDAO = new CustomerDAO();
        this.cachedActiveToppings = toppingDAO.getAllActiveToppings();
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

        // Mỗi lần thêm món tạo một dòng riêng để topping không bị dính sang ly khác.
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
        currentCustomerId = 0;
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
    public void changeToppingQuantity(OrderDetailDTO item, int toppingId, int delta) {
        if (item == null || !cartItems.contains(item)) {
            return;
        }

        Map<Integer, Integer> map = item.getToppingQuantities();

        if (map == null) {
            map = new HashMap<>();
        } else {
            map = new HashMap<>(map);
        }

        int prev = map.getOrDefault(toppingId, 0);
        int now = prev + delta;

        if (now <= 0) {
            map.remove(toppingId);
        } else {
            map.put(toppingId, now);
        }

        item.setToppingQuantities(map);
        item.setToppingPrice(calculateToppingPrice(map));
        calculateTotal();
    }

    public void removeToppingFromItem(OrderDetailDTO item, int toppingId) {
        if (item == null || !cartItems.contains(item)) {
            return;
        }

        Map<Integer, Integer> map = item.getToppingQuantities();

        if (map == null || !map.containsKey(toppingId)) {
            return;
        }

        map = new HashMap<>(map);
        map.remove(toppingId);

        item.setToppingQuantities(map);
        item.setToppingPrice(calculateToppingPrice(map));
        calculateTotal();
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
        lastEarnedPoints = 0;

        if (currentOrder.getCustomerId() != null && currentOrder.getCustomerId() > 0) {
            currentCustomerId = currentOrder.getCustomerId();
        } else {
            currentOrder.setCustomerId(null);
            currentCustomerId = 0;
        }

        List<OrderDetail> details = getDetailsForCheckout(0);

        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            if (conn == null) {
                throw new java.sql.SQLException("Khong the ket noi database.");
            }
            conn.setAutoCommit(false);

            int savedOrderId = orderDAO.checkoutOrder(conn, currentOrder, details);

            if (savedOrderId <= 0) {
                throw new Exception("Lỗi hệ thống: Không thể tạo hóa đơn!");
            }

            // Lưu lại order_id vừa tạo để POSController mở bill preview.
            currentOrder.setOrderId(savedOrderId);

            if (currentCustomerId > 0) {
                if (pointsToUse > 0) {
                    boolean deducted = customerDAO.deductRewardPoints(conn, currentCustomerId, pointsToUse);
                    if (!deducted) {
                        throw new java.sql.SQLException("Khach hang khong du diem de su dung.");
                    }
                }

                int pointsEarned = currentOrder.getTotalAmount()
                        .divide(new BigDecimal("10000"), java.math.RoundingMode.DOWN)
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

            // Không clearCart ở đây vì POSController còn cần order_id để mở bill preview.
            currentCustomerId = 0;
            pointsToUse = 0;

            return true;

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println(">>> TRANSACTION FAILED: Đã Rollback giao dịch an toàn!");
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

        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<Integer, Integer> entry : toppingQuantities.entrySet()) {
            int toppingId = entry.getKey();
            int quantity = entry.getValue();

            Topping topping = findActiveToppingById(this.cachedActiveToppings, toppingId);

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



    // ==================== CUSTOMER & POINTS METHODS ====================

    // Gọi hàm này khi thu ngân quét mã hoặc nhập xong SĐT khách
    public void setCustomer(int customerId) {
        if (customerId > 0) {
            this.currentCustomerId = customerId;
            this.currentOrder.setCustomerId(customerId);
        } else {
            this.currentCustomerId = 0;
            this.currentOrder.setCustomerId(null);
        }

        this.pointsToUse = 0; // Đổi khách thì reset điểm muốn dùng về 0
        calculateTotal();
    }

    // Gọi hàm này khi thu ngân gõ số điểm muốn xài vào ô Text
    public void applyRewardPoints(int points) throws Exception {
        if (currentCustomerId <= 0) {
            throw new Exception("Lỗi: Vui lòng chọn khách hàng thành viên trước!");
        }

        CustomerDTO customer = customerDAO.getCustomerById(currentCustomerId);
        if (customer == null || customer.getRewardPoints() < points) {
            throw new Exception("Lỗi: Khách hàng không đủ điểm!");
        }

        this.pointsToUse = points;
        calculateTotal(); // Tính lại tổng tiền ngay lập tức
    }

    // Lấy tiền giảm giá để UI hiển thị
    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public int getLastEarnedPoints() {
        return lastEarnedPoints;
    }

    // ==================== CALCULATE / VALIDATE METHODS ====================

    private void calculateTotal() {
        BigDecimal subtotal = BigDecimal.ZERO;

        for (OrderDetailDTO item: cartItems) {
            subtotal = subtotal.add(item.getSubTotal());
        }

        // 1. Tính số tiền giảm giá thông qua quy đổi điểm thưởng (Ví dụ 1 điểm = 1000đ)
        this.discountAmount = new BigDecimal(this.pointsToUse).multiply(POINT_CONVERSION_RATE);

        // 2. Ép bảo mật: Không được giảm lố tiền món nước
        if (this.discountAmount.compareTo(subtotal) > 0) {
            this.discountAmount = subtotal; // Ép tiền giảm = tiền gốc
            // Ép ngược lại số điểm thực tế bị trừ
            this.pointsToUse = subtotal.divide(POINT_CONVERSION_RATE, java.math.RoundingMode.DOWN).intValue();
        }

        // 3. Tính lại tiền gốc sau khi đã trừ giảm giá
        BigDecimal amountAfterDiscount = subtotal.subtract(this.discountAmount);

        // 4. Áp dụng thuế VAT lên số tiền sau khi giảm giá
        BigDecimal vat = amountAfterDiscount.multiply(new BigDecimal("0.10"));

        // 5. Tính tổng tiền cuối cùng = Tiền sau giảm giá + VAT
        BigDecimal finalTotal = amountAfterDiscount.add(vat);

        currentOrder.setPointDiscountAmount(this.discountAmount);
        currentOrder.setTierDiscountAmount(BigDecimal.ZERO);
        currentOrder.setTotalAmount(finalTotal);
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
