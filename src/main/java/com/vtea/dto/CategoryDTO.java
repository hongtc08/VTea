package com.vtea.dto;

public class CategoryDTO {
    private int categoryId;
    private String name;
    private String description;
    private Boolean isAvailable;

    public CategoryDTO() {

    }

    public CategoryDTO(int categoryId, String name, String description, Boolean isAvailable) {
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.isAvailable = isAvailable;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public String getDescription() {
        return description;
    }

    public Boolean getAvailable() {
        return isAvailable;
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

    public void setAvailable(Boolean available) {
        isAvailable = available;
    }

    public void setName(String name) {
        this.name = name;
    }
}
