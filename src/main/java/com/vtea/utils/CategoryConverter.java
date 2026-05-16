package com.vtea.utils;

import com.vtea.dto.CategoryDTO;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;

public class CategoryConverter extends StringConverter<CategoryDTO> {

    private final ComboBox<CategoryDTO> comboBox;

    // Truyền ComboBox vào để lấy danh sách items
    public CategoryConverter(ComboBox<CategoryDTO> comboBox) {
        this.comboBox = comboBox;
    }

    @Override
    public String toString(CategoryDTO object) {
        if (object == null) return null;
        return object.getName();
    }

    @Override
    public CategoryDTO fromString(String string) {
        return comboBox.getItems().stream()
                .filter(c -> c.getName().equals(string))
                .findFirst()
                .orElse(null);
    }
}