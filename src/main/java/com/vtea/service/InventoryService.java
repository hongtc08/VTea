package com.vtea.service;

import com.vtea.dao.IngredientDAO;
import com.vtea.dao.InventoryCheckDAO;
import com.vtea.dao.InventoryTransactionDAO;
import com.vtea.dto.InventoryCheckDTO;
import com.vtea.dto.InventoryTransactionDTO;
import com.vtea.model.InventoryCheck;
import com.vtea.model.InventoryTransaction;
import com.vtea.utils.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class InventoryService {

    private final InventoryCheckDAO checkDAO = new InventoryCheckDAO();
    private final InventoryTransactionDAO transactionDAO = new InventoryTransactionDAO();
    private final IngredientDAO ingredientDAO = new IngredientDAO(); // Bổ sung DAO gốc

    // ==================== NGHIỆP VỤ CỦA STAFF ====================

    public boolean submitCount(int staffId, int ingredientId, BigDecimal systemQty, BigDecimal actualQty) throws Exception {
        if (actualQty.compareTo(BigDecimal.ZERO) < 0) {
            throw new Exception("Số lượng thực tế không được là số âm!");
        }

        BigDecimal difference = actualQty.subtract(systemQty);

        if (difference.compareTo(BigDecimal.ZERO) == 0) {
            return true;
        }

        InventoryCheck check = new InventoryCheck();
        check.setIngredientId(ingredientId);
        check.setStaffId(staffId);
        check.setSystemQty(systemQty);
        check.setActualQty(actualQty);
        check.setDifference(difference);

        return checkDAO.insertPendingCheck(check);
    }

    // ==================== NGHIỆP VỤ CỦA ADMIN ====================

    public List<InventoryCheckDTO> getAllPendingChecks() {
        return checkDAO.getPendingChecks();
    }

    public List<InventoryTransactionDTO> getTransactionHistory() {
        return transactionDAO.getAllTransactions();
    }

    public boolean rejectCheck(int logId, int adminId) throws Exception {
        InventoryCheck check = checkDAO.getCheckById(logId);
        if (check == null || !"PENDING".equals(check.getStatus())) {
            throw new Exception("Phiếu kiểm kê không tồn tại hoặc đã được xử lý!");
        }

        try (Connection conn = DBConnection.getConnection()) {
            return checkDAO.updateCheckStatus(logId, "REJECTED", adminId, conn);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception("Lỗi hệ thống khi từ chối phiếu.");
        }
    }

    public boolean approveCheck(int logId, int adminId, String note) throws Exception {
        InventoryCheck check = checkDAO.getCheckById(logId);
        if (check == null || !"PENDING".equals(check.getStatus())) {
            throw new Exception("Phiếu kiểm kê không tồn tại hoặc đã được xử lý!");
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Cập nhật trạng thái Phiếu nháp thành APPROVED
            boolean isCheckUpdated = checkDAO.updateCheckStatus(logId, "APPROVED", adminId, conn);
            if (!isCheckUpdated) throw new SQLException("Không thể cập nhật trạng thái phiếu nháp.");

            // 2. Cập nhật số lượng gốc trong bảng ingredient (Gọi qua DAO)
            if (!ingredientDAO.updateStockQuantity(check.getIngredientId(), check.getDifference(), conn)) {
                throw new SQLException("Không thể cập nhật số lượng kho gốc.");
            }

            // 3. Ghi vết vào inventory_transaction
            InventoryTransaction tx = new InventoryTransaction();
            tx.setIngredientId(check.getIngredientId());
            tx.setAdminId(adminId);
            tx.setChangeType("ADJUSTMENT");
            tx.setQuantityChanged(check.getDifference());
            tx.setNote(note != null && !note.isEmpty() ? note : "Duyệt chênh lệch kiểm kho");

            boolean isTxInserted = transactionDAO.insertTransaction(tx, conn);
            if (!isTxInserted) throw new SQLException("Không thể ghi sổ lịch sử giao dịch.");

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            throw new Exception("Lỗi giao dịch CSDL: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // ==================== NGHIỆP VỤ NHẬP / XUẤT TRỰC TIẾP CỦA ADMIN ====================

    public boolean importStock(int adminId, int ingredientId, BigDecimal quantity, String note) throws Exception {
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("Số lượng nhập kho phải lớn hơn 0!");
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Tăng số lượng trong bảng nguyên liệu gốc (Gọi qua DAO)
            if (!ingredientDAO.updateStockQuantity(ingredientId, quantity, conn)) {
                throw new SQLException("Không thể cập nhật số lượng kho gốc.");
            }

            // 2. Ghi sổ inventory_transaction
            InventoryTransaction tx = new InventoryTransaction();
            tx.setIngredientId(ingredientId);
            tx.setAdminId(adminId);
            tx.setChangeType("IMPORT");
            tx.setQuantityChanged(quantity);
            tx.setNote(note != null && !note.isEmpty() ? note : "Admin nhập hàng vào kho");

            if (!transactionDAO.insertTransaction(tx, conn)) {
                throw new SQLException("Không thể ghi sổ lịch sử giao dịch.");
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw new Exception("Lỗi hệ thống khi nhập kho: " + e.getMessage());
        } finally {
            if (conn != null) { conn.setAutoCommit(true); conn.close(); }
        }
    }

    public boolean exportStock(int adminId, int ingredientId, BigDecimal quantity, String note) throws Exception {
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("Số lượng xuất kho phải lớn hơn 0!");
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Trừ số lượng trong bảng nguyên liệu gốc (Truyền số âm bằng cách dùng .negate())
            if (!ingredientDAO.updateStockQuantity(ingredientId, quantity.negate(), conn)) {
                throw new SQLException("Không thể cập nhật số lượng kho gốc.");
            }

            // 2. Ghi sổ inventory_transaction
            InventoryTransaction tx = new InventoryTransaction();
            tx.setIngredientId(ingredientId);
            tx.setAdminId(adminId);
            tx.setChangeType("EXPORT");
            tx.setQuantityChanged(quantity.negate());
            tx.setNote(note != null && !note.isEmpty() ? note : "Admin xuất hàng khỏi kho");

            if (!transactionDAO.insertTransaction(tx, conn)) {
                throw new SQLException("Không thể ghi sổ lịch sử giao dịch.");
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw new Exception("Lỗi hệ thống khi xuất kho: " + e.getMessage());
        } finally {
            if (conn != null) { conn.setAutoCommit(true); conn.close(); }
        }
    }
}