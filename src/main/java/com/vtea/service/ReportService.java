package com.vtea.service;

import com.vtea.dao.ReportDAO;
import com.vtea.dto.CategoryRevenueDTO;
import com.vtea.dto.ProductSalesDTO;
import com.vtea.dto.StaffSalesDTO;
import com.vtea.dto.TimeRevenueDTO;

import java.time.LocalDate;
import java.util.List;

public class ReportService {

    private final ReportDAO reportDAO;

    public ReportService() {
        this.reportDAO = new ReportDAO();
    }

    public List<TimeRevenueDTO> getRevenueByDate(LocalDate start, LocalDate end) {
        return reportDAO.getRevenueByDate(start, end);
    }

    public List<CategoryRevenueDTO> getRevenueByCategory(LocalDate start, LocalDate end) {
        return reportDAO.getRevenueByCategory(start, end);
    }

    public List<ProductSalesDTO> getTopSellingProducts(LocalDate start, LocalDate end) {
        return reportDAO.getTopSellingProducts(start, end);
    }

    public List<StaffSalesDTO> getRevenueByStaff(LocalDate start, LocalDate end) {
        return reportDAO.getRevenueByStaff(start, end);
    }
}