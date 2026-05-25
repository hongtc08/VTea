package com.vtea.service;

import com.vtea.dao.OrderDAO;
import com.vtea.dto.OrderDTO;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DashboardService {

    private final OrderDAO orderDAO = new OrderDAO();

    public BigDecimal getTodayRevenue() {
        Date[] range = todayRange();
        return BigDecimal.valueOf(orderDAO.getRevenue(range[0], range[1]));
    }

    public int getTodayOrderCount() {
        return orderDAO.countPaidOrdersToday();
    }

    public int getTodayCustomerCount() {
        return orderDAO.countDistinctCustomersToday();
    }

    public List<OrderDTO> getRecentOrdersToday(int maxCount) {
        Date[] range = todayRange();
        List<OrderDTO> orders = orderDAO.getOrderHistory(range[0], range[1]);
        if (orders.size() <= maxCount) {
            return orders;
        }
        return orders.subList(0, maxCount);
    }

    public List<Object[]> getTopProductsToday(int limit) {
        return orderDAO.getTopSellingProductsToday(limit);
    }

    private Date[] todayRange() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date start = cal.getTime();

        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        Date end = cal.getTime();
        return new Date[]{start, end};
    }
}
