package com.vtea.controller;
import com.vtea.dto.CategoryDTO;
import com.vtea.dto.ProductDTO;
import com.vtea.dto.ToppingDTO;
import com.vtea.service.CategoryService;
import com.vtea.service.ProductService;
import com.vtea.service.ToppingService;
import com.vtea.utils.DialogHelper;
import com.vtea.utils.FormatUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * MenuController chịu trách nhiệm quản lý màn hình Quản lý Thực đơn (Admin).
 * Hỗ trợ hiển thị, lọc, thêm, sửa, xóa Món ăn (Product) và Topping.
 */
public class MenuController {

    // ===== ĐỊNH NGHĨA ID DANH MỤC CỐ ĐỊNH (Khớp với Database và POSController) =====
    private static final int CATEGORY_ALL = -1;
    @FXML private TextField searchField;
    @FXML private ScrollPane categoryScroll;
    @FXML private HBox categoryBar;
    @FXML private FlowPane menuGrid;
    @FXML private Label lblAddItem;
    @FXML private Label lblToggleToppingMode;

    private ProductService productService = new ProductService();
    private CategoryService categoryService = new CategoryService();
    private ToppingService toppingService = new ToppingService();

    private List<ProductDTO> allProducts = new ArrayList<>();
    private List<CategoryDTO> allCategories = new ArrayList<>();
    private List<ToppingDTO> allToppings = new ArrayList<>();

    // Thay thế biến String thành int để lưu ID danh mục đang chọn
    private int currentCategoryIdFilter = CATEGORY_ALL;
    private boolean toppingMode = false;

    @FXML
    public void initialize() {
        loadData();
        setupSearch();
    }

    /**
     * Tải toàn bộ dữ liệu món ăn và danh mục từ Cache/Database,
     * thiết lập giao diện danh mục và hiển thị sản phẩm.
     */
    public void loadData() {
        try {
            allCategories = categoryService.getAllActiveCategories();
            allProducts = productService.getAllActiveProducts();

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
            refreshGrid();
        });
    }

    @FXML
    private void handleManageCategory(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vtea/view/category-manager.fxml"));
            javafx.scene.Parent root = loader.load();
            
            showFormStage(root);
            
            // Sau khi đóng dialog, nạp lại cache và tải lại view
            posCacheService.refresh();
            loadData();
        } catch (Exception e) {
            e.printStackTrace();
            DialogHelper.showInfo("Lỗi", "Không thể mở form quản lý danh mục: " + e.getMessage());
        }
    }

    @FXML
    private void handleToggleToppingMode(ActionEvent event) {
        toppingMode = !toppingMode;

        if (toppingMode) {
            loadToppingData();
            setCategoryVisible(false);
            if (lblAddItem != null) {
                lblAddItem.setText("Thêm topping");
            }
            if (lblToggleToppingMode != null) {
                lblToggleToppingMode.setText("Quản lý món");
            }
            refreshGrid();
            return;
        }

        setCategoryVisible(true);
        if (lblAddItem != null) {
            lblAddItem.setText("Thêm món ăn");
        }
        if (lblToggleToppingMode != null) {
            lblToggleToppingMode.setText("Quản lý topping");
        }
        loadData();
    }

    private void loadToppingData() {
        try {
            allToppings = toppingService.getAllActiveToppings();
        } catch (Exception e) {
            e.printStackTrace();
            DialogHelper.showInfo("Lỗi tải dữ liệu", "Không thể tải danh sách topping: " + e.getMessage());
        }
    }

    private void refreshGrid() {
        if (toppingMode) {
            filterToppings();
        } else {
            filterProducts();
        }
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
        
        if (menuGrid.getChildren().isEmpty()) {
            javafx.scene.layout.VBox emptyState = new javafx.scene.layout.VBox(16);
            emptyState.setAlignment(javafx.geometry.Pos.CENTER);
            emptyState.setPadding(new javafx.geometry.Insets(100, 0, 0, 0));
            emptyState.setPrefWidth(800);
            
            org.kordamp.ikonli.javafx.FontIcon icon = new org.kordamp.ikonli.javafx.FontIcon("fth-coffee");
            icon.setIconSize(64);
            icon.setIconColor(javafx.scene.paint.Color.web("#d6d3d1"));
            
            javafx.scene.control.Label lbl = new javafx.scene.control.Label("Không tìm thấy món nào");
            lbl.setStyle("-fx-text-fill: #a8a29e; -fx-font-size: 18px; -fx-font-weight: bold;");
            
            emptyState.getChildren().addAll(icon, lbl);
            menuGrid.getChildren().add(emptyState);
        }
    }

    private void filterToppings() {
        menuGrid.getChildren().clear();
        String searchText = searchField.getText() == null ? "" : searchField.getText().toLowerCase();

        for (ToppingDTO topping : allToppings) {
            boolean matchesSearch = topping.getName() != null
                    && topping.getName().toLowerCase().contains(searchText);

            if (matchesSearch) {
                menuGrid.getChildren().add(createToppingCard(topping));
            }
        }
        
        if (menuGrid.getChildren().isEmpty()) {
            javafx.scene.layout.VBox emptyState = new javafx.scene.layout.VBox(16);
            emptyState.setAlignment(javafx.geometry.Pos.CENTER);
            emptyState.setPadding(new javafx.geometry.Insets(100, 0, 0, 0));
            emptyState.setPrefWidth(800);
            
            org.kordamp.ikonli.javafx.FontIcon icon = new org.kordamp.ikonli.javafx.FontIcon("fth-grid");
            icon.setIconSize(64);
            icon.setIconColor(javafx.scene.paint.Color.web("#d6d3d1"));
            
            javafx.scene.control.Label lbl = new javafx.scene.control.Label("Không tìm thấy topping nào");
            lbl.setStyle("-fx-text-fill: #a8a29e; -fx-font-size: 18px; -fx-font-weight: bold;");
            
            emptyState.getChildren().addAll(icon, lbl);
            menuGrid.getChildren().add(emptyState);
        }
    }

    /**
     * Tạo giao diện thẻ (card) dùng chung cho cả Món ăn và Topping.
     */
    private VBox createMenuItemCard(String name, String categoryName, java.math.BigDecimal price, String imageUrl, Runnable onEdit, Runnable onDelete) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vtea/view/MenuItem.fxml"));
            VBox card = loader.load();

            ImageView imgProduct = (ImageView) loader.getNamespace().get("imgProduct");
            Label lblProductName = (Label) loader.getNamespace().get("lblProductName");
            Label lblCategory = (Label) loader.getNamespace().get("lblCategory");
            Label lblPrice = (Label) loader.getNamespace().get("lblPrice");
            Button btnEdit = (Button) loader.getNamespace().get("btnEdit");
            Button btnDelete = (Button) loader.getNamespace().get("btnDelete");

            lblProductName.setText(name);
            lblCategory.setText(categoryName);
            lblPrice.setText(FormatUtils.formatPrice(price));

            try {
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Image img;
                    if (imageUrl.startsWith("http") || imageUrl.startsWith("file:")) {
                        img = new Image(imageUrl);
                    } else {
                        img = new Image(getClass().getResourceAsStream(imageUrl));
                    }
                    if (img != null && !img.isError()) {
                        imgProduct.setImage(img);
                    }
                }
            } catch (Exception e) {
                System.out.println("Could not load image for: " + name);
            }

            btnEdit.setOnAction(e -> onEdit.run());
            btnDelete.setOnAction(e -> onDelete.run());

            return card;
        } catch (IOException e) {
            e.printStackTrace();
            return new VBox();
        }
    }

    /**
     * Tạo thẻ hiển thị cho một món ăn.
     */
    private VBox createProductCard(ProductDTO product) {
        return createMenuItemCard(
            product.getName(), 
            product.getCategoryName(), 
            product.getPrice(), 
            product.getImageUrl(), 
            () -> handleEditProduct(product), 
            () -> handleDeleteProduct(product)
        );
    }

    /**
     * Tạo thẻ hiển thị cho một topping.
     */
    private VBox createToppingCard(ToppingDTO topping) {
        return createMenuItemCard(
            topping.getName(), 
            topping.getAvailable() ? "Topping" : "Topping (ngừng bán)", 
            topping.getPrice(), 
            topping.getImageUrl(), 
            () -> handleEditTopping(topping), 
            () -> handleDeleteTopping(topping)
        );
    }

    /**
     * Mở form để sửa một món ăn đã chọn.
     */
    private void handleEditProduct(ProductDTO product) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vtea/view/MenuForm.fxml"));
            Parent root = loader.load();

            MenuFormController controller = loader.getController();
            controller.setProduct(product, this);

            showFormStage(root);
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

                DialogHelper.showInfo("Thành công", "Đã xóa món thành công.");
                loadData();
            } catch (Exception e) {
                DialogHelper.showInfo("Lỗi", "Không thể xóa món: " + e.getMessage());
            }
        }
    }

    /**
     * Xử lý thêm món ăn hoặc topping mới.
     */
    public void handleAddNewDish(ActionEvent actionEvent) {
        if (toppingMode) {
            handleAddNewTopping();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vtea/view/MenuForm.fxml"));
            Parent root = loader.load();

            MenuFormController controller = loader.getController();
            controller.setProduct(null, this);

            showFormStage(root);
        } catch (IOException e) {
            e.printStackTrace();
            DialogHelper.showInfo("Lỗi", "Không thể mở form thêm món: " + e.getMessage());
        }
    }

    private void handleAddNewTopping() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vtea/view/MenuForm.fxml"));
            Parent root = loader.load();

            MenuFormController controller = loader.getController();
            controller.setTopping(null, this);

            showFormStage(root);
        } catch (IOException e) {
            e.printStackTrace();
            DialogHelper.showInfo("Lỗi", "Không thể mở form thêm topping: " + e.getMessage());
        }
    }

    private void handleEditTopping(ToppingDTO topping) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vtea/view/MenuForm.fxml"));
            Parent root = loader.load();

            MenuFormController controller = loader.getController();
            controller.setTopping(topping, this);

            showFormStage(root);
        } catch (IOException e) {
            e.printStackTrace();
            DialogHelper.showInfo("Lỗi", "Không thể mở form sửa topping: " + e.getMessage());
        }
    }

    private void handleDeleteTopping(ToppingDTO topping) {
        boolean isConfirmed = DialogHelper.showConfirm("Xóa topping", "Bạn có chắc chắn muốn xóa '" + topping.getName() + "'?");
        if (isConfirmed) {
            try {
                toppingService.softDeleteTopping(topping.getToppingId());

                DialogHelper.showInfo("Thành công", "Đã xóa topping thành công.");
                reloadToppingMode();
            } catch (Exception e) {
                DialogHelper.showInfo("Lỗi", "Không thể xóa topping: " + e.getMessage());
            }
        }
    }

    public void reloadToppingMode() {
        loadToppingData();
        refreshGrid();
    }

    private void showFormStage(Parent root) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);

        Scene scene = new Scene(root);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        stage.setScene(scene);
        
        com.vtea.utils.DialogHelper.applyBlurBackground(true);
        com.vtea.utils.DialogHelper.animateDialog(root);
        stage.showAndWait();
        com.vtea.utils.DialogHelper.applyBlurBackground(false);
    }

    private void setCategoryVisible(boolean visible) {
        if (categoryScroll != null) {
            categoryScroll.setVisible(visible);
            categoryScroll.setManaged(visible);
            return;
        }

        if (categoryBar != null) {
            categoryBar.setVisible(visible);
            categoryBar.setManaged(visible);
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


}
