package com.vtea.service;

import com.vtea.dao.IngredientDAO;
import com.vtea.model.Ingredient;

import java.math.BigDecimal;
import java.util.List;

public class InventoryService {

    private final IngredientDAO ingredientDAO = new IngredientDAO();

    public List<Ingredient> getAllIngredients() {
        return ingredientDAO.getAllActiveIngredients();
    }

    public boolean createIngredient(String name, String unit, BigDecimal initialQty) {
        if (name == null || name.isBlank()) {
            return false;
        }
        Ingredient item = new Ingredient();
        item.setName(name.trim());
        item.setUnit(unit != null && !unit.isBlank() ? unit.trim() : "kg");
        item.setAvailable(true);

        if (!ingredientDAO.insertIngredient(item)) {
            return false;
        }

        if (initialQty != null && initialQty.compareTo(BigDecimal.ZERO) > 0) {
            List<Ingredient> list = ingredientDAO.getAllActiveIngredients();
            for (Ingredient ing : list) {
                if (ing.getName().equalsIgnoreCase(name.trim())) {
                    return ingredientDAO.updateActualQuantity(ing.getIngredientId(), initialQty);
                }
            }
        }
        return true;
    }

    public boolean updateIngredientInfo(Ingredient ingredient) {
        return ingredientDAO.updateIngredient(ingredient);
    }

    public boolean updateStockQuantity(int ingredientId, BigDecimal quantity) {
        return ingredientDAO.updateActualQuantity(ingredientId, quantity);
    }

    public boolean deleteIngredient(int ingredientId) {
        return ingredientDAO.deleteIngredient(ingredientId);
    }
}
