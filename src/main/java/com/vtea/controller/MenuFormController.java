package com.vtea.controller;

import com.vtea.dto.CategoryDTO;
import com.vtea.dto.ProductDTO;
import com.vtea.service.CategoryService;
import com.vtea.service.ProductService;
import com.vtea.utils.DialogHelper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import com.vtea.service.POSCacheService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class MenuFormController {

    @FXML private Label lblFormTitle;
    @FXML private Button btnClose;
    @FXML private TextField txtName;
    @FXML private ComboBox<CategoryDTO> cbCategory;
    @FXML private TextField txtPrice;
    @FXML private VBox imageUploadArea;
    @FXML private ImageView imgPreview;
    @FXML private Button btnCancel;
    @FXML private Button btnSubmit;

    private ProductService productService = new ProductService();
    private CategoryService categoryService = new CategoryService();

    private ProductDTO currentProduct;
    private MenuController parentController;
    private String selectedImagePath = null;

    @FXML
    public void initialize() {
        setupCategoryComboBox();
        setupEventHandlers();

        // Remove focus from textfields on start
        Platform.runLater(() -> btnCancel.requestFocus());
    }

    private void setupCategoryComboBox() {
        try {
            List<CategoryDTO> categories = categoryService.getAllActiveCategories();
            cbCategory.getItems().addAll(categories);

            cbCategory.setConverter(new com.vtea.utils.CategoryConverter(cbCategory));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupEventHandlers() {
        btnClose.setOnAction(e -> closeWindow());
        btnCancel.setOnAction(e -> closeWindow());
        btnSubmit.setOnAction(e -> handleSave());

        imageUploadArea.setOnMouseClicked(e -> handleImageUpload());

        // Only allow numbers in price field
        txtPrice.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtPrice.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    public void setProduct(ProductDTO product, MenuController parentController) {
        this.parentController = parentController;
        this.currentProduct = product;

        if (product != null) {
            lblFormTitle.setText("Chỉnh sửa món");
            btnSubmit.setText("Lưu thay đổi");

            txtName.setText(product.getName());
            if (product.getPrice() != null) {
                txtPrice.setText(String.valueOf(product.getPrice().longValue()));
            }

            // Select category
            for (CategoryDTO cat : cbCategory.getItems()) {
                if (cat.getCategoryId() == product.getCategoryId()) {
                    cbCategory.getSelectionModel().select(cat);
                    break;
                }
            }

            // Load image
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                try {
                    selectedImagePath = product.getImageUrl();
                    Image img;
                    if (product.getImageUrl().startsWith("http") || product.getImageUrl().startsWith("file:")) {
                        img = new Image(product.getImageUrl());
                    } else {
                        img = new Image(getClass().getResourceAsStream(product.getImageUrl()));
                    }
                    if (img != null && !img.isError()) {
                        showPreviewImage(img);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            lblFormTitle.setText("Thêm món mới");
            btnSubmit.setText("Xác nhận thêm");
            currentProduct = new ProductDTO();
        }
    }

    private void handleImageUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn hình ảnh món ăn");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        Stage stage = (Stage) imageUploadArea.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                selectedImagePath = saveProductImageToResources(file);

                Image img = new Image(file.toURI().toString());
                showPreviewImage(img);
            } catch (Exception e) {
                e.printStackTrace();
                DialogHelper.showInfo("Lỗi", "Không thể tải hình ảnh: " + e.getMessage());
            }
        }
    }
    private String saveProductImageToResources(File selectedFile) throws IOException {
        String extension = getFileExtension(selectedFile.getName());

        String safeName = txtName.getText() == null
                ? "product"
                : txtName.getText().trim()
                  .toLowerCase()
                  .replaceAll("\\s+", "-")
                  .replaceAll("[^a-z0-9-]", "");

        if (safeName.isEmpty()) {
            safeName = "product";
        }

        String fileName = safeName + "-" + System.currentTimeMillis() + extension;

        // 1. Copy vào src/main/resources để ảnh được commit lên GitHub
        Path sourceResourceDir = Paths.get("src/main/resources/images/products");
        Files.createDirectories(sourceResourceDir);

        Path sourceResourcePath = sourceResourceDir.resolve(fileName);
        Files.copy(
                selectedFile.toPath(),
                sourceResourcePath,
                StandardCopyOption.REPLACE_EXISTING
        );

        // 2. Copy thêm vào target/classes để app đang chạy load được ngay
        Path runtimeResourceDir = Paths.get("target/classes/images/products");
        Files.createDirectories(runtimeResourceDir);

        Path runtimeResourcePath = runtimeResourceDir.resolve(fileName);
        Files.copy(
                selectedFile.toPath(),
                runtimeResourcePath,
                StandardCopyOption.REPLACE_EXISTING
        );

        // DB vẫn lưu đường dẫn resources
        return "/images/products/" + fileName;
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");

        if (dotIndex == -1) {
            return ".png";
        }

        return fileName.substring(dotIndex).toLowerCase();
    }

    private void showPreviewImage(Image img) {
        imgPreview.setImage(img);
        imgPreview.setManaged(true);
        imgPreview.setVisible(true);

        imageUploadArea.getChildren().get(0).setVisible(false);
        imageUploadArea.getChildren().get(0).setManaged(false);
        imageUploadArea.getChildren().get(1).setVisible(false);
        imageUploadArea.getChildren().get(1).setManaged(false);
    }

    private void handleSave() {
        try {
            if (txtName.getText() == null || txtName.getText().trim().isEmpty()) {
                DialogHelper.showInfo("Lỗi", "Vui lòng nhập tên món ăn.");
                return;
            }
            if (cbCategory.getSelectionModel().getSelectedItem() == null) {
                DialogHelper.showInfo("Lỗi", "Vui lòng chọn loại món.");
                return;
            }
            if (txtPrice.getText() == null || txtPrice.getText().trim().isEmpty()) {
                DialogHelper.showInfo("Lỗi", "Vui lòng nhập giá bán.");
                return;
            }

            BigDecimal price = new BigDecimal(txtPrice.getText());
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                DialogHelper.showInfo("Lỗi", "Giá bán phải lớn hơn 0.");
                return;
            }

            currentProduct.setName(txtName.getText().trim());
            currentProduct.setCategoryId(cbCategory.getSelectionModel().getSelectedItem().getCategoryId());
            currentProduct.setPrice(price);

            if (selectedImagePath != null) {
                currentProduct.setImageUrl(selectedImagePath);
            } else {
                currentProduct.setImageUrl("");
            }
            currentProduct.setAvailable(true);

            if (currentProduct.getProductId() > 0) {
                productService.updateProduct(currentProduct);
                DialogHelper.showInfo("Thành công", "Đã cập nhật món ăn.");
            } else {
                productService.createProduct(currentProduct);
                DialogHelper.showInfo("Thành công", "Đã thêm món ăn mới.");
            }
            POSCacheService.getInstance().refresh();
            if (parentController != null) {
                parentController.loadData();
            }
            closeWindow();

        } catch (Exception e) {
            e.printStackTrace();
            DialogHelper.showInfo("Lỗi lưu dữ liệu", e.getMessage());
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }
}
