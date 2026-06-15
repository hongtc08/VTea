package com.vtea.controller;

import com.vtea.dto.CustomerDTO;
import com.vtea.service.CustomerService;
import com.vtea.utils.DialogHelper;
import com.vtea.utils.FormatUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;

public class CustomerRowController {

    @FXML private Label lblName;
    @FXML private Label lblPhone;
    @FXML private Label lblPoints;
    @FXML private Label lblTier;
    @FXML private Label lblLastPurchase;

    private CustomerDTO customer;
    private CustomerController parentController;
    private final CustomerService customerService = new CustomerService();

    public void setData(CustomerDTO customer, CustomerController parentController) {
        this.customer = customer;
        this.parentController = parentController;

        lblName.setText(customer.getFullName());
        lblPhone.setText(customer.getPhoneNumber());
        lblPoints.setText(FormatUtils.formatNumber(customer.getTotalAccumulatedPoints()));
        
        lblTier.setText(customer.getTierName());
        // Set CSS based on tier
        lblTier.getStyleClass().removeAll("tier-kimcuong", "tier-vang", "tier-bac", "tier-dong");
        String tName = customer.getTierName() != null ? customer.getTierName().toLowerCase() : "";
        if (tName.contains("kim") || tName.contains("diamond")) {
            lblTier.getStyleClass().add("tier-kimcuong");
        } else if (tName.contains("vàng") || tName.contains("vang") || tName.contains("gold")) {
            lblTier.getStyleClass().add("tier-vang");
        } else if (tName.contains("bạc") || tName.contains("bac") || tName.contains("silver")) {
            lblTier.getStyleClass().add("tier-bac");
        } else {
            lblTier.getStyleClass().add("tier-dong");
        }

        if (customer.getLastPurchase() != null) {
            lblLastPurchase.setText(customer.getLastPurchase().format(DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy")));
        } else {
            lblLastPurchase.setText("Chưa có");
        }
    }

    @FXML
    private void handleEdit(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vtea/view/CustomerEditForm.fxml"));
            Parent root = loader.load();
            
            CustomerEditFormController formController = loader.getController();
            formController.setCustomer(customer);

            Stage stage = new Stage();
            stage.initStyle(javafx.stage.StageStyle.UNDECORATED);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            
            DialogHelper.applyBlurBackground(true);
            DialogHelper.animateDialog(root);
            try {
                stage.showAndWait();
            } finally {
                DialogHelper.applyBlurBackground(false);
            }

            if (formController.isSaved()) {
                parentController.loadData();
                DialogHelper.showInfo("Thành công", "Đã cập nhật thông tin khách hàng!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        boolean confirm = DialogHelper.showConfirm("Xác nhận", "Bạn có chắc muốn xóa khách hàng này?\nHóa đơn cũ của khách hàng sẽ được giữ lại dưới dạng khách vãng lai.");
        if (confirm) {
            boolean success = customerService.deleteCustomer(customer.getCustomerId());
            if (success) {
                parentController.loadData();
                DialogHelper.showInfo("Thành công", "Đã xóa khách hàng!");
            } else {
                DialogHelper.showInfo("Lỗi", "Không thể xóa khách hàng này.");
            }
        }
    }
}
