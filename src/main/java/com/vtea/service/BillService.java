package com.vtea.service;

import com.vtea.dao.BillDAO;
import com.vtea.dto.BillDTO;
import com.vtea.dto.OrderHistoryDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * Service xử lý nghiệp vụ liên quan đến hóa đơn.
 * Controller chỉ nên gọi service này, không gọi trực tiếp BillDAO.
 */
public class BillService {
    private final BillDAO billDAO;

    public BillService() {
        this.billDAO = new BillDAO();
    }

    /**
     * Lấy chi tiết đầy đủ của một hóa đơn theo order_id.
     * Dùng cho bill preview, xem chi tiết hóa đơn và xuất PDF.
     */
    public BillDTO getBillByOrderId(int orderId) {
        if (orderId <= 0) {
            throw new IllegalArgumentException("Mã hóa đơn không hợp lệ");
        }

        BillDTO bill = billDAO.getBillByOrderId(orderId);

        if (bill == null) {
            throw new IllegalArgumentException("Không tìm thấy hóa đơn có mã: " + orderId);
        }

        return bill;
    }

    /**
     * Lấy toàn bộ lịch sử hóa đơn.
     * Dùng khi mở màn hình lịch sử hóa đơn lần đầu.
     */
    public List<OrderHistoryDTO> getOrderHistory() {
        return billDAO.getOrderHistory();
    }

    /**
     * Lọc lịch sử hóa đơn theo khoảng ngày.
     * Dùng cho bộ lọc ngày/tháng trên màn hình lịch sử.
     */
    public List<OrderHistoryDTO> getOrderHistoryByDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null) {
            throw new IllegalArgumentException("Ngày bắt đầu và ngày kết thúc không được để trống");
        }

        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("Ngày bắt đầu không được lớn hơn ngày kết thúc");
        }

        return billDAO.getOrderHistoryByDateRange(fromDate, toDate);
    }

    /**
     * Tìm kiếm hóa đơn theo mã hóa đơn, tên khách hàng, số điện thoại hoặc tên nhân viên.
     */
    public List<OrderHistoryDTO> searchOrderHistory(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getOrderHistory();
        }

        return billDAO.searchOrderHistory(keyword.trim());
    }
}