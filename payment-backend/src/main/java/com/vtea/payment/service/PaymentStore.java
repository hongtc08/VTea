package com.vtea.payment.service;

/**
 * Lưu tạm trạng thái thanh toán theo orderCode.
 */
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PaymentStore {

    private final Map<Long, String> paymentStatusMap = new ConcurrentHashMap<>();


    /**
     * Tạo trạng thái chờ thanh toán cho giao dịch mới.
     */
    public void createPending(long orderCode) {
        paymentStatusMap.put(orderCode, "PENDING");
    }

    /**
     * Đánh dấu giao dịch đã thanh toán thành công.
     */
    public void markPaid(long orderCode) {
        paymentStatusMap.put(orderCode, "PAID");
    }


    /**
     * Đánh dấu giao dịch đã bị hủy.
     */
    public void markCancelled(long orderCode) {
        paymentStatusMap.put(orderCode, "CANCELLED");
    }

    /**
     * Lấy trạng thái hiện tại của giao dịch.
     */
    public String getStatus(long orderCode) {
        return paymentStatusMap.getOrDefault(orderCode, "NOT_FOUND");
    }
}