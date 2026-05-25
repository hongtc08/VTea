package com.vtea.service;

import com.vtea.dao.CustomerDAO;
import com.vtea.model.Customer;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CustomerService {

    public static final int POINT_VALUE = 1000;
    public static final BigDecimal VAT_RATE = new BigDecimal("0.10");

    private final CustomerDAO customerDAO = new CustomerDAO();

    public Customer findByPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }
        return customerDAO.getCustomerByPhone(phone.trim());
    }

    public int registerCustomer(String fullName, String phone) {
        Customer customer = new Customer();
        customer.setFullName(fullName.trim());
        customer.setPhoneNumber(phone.trim());
        return customerDAO.insertCustomer(customer);
    }

    public int calculateEarnablePoints(BigDecimal subtotal) {
        if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        return subtotal.divide(BigDecimal.valueOf(POINT_VALUE), RoundingMode.FLOOR).intValue();
    }

    public int calculateMaxUsablePoints(int customerPoints, BigDecimal subtotal) {
        return Math.min(Math.max(customerPoints, 0), calculateEarnablePoints(subtotal));
    }

    public BigDecimal calculateDiscountAmount(int pointsUsed) {
        return BigDecimal.valueOf((long) pointsUsed * POINT_VALUE);
    }

    public BigDecimal calculateVat(BigDecimal subtotalAfterDiscount) {
        return subtotalAfterDiscount.multiply(VAT_RATE);
    }

    public BigDecimal calculateTotal(BigDecimal subtotal, int pointsUsed) {
        BigDecimal discountedSubtotal = subtotal.subtract(calculateDiscountAmount(pointsUsed)).max(BigDecimal.ZERO);
        return discountedSubtotal.add(calculateVat(discountedSubtotal));
    }

    public boolean updateRewardPoints(int customerId, int pointsDelta) {
        if (customerId <= 0 || pointsDelta == 0) {
            return true;
        }
        return customerDAO.updateRewardPoints(customerId, pointsDelta);
    }

    public String formatMoney(BigDecimal amount) {
        if (amount == null) {
            return "0đ";
        }
        return String.format("%,.0fđ", amount);
    }

    public String formatPointsValue(int points) {
        return formatMoney(BigDecimal.valueOf((long) points * POINT_VALUE));
    }
}
