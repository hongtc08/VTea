package com.vtea.controller;

import com.vtea.dto.CustomerDTO;
import com.vtea.utils.DialogHelper;
import com.vtea.service.CustomerService;
import com.vtea.model.Customer;
import com.vtea.utils.FormatUtils;
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

    @FXML private Label lblTierName;
    @FXML private Label lblTierDiscount;
    @FXML private javafx.scene.layout.HBox tierDiscountBox;

    @FXML private RadioButton radioEarn;
    @FXML private RadioButton radioUse;
    @FXML private Button btnWalkIn;

    private boolean walkIn = false;
    private final CustomerService customerService = new CustomerService();

    private CustomerDTO selectedCustomer;
    private boolean submitted = false;

    private BigDecimal orderSubtotal = BigDecimal.ZERO;
    private BigDecimal orderTotal = BigDecimal.ZERO;
    private BigDecimal tierDiscountAmount = BigDecimal.ZERO;
    private int earnPoints = 0;
    private int pointsToUse = 0;

    private static final BigDecimal POINT_CONVERSION_RATE = BigDecimal.valueOf(1000);
    private static final BigDecimal VAT_RATE = BigDecimal.valueOf(0.10);

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
            radioEarn.selectedProperty().addListener((obs, oldValue, selected) -> updatePointActionPreview());
        }

        if (radioUse != null) {
            radioUse.selectedProperty().addListener((obs, oldValue, selected) -> updatePointActionPreview());
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
        pointsToUse = 0;
        walkIn = true;
        submitted = true;
        closeDialog();
    }

    public boolean isWalkIn() {
        return walkIn;
    }

    public void setOrderSummary(BigDecimal subtotal, BigDecimal vat, BigDecimal total) {
        this.orderSubtotal = subtotal != null ? subtotal : BigDecimal.ZERO;
        this.orderTotal = total != null ? total : BigDecimal.ZERO;
        this.earnPoints = calculateEarnPoints(this.orderTotal);

        if (lblSubTotal != null) {
            lblSubTotal.setText(FormatUtils.formatPrice(subtotal));
        }

        if (lblVat != null) {
            lblVat.setText(FormatUtils.formatPrice(vat));
        }

        if (lblTotal != null) {
            lblTotal.setText(FormatUtils.formatPrice(total));
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

        updatePointActionPreview();
    }

    private void handleCheckCustomer() {
        String phone = txtPhone.getText() == null ? "" : txtPhone.getText().trim();

        if (!isValidPhone(phone)) {
            showAlert("Lỗi", "Số điện thoại phải gồm 10 chữ số.");
            return;
        }

        CustomerDTO customer = customerService.findCustomerByPhone(phone);

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

        updatePointActionPreview();
        submitted = true;
        closeDialog();
    }

    private void showExistingCustomer(CustomerDTO customer) {
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
            lblEquivalentValue.setText("(" + FormatUtils.formatPrice(BigDecimal.valueOf(customer.getRewardPoints() * 1000L)) + ")");
        }

        if (lblAvatarInitials != null) {
            lblAvatarInitials.setText(getInitial(customer.getFullName()));
        }

        if (lblTierName != null && customer.getTierName() != null) {
            lblTierName.setText(customer.getTierName());
            if (customer.getTierId() == 2) {
                lblTierName.setStyle("-fx-background-color: #E2E8F0; -fx-text-fill: #475569; -fx-padding: 2 6; -fx-background-radius: 4; -fx-font-size: 11px; -fx-font-weight: bold;");
            } else if (customer.getTierId() == 3) {
                lblTierName.setStyle("-fx-background-color: #FEF3C7; -fx-text-fill: #B45309; -fx-padding: 2 6; -fx-background-radius: 4; -fx-font-size: 11px; -fx-font-weight: bold;");
            } else if (customer.getTierId() == 4) {
                lblTierName.setStyle("-fx-background-color: #DBEAFE; -fx-text-fill: #1D4ED8; -fx-padding: 2 6; -fx-background-radius: 4; -fx-font-size: 11px; -fx-font-weight: bold;");
            } else {
                lblTierName.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #6B7280; -fx-padding: 2 6; -fx-background-radius: 4; -fx-font-size: 11px; -fx-font-weight: bold;");
            }
        }

        updatePointActionPreview();
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

        updatePointActionPreview();
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

    public CustomerDTO getSelectedCustomer() {
        return selectedCustomer;
    }

    public boolean isSubmitted() {
        return submitted;
    }

    public int getEarnPoints() {
        return earnPoints;
    }

    public int getPointsToUse() {
        return pointsToUse;
    }

    public BigDecimal getTierDiscountAmount() {
        return tierDiscountAmount;
    }

    private void updatePointActionPreview() {
        calculateTierDiscount();
        
        boolean canUsePoints = selectedCustomer != null && calculateMaxUsablePoints(selectedCustomer) > 0;
        updateRadioUseState(canUsePoints);

        if (selectedCustomer == null || radioUse == null || !radioUse.isSelected()) {
            updatePreviewForEarningOnly(canUsePoints);
        } else {
            updatePreviewForUsingPoints();
        }
    }

    private void calculateTierDiscount() {
        if (selectedCustomer != null && selectedCustomer.getDiscountPercent() > 0) {
            BigDecimal discountPercent = BigDecimal.valueOf(selectedCustomer.getDiscountPercent());
            tierDiscountAmount = orderSubtotal.multiply(discountPercent).divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);
            
            if (tierDiscountBox != null) {
                tierDiscountBox.setVisible(true);
                tierDiscountBox.setManaged(true);
            }
            if (lblTierDiscount != null) {
                lblTierDiscount.setText("-" + FormatUtils.formatPrice(tierDiscountAmount) + " (" + selectedCustomer.getDiscountPercent() + "%)");
            }
        } else {
            tierDiscountAmount = BigDecimal.ZERO;
            if (tierDiscountBox != null) {
                tierDiscountBox.setVisible(false);
                tierDiscountBox.setManaged(false);
            }
        }
    }

    private void updateRadioUseState(boolean canUsePoints) {
        if (radioUse != null) {
            radioUse.setDisable(!canUsePoints);
            if (!canUsePoints && radioUse.isSelected() && radioEarn != null) {
                radioEarn.setSelected(true);
            }
        }
    }

    private void updatePreviewForEarningOnly(boolean canUsePoints) {
        pointsToUse = 0;
        
        BigDecimal amountAfterTierDiscount = orderSubtotal.subtract(tierDiscountAmount);
        if (amountAfterTierDiscount.compareTo(BigDecimal.ZERO) < 0) amountAfterTierDiscount = BigDecimal.ZERO;
        
        BigDecimal vatAfterDiscount = amountAfterTierDiscount.multiply(VAT_RATE);
        BigDecimal totalAfterDiscount = amountAfterTierDiscount.add(vatAfterDiscount);
        earnPoints = calculateEarnPoints(totalAfterDiscount);

        updateLabels(vatAfterDiscount, totalAfterDiscount, 
                "Nhận thêm " + earnPoints + " điểm", 
                canUsePoints ? "Có thể dùng tối đa " + calculateMaxUsablePoints(selectedCustomer) + " điểm" : "Không có điểm khả dụng cho đơn này");
    }

    private void updatePreviewForUsingPoints() {
        pointsToUse = calculateMaxUsablePoints(selectedCustomer);
        BigDecimal discount = POINT_CONVERSION_RATE.multiply(BigDecimal.valueOf(pointsToUse));
        BigDecimal amountAfterDiscount = orderSubtotal.subtract(tierDiscountAmount).subtract(discount);

        if (amountAfterDiscount.compareTo(BigDecimal.ZERO) < 0) {
            amountAfterDiscount = BigDecimal.ZERO;
        }

        BigDecimal vatAfterDiscount = amountAfterDiscount.multiply(VAT_RATE);
        BigDecimal totalAfterDiscount = amountAfterDiscount.add(vatAfterDiscount);
        earnPoints = calculateEarnPoints(totalAfterDiscount);

        updateLabels(vatAfterDiscount, totalAfterDiscount, 
                "Sau khi trừ điểm sẽ nhận " + earnPoints + " điểm", 
                "Dùng " + pointsToUse + " điểm, giảm " + FormatUtils.formatPrice(discount));
    }

    private void updateLabels(BigDecimal vat, BigDecimal total, String earnText, String useText) {
        if (lblVat != null) {
            lblVat.setText(FormatUtils.formatPrice(vat));
        }

        if (lblTotal != null) {
            lblTotal.setText(FormatUtils.formatPrice(total));
        }

        if (lblEarnPreview != null) {
            lblEarnPreview.setText(earnText);
        }

        if (lblUsePreview != null) {
            lblUsePreview.setText(useText);
        }
    }

    private int calculateMaxUsablePoints(CustomerDTO customer) {
        if (customer == null || orderSubtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }

        BigDecimal subtotalAfterTier = orderSubtotal.subtract(tierDiscountAmount);
        if (subtotalAfterTier.compareTo(BigDecimal.ZERO) <= 0) return 0;

        int maxByOrder = subtotalAfterTier.divide(POINT_CONVERSION_RATE, 0, RoundingMode.DOWN).intValue();
        return Math.min(customer.getRewardPoints(), maxByOrder);
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



    private void closeDialog() {
        Stage stage = (Stage) btnSubmit.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        DialogHelper.showInfo(title, message);
    }
}
