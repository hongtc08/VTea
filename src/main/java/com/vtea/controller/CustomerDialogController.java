package com.vtea.controller;

import com.vtea.dto.CustomerCheckoutResult;
import com.vtea.model.Customer;
import com.vtea.service.CustomerService;
import com.vtea.utils.DialogHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;

public class CustomerDialogController {

    private final CustomerService customerService = new CustomerService();

    @FXML private Button btnClose;
    @FXML private TextField txtPhone;
    @FXML private Button btnCheck;
    @FXML private VBox newCustomerBox;
    @FXML private TextField txtName;
    @FXML private Label lblNewPointPreview;
    @FXML private VBox existingCustomerBox;
    @FXML private Label lblAvatarInitials;
    @FXML private Label lblExistingName;
    @FXML private Label lblExistingPhone;
    @FXML private Label lblCurrentPoints;
    @FXML private Label lblEquivalentValue;
    @FXML private ToggleGroup pointActionGroup;
    @FXML private RadioButton radioEarn;
    @FXML private RadioButton radioUse;
    @FXML private Label lblEarnPreview;
    @FXML private Label lblUsePreview;
    @FXML private Label lblSubTotal;
    @FXML private Label lblVat;
    @FXML private Label lblTotal;
    @FXML private Button btnWalkIn;
    @FXML private Button btnCancel;
    @FXML private Button btnSubmit;

    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal vat = BigDecimal.ZERO;
    private BigDecimal total = BigDecimal.ZERO;

    private CustomerCheckoutResult result = CustomerCheckoutResult.cancelled();
    private Customer currentCustomer;
    private boolean phoneChecked;
    private boolean isNewCustomer;

    @FXML
    public void initialize() {
        hideCustomerSections();

        btnClose.setOnAction(e -> closeDialog());
        btnCancel.setOnAction(e -> closeDialog());
        btnCheck.setOnAction(e -> handleCheckPhone());
        btnSubmit.setOnAction(e -> handleSubmit());
        if (btnWalkIn != null) {
            btnWalkIn.setOnAction(e -> handleWalkIn());
        }

        if (pointActionGroup != null) {
            pointActionGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> updatePointPreviews());
        }
    }

    public void initCheckoutData(BigDecimal subtotal, BigDecimal vat, BigDecimal total) {
        this.subtotal = subtotal != null ? subtotal : BigDecimal.ZERO;
        this.vat = vat != null ? vat : BigDecimal.ZERO;
        this.total = total != null ? total : BigDecimal.ZERO;
        updateSummaryLabels(this.subtotal, this.vat, this.total);
    }

    public CustomerCheckoutResult getResult() {
        return result;
    }

    private void handleCheckPhone() {
        String phone = normalizePhone(txtPhone.getText());
        if (phone.isEmpty()) {
            showInlineError("Vui lòng nhập số điện thoại!");
            return;
        }
        if (!isValidPhone(phone)) {
            showInlineError("Số điện thoại không hợp lệ (9-11 chữ số)!");
            return;
        }

        txtPhone.setText(phone);
        currentCustomer = customerService.findByPhone(phone);
        phoneChecked = true;
        isNewCustomer = currentCustomer == null;

        if (isNewCustomer) {
            showNewCustomerSection();
        } else {
            showExistingCustomerSection(currentCustomer);
        }
    }

    private void handleWalkIn() {
        result = CustomerCheckoutResult.walkIn(total);
        closeDialog();
    }

    private void handleSubmit() {
        if (!phoneChecked) {
            showInlineError("Vui lòng nhấn \"Kiểm tra\" số điện thoại trước!");
            return;
        }

        String phone = normalizePhone(txtPhone.getText());
        if (phone.isEmpty() || !isValidPhone(phone)) {
            showInlineError("Số điện thoại không hợp lệ!");
            return;
        }

        if (isNewCustomer) {
            submitNewCustomer(phone);
        } else {
            submitExistingCustomer();
        }
    }

    private void submitNewCustomer(String phone) {
        String name = txtName.getText() != null ? txtName.getText().trim() : "";
        if (name.isEmpty()) {
            showInlineError("Vui lòng nhập tên khách hàng!");
            txtName.requestFocus();
            return;
        }

        int customerId = customerService.registerCustomer(name, phone);
        if (customerId <= 0) {
            showInlineError("Không thể đăng ký khách hàng. Vui lòng thử lại!");
            return;
        }

        int pointsToEarn = customerService.calculateEarnablePoints(subtotal);
        BigDecimal finalTotal = customerService.calculateTotal(subtotal, 0);

        result = CustomerCheckoutResult.confirmed(customerId, false, 0, pointsToEarn, finalTotal);
        closeDialog();
    }

    private void submitExistingCustomer() {
        if (currentCustomer == null || currentCustomer.getCustomerId() == null) {
            showInlineError("Không tìm thấy thông tin khách hàng!");
            return;
        }

        boolean usePoints = radioUse != null && radioUse.isSelected();
        int maxUsable = customerService.calculateMaxUsablePoints(currentCustomer.getRewardPoints(), subtotal);
        int pointsUsed = usePoints ? maxUsable : 0;
        int pointsToEarn = usePoints ? 0 : customerService.calculateEarnablePoints(
                subtotal.subtract(customerService.calculateDiscountAmount(pointsUsed)).max(BigDecimal.ZERO)
        );
        BigDecimal finalTotal = customerService.calculateTotal(subtotal, pointsUsed);

        result = CustomerCheckoutResult.confirmed(
                currentCustomer.getCustomerId(),
                usePoints,
                pointsUsed,
                pointsToEarn,
                finalTotal
        );
        closeDialog();
    }

    private void showNewCustomerSection() {
        existingCustomerBox.setVisible(false);
        existingCustomerBox.setManaged(false);
        newCustomerBox.setVisible(true);
        newCustomerBox.setManaged(true);

        int earnable = customerService.calculateEarnablePoints(subtotal);
        lblNewPointPreview.setText("Khách hàng mới sẽ nhận " + earnable + " điểm");
        txtName.clear();
        txtName.requestFocus();
    }

    private void showExistingCustomerSection(Customer customer) {
        newCustomerBox.setVisible(false);
        newCustomerBox.setManaged(false);
        existingCustomerBox.setVisible(true);
        existingCustomerBox.setManaged(true);

        String name = customer.getFullName() != null ? customer.getFullName() : "";
        lblAvatarInitials.setText(getInitials(name));
        lblExistingName.setText(name);
        lblExistingPhone.setText(customer.getPhoneNumber());

        int points = customer.getRewardPoints();
        lblCurrentPoints.setText(points + " điểm");
        lblEquivalentValue.setText("(" + customerService.formatPointsValue(points) + ")");

        if (radioEarn != null) {
            radioEarn.setSelected(true);
        }

        int maxUsable = customerService.calculateMaxUsablePoints(points, subtotal);
        if (radioUse != null) {
            radioUse.setDisable(maxUsable <= 0);
            if (maxUsable <= 0 && radioUse.isSelected() && radioEarn != null) {
                radioEarn.setSelected(true);
            }
        }

        updatePointPreviews();
    }

    private void updatePointPreviews() {
        if (currentCustomer == null) {
            return;
        }

        int maxUsable = customerService.calculateMaxUsablePoints(currentCustomer.getRewardPoints(), subtotal);
        int earnable = customerService.calculateEarnablePoints(subtotal);
        boolean usePoints = radioUse != null && radioUse.isSelected() && maxUsable > 0;

        if (lblEarnPreview != null) {
            lblEarnPreview.setText("Nhận thêm " + earnable + " điểm");
        }

        if (lblUsePreview != null) {
            int pointsToUse = usePoints ? maxUsable : 0;
            BigDecimal discount = customerService.calculateDiscountAmount(pointsToUse);
            lblUsePreview.setText("Giảm " + pointsToUse + " điểm (-" + customerService.formatMoney(discount) + ")");
        }

        if (usePoints) {
            BigDecimal finalTotal = customerService.calculateTotal(subtotal, maxUsable);
            BigDecimal discountedSubtotal = subtotal.subtract(customerService.calculateDiscountAmount(maxUsable)).max(BigDecimal.ZERO);
            BigDecimal newVat = customerService.calculateVat(discountedSubtotal);
            updateSummaryLabels(discountedSubtotal, newVat, finalTotal);
        } else {
            updateSummaryLabels(subtotal, vat, total);
        }
    }

    private void updateSummaryLabels(BigDecimal displaySubtotal, BigDecimal displayVat, BigDecimal displayTotal) {
        lblSubTotal.setText(customerService.formatMoney(displaySubtotal));
        lblVat.setText(customerService.formatMoney(displayVat));
        lblTotal.setText(customerService.formatMoney(displayTotal));
    }

    private void hideCustomerSections() {
        newCustomerBox.setVisible(false);
        newCustomerBox.setManaged(false);
        existingCustomerBox.setVisible(false);
        existingCustomerBox.setManaged(false);
    }

    private String normalizePhone(String phone) {
        if (phone == null) {
            return "";
        }
        return phone.trim().replaceAll("\\s+", "");
    }

    private boolean isValidPhone(String phone) {
        return phone.matches("\\d{9,11}");
    }

    private String getInitials(String name) {
        if (name == null || name.isBlank()) {
            return "?";
        }
        return name.substring(0, 1).toUpperCase();
    }

    private void showInlineError(String message) {
        DialogHelper.showInfo("Thông báo", message);
    }

    private void closeDialog() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
}
