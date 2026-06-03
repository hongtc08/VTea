package com.vtea.payment.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PaymentStore {

    private final Map<Long, String> paymentStatusMap = new ConcurrentHashMap<>();

    public void createPending(long orderCode) {
        paymentStatusMap.put(orderCode, "PENDING");
    }

    public void markPaid(long orderCode) {
        paymentStatusMap.put(orderCode, "PAID");
    }

    public void markCancelled(long orderCode) {
        paymentStatusMap.put(orderCode, "CANCELLED");
    }

    public String getStatus(long orderCode) {
        return paymentStatusMap.getOrDefault(orderCode, "NOT_FOUND");
    }
}