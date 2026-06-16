package com.vtea.service;

import com.vtea.dao.BillDAO;
import com.vtea.dto.BillDTO;
import com.vtea.dto.OrderHistoryDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * Xử lý logic hóa đơn
 */
public class BillService {
    private final BillDAO billDAO;

    public BillService() {
        this.billDAO = new BillDAO();
    }

    /**
     * Lấy chi tiết hóa đơn theo order_id
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
     */
    public List<OrderHistoryDTO> getOrderHistory() {
        return billDAO.getOrderHistory();
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