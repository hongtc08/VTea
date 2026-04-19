package com.vtea.model;

public class Category {
    private int categoryId;
    private String name;
    private String description;

    public Category() {

    }

    public Category(int categoryId, String description, String name) {
        this.categoryId = categoryId;
        this.description = description;
        this.name = name;
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

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }
}
