package com.vtea.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * VoucherFormController – Controller của VoucherForm.fxml.
 * Dùng để THÊM MỚI hoặc CHỈNH SỬA một voucher.
 *
 * Cách gọi:
 *  - Thêm mới: setVoucher(null, parentController)
 *  - Chỉnh sửa: setVoucher(voucherDTO, parentController)
 *
 * Sau khi lưu thành công:
 *  parentController.loadData() được gọi để reload danh sách.
 */
public class VoucherFormController {

    // ── FXML bindings ─────────────────────────────────────────────────────────
    @FXML private Label lblFormTitle;        // "Thêm voucher mới" / "Chỉnh sửa voucher"
    @FXML private Button btnClose;
    @FXML private Button btnCancel;
    @FXML private Button btnSubmit;          // "Lưu voucher"

    @FXML private TextField txtVoucherCode;

    @FXML private HBox optionFixed;          // Card chọn loại giảm tiền
    @FXML private HBox optionPercent;        // Card chọn loại giảm %
    @FXML private RadioButton radioFixed;    // Trong optionFixed
    @FXML private RadioButton radioPercent;  // Trong optionPercent

    @FXML private Label lblDiscountValueTitle; // "Giá trị giảm (đ)" / "Giá trị giảm (%)"
    @FXML private TextField txtDiscountValue;
    @FXML private DatePicker dpExpiry;
    @FXML private TextField txtMaxUses;

    // ── State ─────────────────────────────────────────────────────────────────
    private Object currentVoucher;          // TODO: thay Object → VoucherDTO (null = thêm mới)
    private VoucherController parentController;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        // TODO: Khi chọn radioFixed → lblDiscountValueTitle.setText("Giá trị giảm (đ)")
        //       Khi chọn radioPercent → lblDiscountValueTitle.setText("Giá trị giảm (%)")
        // radioFixed.selectedProperty().addListener((obs, old, selected) -> {
        //     if (selected) lblDiscountValueTitle.setText("Giá trị giảm (đ)");
        // });
        // radioPercent.selectedProperty().addListener((obs, old, selected) -> {
        //     if (selected) lblDiscountValueTitle.setText("Giá trị giảm (%)");
        // });

        // TODO: Click vào card optionFixed/optionPercent → chọn RadioButton tương ứng
        // optionFixed.setOnMouseClicked(e -> radioFixed.setSelected(true));
        // optionPercent.setOnMouseClicked(e -> radioPercent.setSelected(true));

        // TODO: Validate txtVoucherCode: tự động uppercase + loại bỏ ký tự đặc biệt
        // txtVoucherCode.textProperty().addListener((obs, old, nv) ->
        //     txtVoucherCode.setText(nv.toUpperCase().replaceAll("[^A-Z0-9]", "")));

        // TODO: Validate txtMaxUses: chỉ nhận số nguyên dương
        // txtMaxUses.textProperty().addListener((obs, old, nv) ->
        //     { if (!nv.matches("\\d*")) txtMaxUses.setText(nv.replaceAll("[^\\d]", "")); });

        // Gán sự kiện nút
        btnClose.setOnAction(e -> closeDialog());
        btnCancel.setOnAction(e -> closeDialog());
        btnSubmit.setOnAction(e -> handleSubmit());
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Truyền dữ liệu voucher vào form.
     * @param voucherDTO null = thêm mới; != null = chỉnh sửa
     * @param parent     để gọi loadData() sau khi lưu
     */
    public void setVoucher(Object voucherDTO, VoucherController parent) {
        this.currentVoucher = voucherDTO;
        this.parentController = parent;

        if (voucherDTO == null) {
            // TODO: Chế độ thêm mới – giữ form trống
            // lblFormTitle.setText("Thêm voucher mới");
            // btnSubmit.setText("Thêm voucher");
            // radioFixed.setSelected(true);  ← mặc định chọn "Số tiền cố định"
        } else {
            // TODO: Chế độ chỉnh sửa – điền dữ liệu cũ vào form
            // lblFormTitle.setText("Chỉnh sửa voucher");
            // btnSubmit.setText("Lưu thay đổi");
            // txtVoucherCode.setText(voucherDTO.getCode());
            // if ("FIXED".equals(voucherDTO.getDiscountType())) radioFixed.setSelected(true);
            // else radioPercent.setSelected(true);
            // txtDiscountValue.setText(voucherDTO.getDiscountValue().toPlainString());
            // dpExpiry.setValue(voucherDTO.getExpiryDate());
            // txtMaxUses.setText(voucherDTO.getMaxUses() == null ? "" : String.valueOf(voucherDTO.getMaxUses()));
        }
    }

    // ── Handlers ──────────────────────────────────────────────────────────────

    /**
     * Validate form và lưu voucher (thêm mới hoặc cập nhật).
     */
    private void handleSubmit() {
        // TODO: 1. Validate các trường
        //   - txtVoucherCode không rỗng và chưa tồn tại trong DB (khi thêm mới)
        //   - txtDiscountValue là số hợp lệ và > 0
        //   - Nếu radioPercent: discountValue <= 100
        //   - dpExpiry không null và >= ngày hiện tại
        //   - txtMaxUses (nếu nhập) là số nguyên > 0
        //
        // TODO: 2. Tạo đối tượng VoucherDTO từ form
        //   VoucherDTO dto = new VoucherDTO();
        //   dto.setCode(txtVoucherCode.getText().trim());
        //   dto.setDiscountType(radioFixed.isSelected() ? "FIXED" : "PERCENT");
        //   dto.setDiscountValue(new BigDecimal(txtDiscountValue.getText().trim()));
        //   dto.setExpiryDate(dpExpiry.getValue());
        //   dto.setMaxUses(txtMaxUses.getText().isBlank() ? null : Integer.parseInt(txtMaxUses.getText().trim()));
        //
        // TODO: 3. Gọi service
        //   if (currentVoucher == null) {
        //       boolean ok = voucherService.createVoucher(dto);
        //   } else {
        //       dto.setVoucherId(currentVoucher.getVoucherId());
        //       boolean ok = voucherService.updateVoucher(dto);
        //   }
        //
        // TODO: 4. Nếu thành công:
        //   DialogHelper.showInfo("Thành công", "Đã lưu voucher.");
        //   parentController.loadData();
        //   closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) btnSubmit.getScene().getWindow();
        stage.close();
    }
}
