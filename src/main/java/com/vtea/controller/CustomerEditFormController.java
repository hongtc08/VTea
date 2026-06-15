package com.vtea.controller;

import com.vtea.dto.CustomerDTO;
import com.vtea.service.CustomerService;
import com.vtea.utils.DialogHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CustomerEditFormController {

    @FXML private TextField txtName;
    @FXML private TextField txtPhone;
    @FXML private TextField txtTotalPoints;
    @FXML private TextField txtRewardPoints;

    private CustomerDTO customer;
    private boolean saved = false;
    private final CustomerService customerService = new CustomerService();

    public void setCustomer(CustomerDTO customer) {
        this.customer = customer;
        if (customer != null) {
            txtName.setText(customer.getFullName());
            txtPhone.setText(customer.getPhoneNumber());
            txtTotalPoints.setText(String.valueOf(customer.getTotalAccumulatedPoints()));
            txtRewardPoints.setText(String.valueOf(customer.getRewardPoints()));
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();

        if (name.isEmpty() || phone.isEmpty() || txtTotalPoints.getText().trim().isEmpty() || txtRewardPoints.getText().trim().isEmpty()) {
            DialogHelper.showInfo("Lỗi", "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        try {
            int totalPts = Integer.parseInt(txtTotalPoints.getText().trim());
            int rewardPts = Integer.parseInt(txtRewardPoints.getText().trim());
            
            if (totalPts < 0 || rewardPts < 0) {
                DialogHelper.showInfo("Lỗi", "Điểm không được nhỏ hơn 0!");
                return;
            }
            
            customer.setTotalAccumulatedPoints(totalPts);
            customer.setRewardPoints(rewardPts);
        } catch (NumberFormatException e) {
            DialogHelper.showInfo("Lỗi", "Vui lòng nhập số hợp lệ cho điểm!");
            return;
        }

        customer.setFullName(name);
        customer.setPhoneNumber(phone);

        boolean success = customerService.updateCustomer(customer);
        if (success) {
            saved = true;
            closeStage();
        } else {
            DialogHelper.showInfo("Lỗi", "Không thể cập nhật thông tin!");
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage) txtName.getScene().getWindow();
        stage.close();
    }

    public boolean isSaved() {
        return saved;
    }
}
