package com.vtea.service;

import com.vtea.dto.OrderDetailDTO;
import com.vtea.model.Order;
import com.vtea.model.OrderDetail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OrderService {

    private Order currentOrder;
    // Dùng DTO cho giỏ hàng tạm thời để giữ được productName hiển thị lên UI
    private List<OrderDetailDTO> cartItems;

    public OrderService() {
        this.currentOrder = new Order();
        this.cartItems = new ArrayList<>();
    }

    public void addToCart(int productId, String productName, BigDecimal price, int quantity) {
        boolean itemExists = false;

        // Xem món đã có trong giỏ (DTO) chưa
        for (OrderDetailDTO item : cartItems) {
            if (item.getProductId() == productId) {
                item.setQuantity(item.getQuantity() + quantity);
                itemExists = true;
                break;
            }
        }

        // Chưa có -> Tạo DTO mới và nhét vào giỏ
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

    // Hàm này FE sẽ gọi để đổ dữ liệu lên bảng (Table/ListView) trên màn hình POS
    public List<OrderDetailDTO> getCartItems() {
        return cartItems;
    }

    public Order getCurrentOrder() {
        return currentOrder;
    }

    // Dùng khi bấm thanh toán -> lưu db.
    // Nó sẽ biến đổi DTO (có tên) thành Model chuẩn (chỉ có ID)
    public List<OrderDetail> getDetailsForCheckout(int savedOrderId) {
        List<OrderDetail> detailsForDB = new ArrayList<>();
        for (OrderDetailDTO dto : cartItems) {
            // Ko có productname
            OrderDetail detail = new OrderDetail(savedOrderId, dto.getProductId(), dto.getQuantity(), dto.getUnitPrice());
            detailsForDB.add(detail);
        }
        return detailsForDB;
    }
}