package com.vtea.controller;

import com.vtea.model.User;
import com.vtea.service.EmployeeService;
import com.vtea.utils.DialogHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.List;

public class EmployeeController {

    @FXML private TextField searchField;
    @FXML private VBox employeeListContainer;

    private final EmployeeService employeeService = new EmployeeService();
    private List<User> allEmployees;

    @FXML
    public void initialize() {
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> renderEmployeeList());
        }
        loadEmployees();
    }

    public void loadEmployees() {
        allEmployees = employeeService.getAllEmployees();
        renderEmployeeList();
    }

    private void renderEmployeeList() {
        if (employeeListContainer == null) {
            return;
        }
        employeeListContainer.getChildren().clear();

        String keyword = searchField != null && searchField.getText() != null
                ? searchField.getText().trim().toLowerCase()
                : "";

        for (User user : allEmployees) {
            if (!matchesSearch(user, keyword)) {
                continue;
            }
            HBox row = loadEmployeeRow(user);
            if (row != null) {
                employeeListContainer.getChildren().add(row);
            }
        }

        if (employeeListContainer.getChildren().isEmpty()) {
            employeeListContainer.getChildren().add(new Label("Không có nhân viên nào"));
        }
    }

    private boolean matchesSearch(User user, String keyword) {
        if (keyword.isEmpty()) {
            return true;
        }
        String name = user.getFullName() != null ? user.getFullName().toLowerCase() : "";
        String username = user.getUserName() != null ? user.getUserName().toLowerCase() : "";
        return name.contains(keyword) || username.contains(keyword);
    }

    private HBox loadEmployeeRow(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vtea/view/EmployeeRow.fxml"));
            HBox row = loader.load();

            String initial = user.getFullName() != null && !user.getFullName().isBlank()
                    ? user.getFullName().substring(0, 1).toUpperCase()
                    : "?";

            ((Label) row.lookup("#lblAvatarText")).setText(initial);
            ((Label) row.lookup("#lblName")).setText(user.getFullName());
            ((Label) row.lookup("#lblEmail")).setText(user.getUserName());
            ((Label) row.lookup("#lblRole")).setText(formatRole(user.getRole()));
            ((Label) row.lookup("#lblPhone")).setText(user.getUserName());
            ((Label) row.lookup("#lblSalary")).setText("—");
            ((Label) row.lookup("#lblStartDate")).setText(user.getCreatedAt() != null
                    ? user.getCreatedAt().toLocalDate().toString()
                    : "—");
            ((Label) row.lookup("#lblStatus")).setText(formatStatus(user.getStatus()));

            Button btnEdit = (Button) row.lookup("#btnEdit");
            Button btnDelete = (Button) row.lookup("#btnDelete");

            if (btnEdit != null) {
                btnEdit.setOnAction(e -> openEmployeeForm(user));
            }
            if (btnDelete != null) {
                btnDelete.setOnAction(e -> toggleLockUser(user));
            }

            return row;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @FXML
    public void handleAddEmployee(ActionEvent actionEvent) {
        openEmployeeForm(null);
    }

    private void openEmployeeForm(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vtea/view/EmployeeForm.fxml"));
            Parent root = loader.load();

            EmployeeFormController controller = loader.getController();
            controller.setEmployee(user, this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            DialogHelper.showInfo("Lỗi", "Không thể mở form nhân viên!");
        }
    }

    private void toggleLockUser(User user) {
        boolean currentlyLocked = EmployeeService.isLocked(user.getStatus());
        String action = currentlyLocked ? "mở khóa" : "khóa";

        if (employeeService.toggleUserLock(user.getUserId())) {
            DialogHelper.showInfo("Thành công", "Đã " + action + " tài khoản " + user.getFullName());
            loadEmployees();
        } else {
            DialogHelper.showInfo("Lỗi", "Không thể " + action + " tài khoản. Kiểm tra kết nối database!");
        }
    }

    private String formatRole(String role) {
        if (role != null && role.equalsIgnoreCase("ADMIN")) {
            return "Quản lý";
        }
        return "Nhân viên";
    }

    private String formatStatus(String status) {
        if (EmployeeService.isLocked(status)) {
            return "Đã khóa";
        }
        return "Đang làm việc";
    }
}
