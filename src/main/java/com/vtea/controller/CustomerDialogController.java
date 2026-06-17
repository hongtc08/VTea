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
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

import com.vtea.model.Voucher;
import com.vtea.service.VoucherService;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;

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

    // ── Voucher section FXML bindings ────────────────────────────────────────
    @FXML private javafx.scene.layout.HBox voucherToggleHeader;   // Header bấm để mở/đóng
    @FXML private javafx.scene.layout.VBox voucherExpandBox;      // Nội dung mở rộng
    @FXML private javafx.scene.layout.StackPane voucherBadgePane; // Badge số voucher
    @FXML private Label lblAppliedCount;                          // Số trong badge
    @FXML private org.kordamp.ikonli.javafx.FontIcon iconVoucherArrow; // Icon mũi tên
    @FXML private javafx.scene.control.TextField txtVoucherCode;  // Ô nhập mã
    @FXML private Button btnApplyVoucher;                         // Nút "Áp dụng"
    @FXML private javafx.scene.layout.VBox appliedVouchersContainer; // Chứa các card
    @FXML private javafx.scene.layout.HBox voucherDiscountBox;    // Dòng giảm giá trong summary
    @FXML private Label lblVoucherDiscount;                       // Giá trị giảm (VD: -50.000đ)
    @FXML private Label lblVoucherDiscountTitle;                  // "Voucher (1):"

    private boolean walkIn = false;
    private final CustomerService customerService = new CustomerService();

    private CustomerDTO selectedCustomer;
    private boolean submitted = false;

    private BigDecimal orderSubtotal = BigDecimal.ZERO;
    private BigDecimal orderTotal = BigDecimal.ZERO;
    private BigDecimal tierDiscountAmount = BigDecimal.ZERO;
    private BigDecimal totalVoucherDiscount = BigDecimal.ZERO;
    private int earnPoints = 0;
    private int pointsToUse = 0;

    private final VoucherService voucherService = new VoucherService();
    private List<Voucher> appliedVouchers = new ArrayList<>();

    private static final BigDecimal POINT_CONVERSION_RATE = BigDecimal.valueOf(1000);
    private static final BigDecimal VAT_RATE = BigDecimal.valueOf(0.10);

    @FXML
    public void initialize() {
        hideCustomerInfo();

        btnCheck.setOnAction(event -> handleCheckCustomer());
        btnSubmit.setOnAction(event -> handleSubmit());
        btnCancel.setOnAction(event -> closeDialog());
        btnClose.setOnAction(event -> closeDialog());
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

        // ── Voucher section setup ───────────────────────────────────────────
        voucherToggleHeader.setOnMouseClicked(e -> toggleVoucherSection());
        btnApplyVoucher.setOnAction(e -> handleApplyVoucher());
        txtVoucherCode.setOnAction(e -> handleApplyVoucher());

        txtPhone.textProperty().addListener((obs, oldValue, newValue) -> {
            String phone = normalizePhone(newValue);

            if (!phone.equals(newValue)) {
                txtPhone.setText(phone);
            }
        });

        // Tự động áp dụng Welcome Voucher khi nhập xong tên khách mới (mất focus)
        txtName.focusedProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue && selectedCustomer == null) {
                String name = txtName.getText() == null ? "" : txtName.getText().trim();
                String phone = txtPhone.getText() == null ? "" : txtPhone.getText().trim();
                if (!name.isEmpty() && isValidPhone(phone)) {
                    autoApplyWelcomeVoucher(phone);
                }
            }
        });
    }


    /*
    Neu khach ko tich diem thi bo qua -> Khách vãng lai
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

    public String getAppliedVoucherCode() {
        if (appliedVouchers != null && !appliedVouchers.isEmpty()) {
            return appliedVouchers.get(0).getCode();
        }
        return null;
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
        
        BigDecimal amountAfterTierDiscount = orderSubtotal.subtract(tierDiscountAmount).subtract(totalVoucherDiscount);
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
        BigDecimal amountAfterDiscount = orderSubtotal.subtract(tierDiscountAmount).subtract(totalVoucherDiscount).subtract(discount);

        if (amountAfterDiscount.compareTo(BigDecimal.ZERO) < 0) {
            amountAfterDiscount = BigDecimal.ZERO;
        }

        BigDecimal vatAfterDiscount = amountAfterDiscount.multiply(VAT_RATE);
        BigDecimal totalAfterDiscount = amountAfterDiscount.add(vatAfterDiscount);
        earnPoints = 0;

        updateLabels(vatAfterDiscount, totalAfterDiscount, 
                "Sau khi trừ điểm sẽ nhận 0 điểm", 
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

        BigDecimal subtotalAfterTier = orderSubtotal.subtract(tierDiscountAmount).subtract(totalVoucherDiscount);
        if (subtotalAfterTier.compareTo(BigDecimal.ZERO) <= 0) return 0;

        BigDecimal maxAllowedPointDiscount = subtotalAfterTier.multiply(new BigDecimal("0.50"));
        int maxByOrder = maxAllowedPointDiscount.divide(POINT_CONVERSION_RATE, 0, RoundingMode.DOWN).intValue();
        
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

    // ══════════════════════════════════════════════════════════════════════════
    // VOUCHER
    // ══════════════════════════════════════════════════════════════════════════

    private void autoApplyWelcomeVoucher(String phone) {
        try {
            String expectedCode = "NEW_" + phone;
            boolean alreadyApplied = appliedVouchers.stream()
                .anyMatch(v -> v.getCode().equalsIgnoreCase(expectedCode));
            if (alreadyApplied) return;

            // Gọi service tạo mã (backend đã tự handle việc kiểm tra xem sđt này đã từng tạo chưa)
            String code = voucherService.createWelcomeVoucher(phone);
            
            // Tự điền vào ô nhập và gọi hàm áp dụng
            txtVoucherCode.setText(code);
            handleApplyVoucher();
            
            DialogHelper.showInfo("Tặng Voucher", "Hệ thống đã tự động tặng và áp dụng voucher chào mừng cho khách hàng mới!");
        } catch (Exception e) {
            System.err.println("Không thể tạo welcome voucher: " + e.getMessage());
        }
    }

    /**
     * Mở/đóng section nhập voucher.
     * Đổi icon mũi tên (chevron-down ↔ chevron-up) và toggle voucherExpandBox.
     */
    private void toggleVoucherSection() {
        boolean isOpen = voucherExpandBox.isVisible();
        voucherExpandBox.setVisible(!isOpen);
        voucherExpandBox.setManaged(!isOpen);
        iconVoucherArrow.setIconLiteral(isOpen ? "fth-chevron-up" : "fth-chevron-down");
    }

    /**
     * Gọi khi nhấn "Áp dụng":
     */
    private void handleApplyVoucher() {
        String code = txtVoucherCode.getText().trim().toUpperCase();
        if (code.isEmpty()) { 
            showAlert("Lỗi", "Vui lòng nhập mã voucher."); 
            return; 
        }

        // Kiem tra xem voucher da duoc ap dung chua
        boolean alreadyApplied = appliedVouchers.stream()
            .anyMatch(v -> v.getCode().equalsIgnoreCase(code));
        if (alreadyApplied) {
            showAlert("Lỗi", "Mã voucher này đã được áp dụng cho đơn hàng!");
            return;
        }

        try {
            BigDecimal appliedAmount = voucherService.calculateDiscount(code, orderSubtotal);
            com.vtea.dao.VoucherDAO dao = new com.vtea.dao.VoucherDAO();
            Voucher voucher = dao.getVoucherByCode(code);
            
            if (voucher == null) {
                showAlert("Lỗi", "Mã voucher không tồn tại.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vtea/view/VoucherAppliedCard.fxml"));
            HBox card = loader.load();
            VoucherAppliedCardController ctrl = loader.getController();
            ctrl.setData(voucher, appliedAmount, () -> removeAppliedVoucher(voucher, card));
            appliedVouchersContainer.getChildren().add(card);

            appliedVouchers.add(voucher);
            txtVoucherCode.clear();
            updateVoucherBadge();
            recalculateTotals();
        } catch (Exception ex) {
            showAlert("Không hợp lệ", ex.getMessage());
        }
    }

    /**
     * Xóa 1 voucher khỏi đơn khi người dùng bấm × trên VoucherAppliedCard.
     */
    private void removeAppliedVoucher(Voucher voucher, javafx.scene.Node card) {
        appliedVouchers.remove(voucher);
        appliedVouchersContainer.getChildren().remove(card);
        updateVoucherBadge();
        recalculateTotals();
    }

    /**
     * Cập nhật badge số lượng voucher đang áp dụng.
     */
    private void updateVoucherBadge() {
        int count = appliedVouchers.size();
        if (count > 0) {
            lblAppliedCount.setText(String.valueOf(count));
            voucherBadgePane.setVisible(true);
            voucherBadgePane.setManaged(true);
        } else {
            voucherBadgePane.setVisible(false);
            voucherBadgePane.setManaged(false);
        }
    }

    private BigDecimal calculateVoucherDiscount(Voucher voucher) {
        try {
            return voucherService.calculateDiscount(voucher.getCode(), orderSubtotal);
        } catch (Exception ex) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * Tính lại VAT và tổng tiền sau khi thêm/xóa voucher, cập nhật các Label trong summary.
     */
    private void recalculateTotals() {
        totalVoucherDiscount = appliedVouchers.stream()
            .map(v -> calculateVoucherDiscount(v))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalVoucherDiscount.compareTo(BigDecimal.ZERO) > 0) {
            lblVoucherDiscountTitle.setText("Voucher (" + appliedVouchers.size() + "):");
            lblVoucherDiscount.setText("-" + FormatUtils.formatPrice(totalVoucherDiscount));
            voucherDiscountBox.setVisible(true);
            voucherDiscountBox.setManaged(true);
        } else {
            voucherDiscountBox.setVisible(false);
            voucherDiscountBox.setManaged(false);
        }

        updatePointActionPreview();
    }

}
