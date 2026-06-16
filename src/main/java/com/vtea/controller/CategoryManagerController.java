package com.vtea.controller;

import com.vtea.dto.CategoryDTO;
import com.vtea.service.CategoryService;
import com.vtea.utils.DialogHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;

public class CategoryManagerController {

    @FXML private TableView<CategoryDTO> tvCategories;
    @FXML private TableColumn<CategoryDTO, String> colId;
    @FXML private TableColumn<CategoryDTO, String> colName;
    @FXML private TableColumn<CategoryDTO, String> colStatus;
    @FXML private TableColumn<CategoryDTO, Void> colAction;

    @FXML private Label lblFormTitle;
    @FXML private FontIcon iconFormMode;
    @FXML private TextField txtName;
    @FXML private CheckBox chkAvailable;
    @FXML private Button btnSave;

    private final CategoryService categoryService = new CategoryService();
    private ObservableList<CategoryDTO> categoryList;
    private CategoryDTO editingCategory = null;

    @FXML
    public void initialize() {
        setupTableColumns();
        loadCategories();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getCategoryId())));
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colStatus.setCellValueFactory(data -> {
            boolean isAvail = data.getValue().getAvailable() != null && data.getValue().getAvailable();
            return new SimpleStringProperty(isAvail ? "Đang bán" : "Ngưng bán");
        });

        colAction.setCellFactory(param -> new TableCell<CategoryDTO, Void>() {
            private final Button btnEdit = new Button();
            private final Button btnDelete = new Button();
            private final HBox pane = new HBox(8, btnEdit, btnDelete);

            {
                FontIcon editIcon = new FontIcon("fth-edit-2");
                editIcon.setIconSize(16);
                editIcon.setIconColor(javafx.scene.paint.Color.web("#12b6a2"));
                btnEdit.setGraphic(editIcon);
                btnEdit.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 4; -fx-background-radius: 4;");
                btnEdit.setOnMouseEntered(e -> btnEdit.setStyle("-fx-background-color: #e6fcf9; -fx-cursor: hand; -fx-padding: 4; -fx-background-radius: 4;"));
                btnEdit.setOnMouseExited(e -> btnEdit.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 4; -fx-background-radius: 4;"));
                
                FontIcon deleteIcon = new FontIcon("fth-trash-2");
                deleteIcon.setIconSize(16);
                deleteIcon.setIconColor(javafx.scene.paint.Color.web("#ef4444"));
                btnDelete.setGraphic(deleteIcon);
                btnDelete.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 4; -fx-background-radius: 4;");
                btnDelete.setOnMouseEntered(e -> btnDelete.setStyle("-fx-background-color: #fef2f2; -fx-cursor: hand; -fx-padding: 4; -fx-background-radius: 4;"));
                btnDelete.setOnMouseExited(e -> btnDelete.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 4; -fx-background-radius: 4;"));

                pane.setStyle("-fx-alignment: CENTER;");

                btnEdit.setOnAction(event -> {
                    CategoryDTO dto = getTableView().getItems().get(getIndex());
                    handleEditCategory(dto);
                });

                btnDelete.setOnAction(event -> {
                    CategoryDTO dto = getTableView().getItems().get(getIndex());
                    handleDeleteCategory(dto);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadCategories() {
        try {
            List<CategoryDTO> list = categoryService.getAllCategories();
            categoryList = FXCollections.observableArrayList(list);
            tvCategories.setItems(categoryList);
        } catch (Exception e) {
            e.printStackTrace();
            DialogHelper.showInfo("Lỗi", "Không thể tải danh sách danh mục.");
        }
    }

    private void handleEditCategory(CategoryDTO dto) {
        editingCategory = dto;
        lblFormTitle.setText("Sửa danh mục: " + dto.getName());
        if (iconFormMode != null) {
            iconFormMode.setIconLiteral("fth-edit-2");
            iconFormMode.setIconColor(javafx.scene.paint.Color.web("#eab308"));
        }
        txtName.setText(dto.getName());
        chkAvailable.setSelected(dto.getAvailable() != null && dto.getAvailable());
        btnSave.setText("Cập nhật");
    }

    @FXML
    private void handleResetForm(ActionEvent event) {
        editingCategory = null;
        lblFormTitle.setText("Thêm danh mục mới");
        if (iconFormMode != null) {
            iconFormMode.setIconLiteral("fth-plus-circle");
            iconFormMode.setIconColor(javafx.scene.paint.Color.web("#12b6a2"));
        }
        txtName.clear();
        chkAvailable.setSelected(true);
        btnSave.setText("Thêm mới");
    }

    @FXML
    private void handleSave(ActionEvent event) {
        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            DialogHelper.showInfo("Lỗi", "Tên danh mục không được để trống!");
            return;
        }

        try {
            if (editingCategory == null) {
                // Thêm mới
                CategoryDTO newDto = new CategoryDTO();
                newDto.setName(name);
                newDto.setDescription("");
                newDto.setAvailable(chkAvailable.isSelected());
                categoryService.createCategory(newDto);
                DialogHelper.showInfo("Thành công", "Đã thêm danh mục mới!");
            } else {
                // Cập nhật
                editingCategory.setName(name);
                editingCategory.setAvailable(chkAvailable.isSelected());
                categoryService.updateCategory(editingCategory);
                DialogHelper.showInfo("Thành công", "Đã cập nhật danh mục!");
            }
            handleResetForm(null);
            loadCategories();
        } catch (Exception e) {
            e.printStackTrace();
            DialogHelper.showInfo("Lỗi", "Có lỗi xảy ra: " + e.getMessage());
        }
    }

    private void handleDeleteCategory(CategoryDTO dto) {
        if (!DialogHelper.showConfirm("Xác nhận", "Bạn có chắc chắn muốn xóa danh mục '" + dto.getName() + "' không? (Nếu có món ăn thuộc danh mục này, bạn phải đổi danh mục cho chúng trước).")) {
            return;
        }
        try {
            categoryService.softDeleteCategory(dto.getCategoryId());
            DialogHelper.showInfo("Thành công", "Đã xóa danh mục!");
            loadCategories();
        } catch (Exception e) {
            e.printStackTrace();
            DialogHelper.showInfo("Lỗi", "Không thể xóa: " + e.getMessage());
        }
    }

    @FXML
    private void handleClose(ActionEvent event) {
        Stage stage = (Stage) btnSave.getScene().getWindow();
        stage.close();
    }
}
