package com.vtea.service;

import com.vtea.dao.DashboardDAO;
import com.vtea.dto.DashboardSummaryDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class DashboardService {
    private final DashboardDAO dashboardDAO = new DashboardDAO();

    // Lấy thống kê tổng quan trong hôm nay
    public DashboardSummaryDTO getTodaySummary() {
        LocalDate today = LocalDate.now();

        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        return dashboardDAO.getDashBoardSummary(startOfDay, endOfDay);
    }
}