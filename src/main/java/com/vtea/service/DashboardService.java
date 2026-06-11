package com.vtea.service;

import com.vtea.dao.DashboardDAO;
import com.vtea.dto.DashboardSummaryDTO;
import com.vtea.dto.IngredientDTO;
import com.vtea.dto.ProductSalesDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

public class DashboardService {
    private final DashboardDAO dashboardDAO = new DashboardDAO();

    // Lấy thống kê tổng quan trong hôm nay
    public DashboardSummaryDTO getTodaySummary() {
        LocalDate today = LocalDate.now();

        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        return dashboardDAO.getDashBoardSummary(startOfDay, endOfDay);
    }

    // Lấy danh sách đơn hàng gần đây trên dashboard
    public List<Map<String, Object>> getRecentOrders(int limit) {
        return dashboardDAO.getRecentOrders(limit);
    }

    // Lấy top món bán chạy trên dashboard
    public List<ProductSalesDTO> getTopProductsForDashboard(int limit) {
        return dashboardDAO.getTopProductsForDashboard(limit);
    }

    // Lấy top món bán chạy trong hôm nay
    public List<ProductSalesDTO> getTodayTopSellingProducts() {
        LocalDate today = LocalDate.now();
        return dashboardDAO.getTopSellingProducts(today, today);
    }

    // Lấy danh sách nguyên liệu sắp hết
    public List<IngredientDTO> getLowStockIngredients(int limit) {
        return dashboardDAO.getLowStockIngredients(limit);
    }

    // Đếm số hóa đơn đã thanh toán trong hôm nay
    public int getTodayOrderCount() {
        return dashboardDAO.countPaidOrdersByDate(LocalDate.now());
    }

    // Đếm số hóa đơn đã thanh toán theo ngày được chọn
    public int getOrderCountByDate(LocalDate date) {
        return dashboardDAO.countPaidOrdersByDate(date);
    }

    // Đếm số hóa đơn đã thanh toán trong tháng hiện tại
    public int getCurrentMonthOrderCount() {
        YearMonth currentMonth = YearMonth.now();

        return dashboardDAO.countPaidOrdersByMonth(
                currentMonth.getYear(),
                currentMonth.getMonthValue()
        );
    }

    // Đếm số hóa đơn đã thanh toán theo tháng được chọn
    public int getOrderCountByMonth(YearMonth yearMonth) {
        return dashboardDAO.countPaidOrdersByMonth(
                yearMonth.getYear(),
                yearMonth.getMonthValue()
        );
    }
}