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

    @FXML
    public void initialize() {
        loadData();
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
            List<User> userList = userService.getAllUsers();

            // Duyệt từng user để vẽ ra một hàng (Row)
            for (User user : userList) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vtea/view/EmployeeRow.fxml"));
                Parent row = loader.load();

                // Truyền dữ liệu vào Row
                EmployeeRowController rowController = loader.getController();
                rowController.setData(user, this);

                // Thêm hàng vào danh sách hiển thị
                vboxEmployeeList.getChildren().add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
            DialogHelper.showInfo("Lỗi", "Không thể tải danh sách nhân viên: " + e.getMessage());
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
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            DialogHelper.showInfo("Lỗi", "Không thể mở form thêm nhân viên. Vui lòng kiểm tra lại đường dẫn file FXML.");
        }
    }
}