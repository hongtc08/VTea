package com.vtea.controller;
import com.vtea.service.POSCacheService;
import com.vtea.dto.CategoryDTO;
import com.vtea.dto.ProductDTO;
import com.vtea.service.CategoryService;
import com.vtea.service.ProductService;
import com.vtea.utils.DialogHelper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import com.vtea.service.POSCacheService;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MenuController {

    // ===== ĐỊNH NGHĨA ID DANH MỤC CỐ ĐỊNH (Khớp với Database và POSController) =====
    private static final int CATEGORY_ALL = -1;
    @FXML private TextField searchField;
    @FXML private HBox categoryBar;
    @FXML private FlowPane menuGrid;

    private ProductService productService = new ProductService();
    private CategoryService categoryService = new CategoryService();
    private final POSCacheService posCacheService = POSCacheService.getInstance();

    private List<ProductDTO> allProducts = new ArrayList<>();
    private List<CategoryDTO> allCategories = new ArrayList<>();

    // Thay thế biến String thành int để lưu ID danh mục đang chọn
    private int currentCategoryIdFilter = CATEGORY_ALL;

    @FXML
    public void initialize() {
        loadData();
        setupSearch();
    }

    public void loadData() {
        try {
            posCacheService.loadIfNeeded();

            allCategories = posCacheService.getCategories();
            allProducts = posCacheService.getProducts();

            currentCategoryIdFilter = CATEGORY_ALL;
            setupCategoryButtons();

            for (ProductDTO product : allProducts) {
                CategoryDTO cat = allCategories.stream()
                        .filter(c -> c.getCategoryId() == product.getCategoryId())
                        .findFirst()
                        .orElse(null);

                if (cat != null) {
                    product.setCategoryName(cat.getName());
                } else {
                    product.setCategoryName("Khác");
                }
            }

            filterProducts();
        } catch (Exception e) {
            e.printStackTrace();
            DialogHelper.showInfo("Lỗi tải dữ liệu", "Không thể tải danh sách sản phẩm: " + e.getMessage());
        }
    }

    private void setupCategoryButtons() {
        if (categoryBar == null) {
            return;
        }

        categoryBar.getChildren().clear();

        Button allButton = createCategoryButton("T\u1ea5t c\u1ea3");
        allButton.getStyleClass().add("category-btn-active");
        allButton.setOnAction(event -> {
            currentCategoryIdFilter = CATEGORY_ALL;
            updateActiveCategoryButton(allButton);
            filterProducts();
        });
        categoryBar.getChildren().add(allButton);

        for (CategoryDTO category : allCategories) {
            Button categoryButton = createCategoryButton(category.getName());
            categoryButton.setOnAction(event -> {
                currentCategoryIdFilter = category.getCategoryId();
                updateActiveCategoryButton(categoryButton);
                filterProducts();
            });
            categoryBar.getChildren().add(categoryButton);
        }
    }

    private Button createCategoryButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("category-btn");
        return button;
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterProducts();
        });
    }

    // ===== BỘ LỌC ĐÃ ĐƯỢC NÂNG CẤP =====
    private void filterProducts() {
        menuGrid.getChildren().clear();
        String searchText = searchField.getText() == null ? "" : searchField.getText().toLowerCase();

        for (ProductDTO product : allProducts) {
            // 1. Kiểm tra điều kiện tìm kiếm bằng chữ
            boolean matchesSearch = product.getName().toLowerCase().contains(searchText);

            // 2. Kiểm tra điều kiện danh mục (Lọc bằng ID cực kỳ chính xác và an toàn)
            boolean matchesCategory = (currentCategoryIdFilter == CATEGORY_ALL) ||
                    (product.getCategoryId() == currentCategoryIdFilter);

            // Nếu thỏa mãn cả 2 điều kiện thì đưa lên giao diện
            if (matchesSearch && matchesCategory) {
                menuGrid.getChildren().add(createProductCard(product));
            }
        }
    }

    private VBox createProductCard(ProductDTO product) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vtea/view/MenuItem.fxml"));
            VBox card = loader.load();

            ImageView imgProduct = (ImageView) loader.getNamespace().get("imgProduct");
            Label lblProductName = (Label) loader.getNamespace().get("lblProductName");
            Label lblCategory = (Label) loader.getNamespace().get("lblCategory");
            Label lblPrice = (Label) loader.getNamespace().get("lblPrice");
            Button btnEdit = (Button) loader.getNamespace().get("btnEdit");
            Button btnDelete = (Button) loader.getNamespace().get("btnDelete");

            lblProductName.setText(product.getName());
            lblCategory.setText(product.getCategoryName());

            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
            lblPrice.setText(formatter.format(product.getPrice()) + "đ");

            try {
                if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                    Image img;
                    if (product.getImageUrl().startsWith("http") || product.getImageUrl().startsWith("file:")) {
                        img = new Image(product.getImageUrl());
                    } else {
                        img = new Image(getClass().getResourceAsStream(product.getImageUrl()));
                    }
                    if (img != null && !img.isError()) {
                        imgProduct.setImage(img);
                    }
                }
            } catch (Exception e) {
                System.out.println("Could not load image for: " + product.getName());
            }

            btnEdit.setOnAction(e -> handleEditProduct(product));
            btnDelete.setOnAction(e -> handleDeleteProduct(product));

            return card;
        } catch (IOException e) {
            e.printStackTrace();
            return new VBox();
        }
    }

    private void handleEditProduct(ProductDTO product) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vtea/view/MenuForm.fxml"));
            Parent root = loader.load();

            MenuFormController controller = loader.getController();
            controller.setProduct(product, this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);

            Scene scene = new Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            DialogHelper.showInfo("Lỗi", "Không thể mở form sửa món: " + e.getMessage());
        }
    }

    private void handleDeleteProduct(ProductDTO product) {
        boolean isConfirmed = DialogHelper.showConfirm("Xóa món", "Bạn có chắc chắn muốn xóa '" + product.getName() + "'?");
        if (isConfirmed) {
            try {
                productService.softDeleteProduct(product.getProductId());
                POSCacheService.getInstance().refresh();

                DialogHelper.showInfo("Thành công", "Đã xóa món thành công.");
                loadData();
            } catch (Exception e) {
                DialogHelper.showInfo("Lỗi", "Không thể xóa món: " + e.getMessage());
            }
        }
    }

    public void handleAddNewDish(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vtea/view/MenuForm.fxml"));
            Parent root = loader.load();

            MenuFormController controller = loader.getController();
            controller.setProduct(null, this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);

            Scene scene = new Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            DialogHelper.showInfo("Lỗi", "Không thể mở form thêm món: " + e.getMessage());
        }
    }

    private void updateActiveCategoryButton(Button activeButton) {
        if (categoryBar != null) {
            for (javafx.scene.Node node : categoryBar.getChildren()) {
                if (node instanceof Button button) {
                    button.getStyleClass().remove("category-btn-active");
                }
            }
        }

        activeButton.getStyleClass().add("category-btn-active");
    }

    // ===== SỰ KIỆN CLICK CÁC NÚT BỘ LỌC =====
}
