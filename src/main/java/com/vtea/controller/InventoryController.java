package com.vtea.controller;

import com.vtea.dto.IngredientDTO;
import com.vtea.dto.InventoryCheckDTO;
import com.vtea.dto.InventoryTransactionDTO;
import com.vtea.dto.UserSessionDTO;
import com.vtea.model.Ingredient;
import com.vtea.service.IngredientService;
import com.vtea.service.InventoryService;
import com.vtea.utils.DialogHelper;
import com.vtea.utils.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InventoryController {

    // ================= FXML TAB 1 (TỒN KHO) =================
    @FXML private HBox adminHeaderRow;
    @FXML private HBox staffHeaderRow;
    @FXML private VBox inventoryListContainer;
    @FXML private Button btnAddInventory;
    @FXML private TextField searchField;

    // ================= FXML TABS KHUNG CHUNG =================
    @FXML private TabPane mainTabPane;
    @FXML private Tab tabStock;
    @FXML private Tab tabApproval;
    @FXML private Tab tabHistory;

    // ================= FXML TAB 2 & TAB 3 (CONTAINER CHỨA DỮ LIỆU) =================
    @FXML private VBox pendingCheckListContainer;
    @FXML private VBox transactionListContainer;

    // ================= SERVICES & BIẾN LƯU TRỮ =================
    private final IngredientService ingredientService = new IngredientService();
    private final InventoryService inventoryService = new InventoryService();
    private String currentUserRole = "";

    private List<IngredientDTO> adminFullList = new ArrayList<>();
    private List<Ingredient> staffFullList = new ArrayList<>();

    @FXML
    public void initialize() {
        UserSessionDTO user = SessionManager.getCurrentUser();
        if (user != null) {
            currentUserRole = user.getRole();
        }

        if ("ADMIN".equalsIgnoreCase(currentUserRole)) {
            adminHeaderRow.setVisible(true);
            adminHeaderRow.setManaged(true);
            staffHeaderRow.setVisible(false);
            staffHeaderRow.setManaged(false);
            btnAddInventory.setVisible(true);
            btnAddInventory.setManaged(true);

            // Lắng nghe sự kiện chuyển Tab để load lại data mới nhất
            mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
                if (newTab == tabApproval) loadPendingChecks();
                else if (newTab == tabHistory) loadHistoryTransactions();
                else if (newTab == tabStock) loadData();
            });

        } else {
            // Nếu là STAFF thì ẩn hoàn toàn 2 Tab của Admin đi
            mainTabPane.getTabs().removeAll(tabApproval, tabHistory);

            adminHeaderRow.setVisible(false);
            adminHeaderRow.setManaged(false);
            staffHeaderRow.setVisible(true);
            staffHeaderRow.setManaged(true);
            btnAddInventory.setVisible(false);
            btnAddInventory.setManaged(false);
        }

        // Search listener cho Tab 1
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            renderList(newValue);
        });

        // Tải dữ liệu lần đầu khi mở form
        loadData();
    }

    // ===================== LOGIC TAB 1: DANH SÁCH TỒN KHO =====================
    public void loadData() {
        if ("ADMIN".equalsIgnoreCase(currentUserRole)) {
            adminFullList = ingredientService.getAllIngredientsForAdmin();
        } else {
            staffFullList = ingredientService.getAllActiveIngredients();
        }
        renderList(searchField.getText());
    }

    private void renderList(String keyword) {
        inventoryListContainer.getChildren().clear();
        String kw = keyword == null ? "" : keyword.toLowerCase().trim();

        if ("ADMIN".equalsIgnoreCase(currentUserRole)) {
            for (IngredientDTO item : adminFullList) {
                if (item.getName().toLowerCase().contains(kw)) {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vtea/view/InventoryAdminRow.fxml"));
                        HBox row = loader.load();
                        InventoryAdminRowController controller = loader.getController();
                        controller.setData(item, this);
                        inventoryListContainer.getChildren().add(row);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            for (Ingredient item : staffFullList) {
                if (item.getName().toLowerCase().contains(kw)) {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vtea/view/InventoryStaffRow.fxml"));
                        HBox row = loader.load();
                        InventoryStaffRowController controller = loader.getController();
                        controller.setData(item, this);
                        inventoryListContainer.getChildren().add(row);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (inventoryListContainer.getChildren().isEmpty()) {
            javafx.scene.layout.VBox emptyState = new javafx.scene.layout.VBox(16);
            emptyState.setAlignment(javafx.geometry.Pos.CENTER);
            emptyState.setPadding(new javafx.geometry.Insets(100, 0, 0, 0));
            
            org.kordamp.ikonli.javafx.FontIcon icon = new org.kordamp.ikonli.javafx.FontIcon("fth-package");
            icon.setIconSize(64);
            icon.setIconColor(javafx.scene.paint.Color.web("#d6d3d1"));
            
            javafx.scene.control.Label lbl = new javafx.scene.control.Label("Không tìm thấy nguyên liệu nào");
            lbl.setStyle("-fx-text-fill: #a8a29e; -fx-font-size: 18px; -fx-font-weight: bold;");
            
            emptyState.getChildren().addAll(icon, lbl);
            inventoryListContainer.getChildren().add(emptyState);
        }
    }

    @FXML
    public void handleAddInventory(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vtea/view/InventoryForm.fxml"));
            Parent root = loader.load();

            InventoryFormController controller = loader.getController();
            controller.setIngredient(null, this);

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
        } catch (IOException e) {
            DialogHelper.showInfo("Lỗi", "Không thể mở form thêm nguyên liệu: " + e.getMessage());
        }
    }

    // ===================== LOGIC TAB 2: DUYỆT KIỂM KHO =====================
    public void loadPendingChecks() {
        pendingCheckListContainer.getChildren().clear();
        List<InventoryCheckDTO> list = inventoryService.getAllPendingChecks();

        for (InventoryCheckDTO item : list) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vtea/view/PendingCheckRow.fxml"));
                HBox row = loader.load();
                PendingCheckRowController controller = loader.getController();
                controller.setData(item, this);
                pendingCheckListContainer.getChildren().add(row);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // ===================== LOGIC TAB 3: LỊCH SỬ GIAO DỊCH =====================
    public void loadHistoryTransactions() {
        transactionListContainer.getChildren().clear();
        List<InventoryTransactionDTO> list = inventoryService.getTransactionHistory();

        for (InventoryTransactionDTO item : list) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vtea/view/TransactionRow.fxml"));
                HBox row = loader.load();
                TransactionRowController controller = loader.getController();
                controller.setData(item);
                transactionListContainer.getChildren().add(row);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}