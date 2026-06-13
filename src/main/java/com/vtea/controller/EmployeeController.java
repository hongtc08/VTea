package com.vtea.controller;

import com.vtea.model.User;
import com.vtea.service.UserService;
import com.vtea.utils.DialogHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.List;

public class EmployeeController {

    // --- CÁC BIẾN MAP VỚI GIAO DIỆN ---
    @FXML private TextField searchField;
    @FXML private Button btnFilterAll;
    @FXML private Button btnFilterAdmin;
    @FXML private Button btnFilterStaff;
    @FXML private VBox vboxEmployeeList;

    private final UserService userService = new UserService();
    private List<User> allUsers;
    private String currentRoleFilter = "ALL";

    @FXML
    public void initialize() {
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> filterData());
        }
        
        if (btnFilterAll != null) {
            btnFilterAll.setOnAction(e -> {
                setFilterRole("ALL", btnFilterAll);
            });
        }
        
        if (btnFilterAdmin != null) {
            btnFilterAdmin.setOnAction(e -> {
                setFilterRole("ADMIN", btnFilterAdmin);
            });
        }
        
        if (btnFilterStaff != null) {
            btnFilterStaff.setOnAction(e -> {
                setFilterRole("STAFF", btnFilterStaff);
            });
        }
        
        loadData();
    }
    
    private void setFilterRole(String role, Button clickedBtn) {
        currentRoleFilter = role;
        
        if (btnFilterAll != null) btnFilterAll.getStyleClass().remove("category-btn-active");
        if (btnFilterAdmin != null) btnFilterAdmin.getStyleClass().remove("category-btn-active");
        if (btnFilterStaff != null) btnFilterStaff.getStyleClass().remove("category-btn-active");
        
        if (clickedBtn != null && !clickedBtn.getStyleClass().contains("category-btn-active")) {
            clickedBtn.getStyleClass().add("category-btn-active");
        }
        
        filterData();
    }

    /**
     * Hàm lấy danh sách nhân viên từ Database và vẽ lên giao diện
     */
    public void loadData() {
        if (vboxEmployeeList != null) {
            vboxEmployeeList.getChildren().clear();
        }

        try {
            // Lấy danh sách từ Service
            allUsers = userService.getAllUsers();
            filterData();
        } catch (Exception e) {
            e.printStackTrace();
            DialogHelper.showInfo("Lỗi", "Không thể tải danh sách nhân viên: " + e.getMessage());
        }
    }
    
    private void filterData() {
        if (vboxEmployeeList != null) {
            vboxEmployeeList.getChildren().clear();
        }
        
        if (allUsers == null) return;
        
        String searchText = searchField != null && searchField.getText() != null 
                ? searchField.getText().toLowerCase().trim() : "";

        try {
            for (User user : allUsers) {
                // Check search text
                boolean matchesSearch = false;
                if (searchText.isEmpty()) {
                    matchesSearch = true;
                } else {
                    String name = user.getFullName() != null ? user.getFullName().toLowerCase() : "";
                    String phone = user.getPhone() != null ? user.getPhone().toLowerCase() : "";
                    String email = user.getEmail() != null ? user.getEmail().toLowerCase() : "";
                    
                    if (name.contains(searchText) || phone.contains(searchText) || email.contains(searchText)) {
                        matchesSearch = true;
                    }
                }
                
                // Check role
                boolean matchesRole = false;
                if ("ALL".equals(currentRoleFilter)) {
                    matchesRole = true;
                } else if ("ADMIN".equals(currentRoleFilter) && "ADMIN".equalsIgnoreCase(user.getRole())) {
                    matchesRole = true;
                } else if ("STAFF".equals(currentRoleFilter) && "STAFF".equalsIgnoreCase(user.getRole())) {
                    matchesRole = true;
                }
                
                if (matchesSearch && matchesRole) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vtea/view/EmployeeRow.fxml"));
                Parent row = loader.load();

                // Truyền dữ liệu vào Row
                EmployeeRowController rowController = loader.getController();
                rowController.setData(user, this);

                // Thêm hàng vào danh sách hiển thị
                vboxEmployeeList.getChildren().add(row);
            }
            }
            
            if (vboxEmployeeList.getChildren().isEmpty()) {
                javafx.scene.layout.VBox emptyState = new javafx.scene.layout.VBox(16);
                emptyState.setAlignment(javafx.geometry.Pos.CENTER);
                emptyState.setPadding(new javafx.geometry.Insets(100, 0, 0, 0));
                
                org.kordamp.ikonli.javafx.FontIcon icon = new org.kordamp.ikonli.javafx.FontIcon("fth-users");
                icon.setIconSize(64);
                icon.setIconColor(javafx.scene.paint.Color.web("#d6d3d1"));
                
                javafx.scene.control.Label lbl = new javafx.scene.control.Label("Không tìm thấy nhân viên nào");
                lbl.setStyle("-fx-text-fill: #a8a29e; -fx-font-size: 18px; -fx-font-weight: bold;");
                
                emptyState.getChildren().addAll(icon, lbl);
                vboxEmployeeList.getChildren().add(emptyState);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleAddEmployee(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vtea/view/EmployeeForm.fxml"));
            Parent root = loader.load();

            // Truyền controller cha sang form để sau khi Thêm xong thì form gọi loadData() lại
            EmployeeFormController formController = loader.getController();
            formController.setParentController(this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            stage.setScene(scene);
            
            com.vtea.utils.DialogHelper.applyBlurBackground(true);
            stage.showAndWait();
            com.vtea.utils.DialogHelper.applyBlurBackground(false);

        } catch (IOException e) {
            e.printStackTrace();
            DialogHelper.showInfo("Lỗi", "Không thể mở form thêm nhân viên. Vui lòng kiểm tra lại đường dẫn file FXML.");
        }
    }
}