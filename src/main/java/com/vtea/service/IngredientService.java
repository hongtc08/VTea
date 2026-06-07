package com.vtea.service;

import com.vtea.dao.IngredientDAO;
import com.vtea.dto.IngredientDTO;
import com.vtea.model.Ingredient;

import java.math.BigDecimal;
import java.util.List;

public class IngredientService {

    private final IngredientDAO ingredientDAO;

    public IngredientService() {
        this.ingredientDAO = new IngredientDAO();
    }

    /**
     * Lay danh sach nguyen lieu dang su dung (is_available = true).
     * Phu hop cho man hinh kiem kho hoac ban hang.
     */
    public List<Ingredient> getAllActiveIngredients() {
        return ingredientDAO.getAllActiveIngredients();
    }

    /**
     * Lay toan bo danh sach nguyen lieu ke ca da an (kem ten nhan vien cap nhat).
     * Phu hop cho man hinh quan ly cua Admin.
     */
    public List<IngredientDTO> getAllIngredientsForAdmin() {
        return ingredientDAO.getAllIngredientsForAdmin();
    }

    /**
     * Them nguyen lieu moi vao he thong.
     */
    public boolean addIngredient(Ingredient item, int adminId) {
        // Kiem tra tinh hop le cua du lieu truoc khi xuong DB
        validateIngredient(item);
        return ingredientDAO.insertIngredient(item, adminId);
    }

    /**
     * Cap nhat thong tin co ban cua nguyen lieu (Ten, don vi tinh, muc toi thieu).
     */
    public boolean updateIngredientInfo(Ingredient item, int adminId) {
        validateIngredient(item);
        if (item.getIngredientId() <= 0) {
            throw new IllegalArgumentException("ID nguyen lieu khong hop le de cap nhat.");
        }
        return ingredientDAO.updateIngredientInfo(item, adminId);
    }

    /**
     * Xoa mem / An nguyen lieu khoi he thong ban hang.
     */
    public boolean deleteIngredient(int ingredientId, int adminId) {
        if (ingredientId <= 0) {
            throw new IllegalArgumentException("ID nguyen lieu khong hop le de xoa.");
        }
        return ingredientDAO.deleteIngredient(ingredientId, adminId);
    }

    /**
     * Cap nhat so luong ton kho thuc te do nhan vien kiem kho cuoi ngay.
     */
    public boolean updateActualQuantity(int ingredientId, BigDecimal quantity, int userId) {
        if (ingredientId <= 0) {
            throw new IllegalArgumentException("ID nguyen lieu khong hop le.");
        }
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("So luong ton kho thuc te khong duoc am.");
        }
        return ingredientDAO.updateActualQuantity(ingredientId, quantity, userId);
    }

    /**
     * Lay danh sach cac nguyen lieu sap het de hien thi thong bao.
     */
    public List<Ingredient> getLowStockAlerts() {
        return ingredientDAO.getLowStockAlerts();
    }

    // ==================== VALIDATION HELPER ====================

    /**
     * Ham kiem tra du lieu dau vao de ngan chan du lieu rac lam loi Database.
     */
    private void validateIngredient(Ingredient item) {
        if (item == null) {
            throw new IllegalArgumentException("Du lieu nguyen lieu khong duoc de trong.");
        }
        if (item.getName() == null || item.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Ten nguyen lieu khong duoc de trong.");
        }
        if (item.getUnit() == null || item.getUnit().trim().isEmpty()) {
            throw new IllegalArgumentException("Don vi tinh khong duoc de trong.");
        }
        if (item.getStockQty() == null || item.getStockQty().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("So luong ton kho khong duoc am.");
        }
        if (item.getMinStock() == null || item.getMinStock().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Muc ton kho toi thieu khong duoc am.");
        }
    }
}