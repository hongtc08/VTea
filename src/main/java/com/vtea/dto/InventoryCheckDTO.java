package com.vtea.dto;

import com.vtea.model.InventoryCheck;

public class InventoryCheckDTO extends InventoryCheck {
    private String ingredientName;
    private String staffName;
    private String adminName;

    public String getIngredientName() { return ingredientName; }
    public void setIngredientName(String ingredientName) { this.ingredientName = ingredientName; }

    public String getStaffName() { return staffName; }
    public void setStaffName(String staffName) { this.staffName = staffName; }

    public String getAdminName() { return adminName; }
    public void setAdminName(String adminName) { this.adminName = adminName; }
}