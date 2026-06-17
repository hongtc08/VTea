package com.vtea.controller;

import com.vtea.model.Voucher;
import com.vtea.service.VoucherService;
import com.vtea.utils.DialogHelper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * VoucherFormController – Controller của VoucherForm.fxml.
 * Thêm mới: setVoucher(null, parentController)
 * Chỉnh sửa: setVoucher(existingVoucher, parentController)
 *
 * Lưu ý từ backend:
 *  - discountType: "FIXED" hoặc "PERCENTAGE"
 *  - usageLimit phải > 0
 *  - updateVoucher KHÔNG cho đổi code, discountType, discountValue
 */
public class VoucherFormController {

    @FXML private Label lblFormTitle;
    @FXML private Button btnClose;
    @FXML private Button btnCancel;
    @FXML private Button btnSubmit;

    @FXML private TextField txtVoucherCode;

    @FXML private HBox optionFixed;
    @FXML private HBox optionPercent;
    @FXML private RadioButton radioFixed;
    @FXML private RadioButton radioPercent;

    @FXML private Label lblDiscountValueTitle;
    @FXML private TextField txtDiscountValue;
    @FXML private DatePicker dpExpiry;
    @FXML private TextField txtMaxUses;

    private Voucher currentVoucher;        // null = thêm mới
    private VoucherController parentController;
    private final VoucherService voucherService = new VoucherService();
    private boolean isEditMode = false;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        // Click vào card chọn loại giảm → select RadioButton tương ứng
        optionFixed.setOnMouseClicked(e -> radioFixed.setSelected(true));
        optionPercent.setOnMouseClicked(e -> radioPercent.setSelected(true));

        // Đổi label giá trị giảm theo loại
        radioFixed.selectedProperty().addListener((obs, old, selected) -> {
            if (selected) {
                lblDiscountValueTitle.setText("Giá trị giảm (đ)");
                highlightOption(true);
            }
        });
        radioPercent.selectedProperty().addListener((obs, old, selected) -> {
            if (selected) {
                lblDiscountValueTitle.setText("Giá trị giảm (%)");
                highlightOption(false);
            }
        });

        // Tự động uppercase + loại ký tự không hợp lệ cho mã voucher
        txtVoucherCode.textProperty().addListener((obs, old, nv) -> {
            String clean = nv.toUpperCase().replaceAll("[^A-Z0-9]", "");
            if (!clean.equals(nv)) txtVoucherCode.setText(clean);
        });

        // Chỉ nhận số cho trường số lượt sử dụng
        txtMaxUses.textProperty().addListener((obs, old, nv) -> {
            if (!nv.matches("\\d*")) txtMaxUses.setText(nv.replaceAll("[^\\d]", ""));
        });

        // Chỉ nhận số (và dấu chấm) cho trường giá trị giảm
        txtDiscountValue.textProperty().addListener((obs, old, nv) -> {
            if (!nv.matches("\\d*\\.?\\d*")) txtDiscountValue.setText(old);
        });

        btnClose.setOnAction(e -> closeDialog());
        btnCancel.setOnAction(e -> closeDialog());
        btnSubmit.setOnAction(e -> handleSubmit());

        // Mặc định chọn FIXED
        radioFixed.setSelected(true);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public void setVoucher(Voucher voucher, VoucherController parent) {
        this.currentVoucher = voucher;
        this.parentController = parent;
        this.isEditMode = (voucher != null);

        if (!isEditMode) {
            // Chế độ thêm mới
            lblFormTitle.setText("Thêm voucher mới");
            btnSubmit.setText("Thêm voucher");
        } else {
            // Chế độ chỉnh sửa
            // Backend KHÔNG cho sửa: code, discountType, discountValue
            lblFormTitle.setText("Chỉnh sửa voucher");
            btnSubmit.setText("Lưu thay đổi");

            // Điền dữ liệu cũ
            txtVoucherCode.setText(voucher.getCode());
            txtVoucherCode.setDisable(true); // Không được sửa mã

            if ("FIXED".equalsIgnoreCase(voucher.getDiscountType())) {
                radioFixed.setSelected(true);
            } else {
                radioPercent.setSelected(true);
            }
            // Không cho sửa loại và giá trị giảm khi edit
            radioFixed.setDisable(true);
            radioPercent.setDisable(true);
            optionFixed.setDisable(true);
            optionPercent.setDisable(true);

            txtDiscountValue.setText(voucher.getDiscountValue() != null
                    ? voucher.getDiscountValue().toPlainString() : "");
            txtDiscountValue.setDisable(true); // Không được sửa

            if (voucher.getEndDate() != null) {
                dpExpiry.setValue(voucher.getEndDate().toLocalDate());
            }

            int limit = voucher.getUsageLimit();
            txtMaxUses.setText(limit >= 999999 ? "" : String.valueOf(limit));
        }
    }

    // ── Submit handler ─────────────────────────────────────────────────────────

    private void handleSubmit() {
        // 1. Validate
        String code = txtVoucherCode.getText().trim();
        if (code.isEmpty()) {
            DialogHelper.showInfo("Lỗi", "Mã voucher không được để trống.");
            return;
        }

        String discountTypeStr = radioFixed.isSelected() ? "FIXED" : "PERCENTAGE";

        String discountValueStr = txtDiscountValue.getText().trim();
        if (discountValueStr.isEmpty()) {
            DialogHelper.showInfo("Lỗi", "Giá trị giảm không được để trống.");
            return;
        }
        BigDecimal discountValue;
        try {
            discountValue = new BigDecimal(discountValueStr);
            if (discountValue.compareTo(BigDecimal.ZERO) <= 0) {
                DialogHelper.showInfo("Lỗi", "Giá trị giảm phải lớn hơn 0.");
                return;
            }
            if ("PERCENTAGE".equals(discountTypeStr) && discountValue.compareTo(BigDecimal.valueOf(100)) > 0) {
                DialogHelper.showInfo("Lỗi", "Giảm theo phần trăm không được vượt quá 100%.");
                return;
            }
        } catch (NumberFormatException ex) {
            DialogHelper.showInfo("Lỗi", "Giá trị giảm không hợp lệ.");
            return;
        }

        String maxUsesStr = txtMaxUses.getText().trim();
        int usageLimit;
        if (maxUsesStr.isEmpty()) {
            usageLimit = 999999; // Giả lập không giới hạn bằng số lớn vì DB bắt buộc > 0
        } else {
            try {
                usageLimit = Integer.parseInt(maxUsesStr);
                if (usageLimit <= 0) {
                    DialogHelper.showInfo("Lỗi", "Số lượt sử dụng phải lớn hơn 0.");
                    return;
                }
            } catch (NumberFormatException ex) {
                DialogHelper.showInfo("Lỗi", "Số lượt sử dụng không hợp lệ.");
                return;
            }
        }

        LocalDateTime endDate = null;
        if (dpExpiry.getValue() != null) {
            endDate = dpExpiry.getValue().atTime(LocalTime.of(23, 59, 59));
        }

        // 2. Tạo đối tượng Voucher
        Voucher voucher = isEditMode ? currentVoucher : new Voucher();
        voucher.setCode(code);
        voucher.setDiscountType(discountTypeStr);
        voucher.setDiscountValue(discountValue);
        voucher.setUsageLimit(usageLimit);
        voucher.setEndDate(endDate);

        if (!isEditMode) {
            // startDate mặc định là ngay bây giờ khi tạo mới
            voucher.setStartDate(LocalDateTime.now());
            voucher.setActive(true);
        }

        // 3. Gọi service
        try {
            boolean ok;
            if (isEditMode) {
                ok = voucherService.updateVoucherInfo(voucher);
                if (ok) DialogHelper.showInfo("Thành công", "Đã cập nhật voucher " + code + ".");
            } else {
                ok = voucherService.createMarketingVoucher(voucher);
                if (ok) DialogHelper.showInfo("Thành công", "Đã tạo voucher " + code + ".");
            }

            if (ok) {
                parentController.loadData();
                closeDialog();
            } else {
                DialogHelper.showInfo("Lỗi", "Không thể lưu voucher. Vui lòng thử lại.");
            }
        } catch (Exception ex) {
            DialogHelper.showInfo("Lỗi", ex.getMessage());
        }
    }

    // ── UI helpers ─────────────────────────────────────────────────────────────

    /**
     * Đổi viền của 2 card loại giảm giá để highlight cái đang chọn.
     */
    private void highlightOption(boolean fixedSelected) {
        optionFixed.setStyle(
                "-fx-background-color: #faf9f7; -fx-background-radius: 10; -fx-padding: 12 16; -fx-cursor: hand; " +
                "-fx-border-radius: 10; -fx-border-color: " + (fixedSelected ? "#4a3728" : "#e7e5e4") + ";");
        optionPercent.setStyle(
                "-fx-background-color: #faf9f7; -fx-background-radius: 10; -fx-padding: 12 16; -fx-cursor: hand; " +
                "-fx-border-radius: 10; -fx-border-color: " + (!fixedSelected ? "#4a3728" : "#e7e5e4") + ";");
    }

    private void closeDialog() {
        Stage stage = (Stage) btnSubmit.getScene().getWindow();
        stage.close();
    }
}
