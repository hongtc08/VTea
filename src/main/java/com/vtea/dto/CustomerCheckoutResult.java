package com.vtea.dto;

import java.math.BigDecimal;

public class CustomerCheckoutResult {

    private final boolean confirmed;
    private final Integer customerId;
    private final boolean usePoints;
    private final int pointsUsed;
    private final int pointsToEarn;
    private final BigDecimal finalTotal;

    private CustomerCheckoutResult(
            boolean confirmed,
            Integer customerId,
            boolean usePoints,
            int pointsUsed,
            int pointsToEarn,
            BigDecimal finalTotal
    ) {
        this.confirmed = confirmed;
        this.customerId = customerId;
        this.usePoints = usePoints;
        this.pointsUsed = pointsUsed;
        this.pointsToEarn = pointsToEarn;
        this.finalTotal = finalTotal;
    }

    public static CustomerCheckoutResult cancelled() {
        return new CustomerCheckoutResult(false, null, false, 0, 0, null);
    }

    public static CustomerCheckoutResult confirmed(
            Integer customerId,
            boolean usePoints,
            int pointsUsed,
            int pointsToEarn,
            BigDecimal finalTotal
    ) {
        return new CustomerCheckoutResult(true, customerId, usePoints, pointsUsed, pointsToEarn, finalTotal);
    }

    public static CustomerCheckoutResult walkIn(BigDecimal finalTotal) {
        return new CustomerCheckoutResult(true, null, false, 0, 0, finalTotal);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public boolean isUsePoints() {
        return usePoints;
    }

    public int getPointsUsed() {
        return pointsUsed;
    }

    public int getPointsToEarn() {
        return pointsToEarn;
    }

    public BigDecimal getFinalTotal() {
        return finalTotal;
    }
}
