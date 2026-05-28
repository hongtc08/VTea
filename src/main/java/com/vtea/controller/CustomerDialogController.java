package com.vtea.controller;

import com.vtea.utils.DialogHelper;
import com.vtea.service.CustomerService;
import com.vtea.model.Customer;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CustomerDialogController {

    @FXML private TextField txtPhone;
    @FXML private TextField txtName;

    @FXML private Button btnCheck;
    @FXML private Button btnClose;
    @FXML private Button btnCancel;
    @FXML private Button btnSubmit;

    @FXML private VBox newCustomerBox;
    @FXML private VBox existingCustomerBox;

    @FXML private Label lblNewPointPreview;
    @FXML private Label lblAvatarInitials;
    @FXML private Label lblExistingName;
    @FXML private Label lblExistingPhone;
    @FXML private Label lblCurrentPoints;
    @FXML private Label lblEquivalentValue;
    @FXML private Label lblEarnPreview;
    @FXML private Label lblUsePreview;

    @FXML private Label lblSubTotal;
    @FXML private Label lblVat;
    @FXML private Label lblTotal;

    @FXML private RadioButton radioEarn;
    @FXML private RadioButton radioUse;
    @FXML private Button btnWalkIn;

    private boolean walkIn = false;
    private final CustomerService customerService = new CustomerService();

    private Customer selectedCustomer;
    private boolean submitted = false;

    private BigDecimal orderTotal = BigDecimal.ZERO;
    private int earnPoints = 0;

    @FXML
    public void initialize() {
        hideCustomerInfo();

        btnCheck.setOnAction(event -> handleCheckCustomer());
        btnSubmit.setOnAction(event -> handleSubmit());
        btnCancel.setOnAction(event -> closeDialog());
        btnClose.setOnAction(event -> closeDialog());

        // Công việc tuần này chỉ làm tích điểm, chưa xử lý dùng điểm.
        if (radioEarn != null) {
            radioEarn.setSelected(true);
        }

        if (radioUse != null) {
            radioUse.setDisable(true);
        }
        if (btnWalkIn != null) {
            btnWalkIn.setOnAction(event -> handleWalkIn());
        }

        txtPhone.textProperty().addListener((obs, oldValue, newValue) -> {
            String phone = normalizePhone(newValue);

            if (!phone.equals(newValue)) {
                txtPhone.setText(phone);
            }
        });
    }


    /*
    Neu khach ko tich diem thi bo qua
     */
    private void handleWalkIn() {
        boolean confirmed = DialogHelper.showConfirm(
                "Xác nhận",
                "Bạn chắc chắn muốn bỏ qua tích điểm cho đơn hàng này?"
        );

        if (!confirmed) {
            return;
        }

        selectedCustomer = null;
        walkIn = true;
        submitted = true;
        closeDialog();
    }

    public boolean isWalkIn() {
        return walkIn;
    }

    public void setOrderSummary(BigDecimal subtotal, BigDecimal vat, BigDecimal total) {
        this.orderTotal = total != null ? total : BigDecimal.ZERO;
        this.earnPoints = calculateEarnPoints(this.orderTotal);

        if (lblSubTotal != null) {
            lblSubTotal.setText(formatPrice(subtotal));
        }

        if (lblVat != null) {
            lblVat.setText(formatPrice(vat));
        }

        if (lblTotal != null) {
            lblTotal.setText(formatPrice(total));
        }

        if (lblNewPointPreview != null) {
            lblNewPointPreview.setText("Khách hàng mới sẽ nhận " + earnPoints + " điểm");
        }

        if (lblEarnPreview != null) {
            lblEarnPreview.setText("Nhận thêm " + earnPoints + " điểm");
        }

        if (lblUsePreview != null) {
            lblUsePreview.setText("Chức năng dùng điểm sẽ làm sau");
        }
    }

    private void handleCheckCustomer() {
        String phone = txtPhone.getText() == null ? "" : txtPhone.getText().trim();

        if (!isValidPhone(phone)) {
            showAlert("Lỗi", "Số điện thoại phải gồm 10 chữ số.");
            return;
        }

        Customer customer = customerService.findCustomerByPhone(phone);

        if (customer != null) {
            selectedCustomer = customer;
            showExistingCustomer(customer);
        } else {
            selectedCustomer = null;
            showNewCustomerForm(phone);
        }
    }

    private void handleSubmit() {
        String phone = txtPhone.getText() == null ? "" : txtPhone.getText().trim();

        if (!isValidPhone(phone)) {
            showAlert("Lỗi", "Số điện thoại phải gồm 10 chữ số.");
            return;
        }

        // Nếu chưa có khách thì tạo mới.
        if (selectedCustomer == null) {
            String name = txtName.getText() == null ? "" : txtName.getText().trim();

            if (name.isEmpty()) {
                showAlert("Lỗi", "Vui lòng nhập tên khách hàng mới.");
                return;
            }

            Customer newCustomer = new Customer();
            newCustomer.setPhoneNumber(phone);
            newCustomer.setFullName(name);

            boolean inserted = customerService.createCustomer(newCustomer);

            if (!inserted) {
                showAlert("Lỗi", "Không thể tạo khách hàng mới.");
                return;
            }

            selectedCustomer = customerService.findCustomerByPhone(phone);

            if (selectedCustomer == null) {
                showAlert("Lỗi", "Đã tạo khách nhưng không lấy lại được thông tin khách hàng.");
                return;
            }
        }

        submitted = true;
        closeDialog();
    }

    private void showExistingCustomer(Customer customer) {
        if (newCustomerBox != null) {
            newCustomerBox.setVisible(false);
            newCustomerBox.setManaged(false);
        }

        if (existingCustomerBox != null) {
            existingCustomerBox.setVisible(true);
            existingCustomerBox.setManaged(true);
        }

        if (lblExistingName != null) {
            lblExistingName.setText(customer.getFullName());
        }

        if (lblExistingPhone != null) {
            lblExistingPhone.setText(customer.getPhoneNumber());
        }

        if (lblCurrentPoints != null) {
            lblCurrentPoints.setText(customer.getRewardPoints() + " điểm");
        }

        if (lblEquivalentValue != null) {
            lblEquivalentValue.setText("(" + formatPrice(BigDecimal.valueOf(customer.getRewardPoints() * 1000L)) + ")");
        }

        if (lblAvatarInitials != null) {
            lblAvatarInitials.setText(getInitial(customer.getFullName()));
        }
    }

    private void showNewCustomerForm(String phone) {
        if (existingCustomerBox != null) {
            existingCustomerBox.setVisible(false);
            existingCustomerBox.setManaged(false);
        }

        if (newCustomerBox != null) {
            newCustomerBox.setVisible(true);
            newCustomerBox.setManaged(true);
        }

        if (txtName != null) {
            txtName.requestFocus();
        }
    }

    private void hideCustomerInfo() {
        if (existingCustomerBox != null) {
            existingCustomerBox.setVisible(false);
            existingCustomerBox.setManaged(false);
        }

        if (newCustomerBox != null) {
            newCustomerBox.setVisible(false);
            newCustomerBox.setManaged(false);
        }
    }

    public Customer getSelectedCustomer() {
        return selectedCustomer;
    }

    public boolean isSubmitted() {
        return submitted;
    }

    public int getEarnPoints() {
        return earnPoints;
    }

    private int calculateEarnPoints(BigDecimal total) {
        if (total == null) {
            return 0;
        }

        // Quy ước: 10.000đ = 1 điểm
        return total.divide(BigDecimal.valueOf(10000), 0, RoundingMode.DOWN).intValue();
    }

    private boolean isValidPhone(String phone) {
        return phone != null && phone.matches("\\d{10}");
    }

    private String normalizePhone(String phone) {
        if (phone == null) {
            return "";
        }

        return phone.replaceAll("[^0-9]", "");
    }

    private String getInitial(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "?";
        }

        return name.trim().substring(0, 1).toUpperCase();
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) {
            return "0 đ";
        }

        return String.format("%,.0f đ", price);
    }

    private void closeDialog() {
        Stage stage = (Stage) btnSubmit.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
