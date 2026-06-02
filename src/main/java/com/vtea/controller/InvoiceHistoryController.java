package com.vtea.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;

public class InvoiceHistoryController {

    @FXML private TextField searchField;
    @FXML private VBox invoiceGroupsContainer; // Đây là "cái thùng" chứa các tháng

    @FXML
    public void initialize() {
        // Gọi hàm load dữ liệu ngay khi mở màn hình
        loadInvoiceData();
    }

    private void loadInvoiceData() {
        invoiceGroupsContainer.getChildren().clear();

        // TODO: (Ví dụ) Lấy danh sách các tháng có hóa đơn từ DB
        String[] danhSachCacThang = {"Hôm nay", "Tháng 6/2026", "Tháng 5/2026"};

        for (String monthName : danhSachCacThang) {
            try {
                // 1. Đúc ra cái khung của Tháng
                FXMLLoader groupLoader = new FXMLLoader(getClass().getResource("/com/vtea/view/InvoiceGroup.fxml"));
                VBox groupNode = groupLoader.load();

                // 2. Gắn tên tháng
                Label lblGroupName = (Label) groupNode.lookup("#lblGroupName");
                if (lblGroupName != null) lblGroupName.setText(monthName);

                // 3. Xử lý logic Đóng/Mở cho riêng cái tháng này
                VBox groupContent = (VBox) groupNode.lookup("#groupContent");
                FontIcon iconArrow = (FontIcon) groupNode.lookup("#iconArrow");
                HBox groupHeader = (HBox) groupNode.lookup("#groupHeader");

                if (groupHeader != null && groupContent != null && iconArrow != null) {
                    // Ràng buộc managed = visible
                    groupContent.managedProperty().bind(groupContent.visibleProperty());

                    // Sự kiện click
                    groupHeader.setOnMouseClicked(e -> {
                        boolean isVisible = groupContent.isVisible();
                        groupContent.setVisible(!isVisible);
                        iconArrow.setIconLiteral(isVisible ? "fth-chevron-right" : "fth-chevron-down");
                    });
                }

                // 4. TODO: Đúc các dòng Hóa đơn (InvoiceRow) nhét vào bên trong groupContent ở đây

                // 5. Thêm cái Tháng đã đúc xong ra màn hình
                invoiceGroupsContainer.getChildren().add(groupNode);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}