package com.vtea.model;

public class Category {
    private int categoryId;
    private String name;
    private String description;
    private boolean isAvailable;

    public Category() {

    }

    public Category(int categoryId, String description, String name, boolean isAvailable) {
        this.categoryId = categoryId;
        this.description = description;
        this.name = name;
        this.isAvailable = isAvailable;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public boolean getAvailable() { return isAvailable; }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAvailable(boolean available) { this.isAvailable = available;}
}
