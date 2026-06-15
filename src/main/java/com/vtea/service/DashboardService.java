package com.vtea.service;

import com.vtea.dao.DashboardDAO;
import com.vtea.dto.DashboardSummaryDTO;
import com.vtea.dto.ProductSalesDTO;
import com.vtea.dto.IngredientDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public class DashboardService {
    private final DashboardDAO dashboardDAO = new DashboardDAO();

    // Lấy thống kê tổng quan trong hôm nay
    public DashboardSummaryDTO getTodaySummary() {
        LocalDate today = LocalDate.now();

        // Lấy dữ liệu từ đầu tháng đến hiện tại thay vì chỉ hôm nay để dashboard hiển thị đẹp hơn
        LocalDateTime startOfMonth = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        return dashboardDAO.getDashBoardSummary(startOfMonth, endOfDay);
    }

    /**
     * Lấy danh sách đơn hàng gần đây nhất
     * @param limit Số lượng đơn hàng cần lấy
     */
    public List<Map<String, Object>> getRecentOrders(int limit) {

        return dashboardDAO.getRecentOrders(limit);
    }

     // Lấy danh sách sản phẩm bán chạy
    public List<ProductSalesDTO> getTopProductsForDashboard(int limit) {
        return dashboardDAO.getTopProductsForDashboard(limit);
    }

    /**
     * Lấy danh sách nguyên liệu sắp hết tồn kho
     */
    public List<IngredientDTO> getLowStockIngredients(int limit) {
        return dashboardDAO.getLowStockIngredients(limit);
    }
}