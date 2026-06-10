package com.vtea.controller;

import com.vtea.dto.IngredientDTO;
import com.vtea.dto.UserSessionDTO;
import com.vtea.model.Ingredient;
import com.vtea.service.IngredientService;
import com.vtea.utils.DialogHelper;
import com.vtea.utils.SessionManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InventoryController {

    @FXML private HBox adminHeaderRow;
    @FXML private HBox staffHeaderRow;
    @FXML private VBox inventoryListContainer;
    @FXML private Button btnAddInventory;
    @FXML private TextField searchField;

    private final IngredientService ingredientService = new IngredientService();
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
        } else {
            adminHeaderRow.setVisible(false);
            adminHeaderRow.setManaged(false);
            staffHeaderRow.setVisible(true);
            staffHeaderRow.setManaged(true);
            btnAddInventory.setVisible(false);
            btnAddInventory.setManaged(false);
        }

        // Search listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            renderList(newValue);
        });

        loadData();
    }

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
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            DialogHelper.showInfo("Lỗi", "Không thể mở form thêm nguyên liệu: " + e.getMessage());
        }
    }
}
