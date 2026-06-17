package com.vtea.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * VoucherAppliedCardController – Controller của VoucherAppliedCard.fxml.
 *
 * Hiển thị thông tin voucher đã được áp dụng vào đơn hàng bên trong CustomerDialog.
 * Được tạo động bởi CustomerDialogController.
 *
 * Cách sử dụng:
 *   FXMLLoader loader = new FXMLLoader(...VoucherAppliedCard.fxml);
 *   HBox card = loader.load();
 *   VoucherAppliedCardController ctrl = loader.getController();
 *   ctrl.setData(voucherDTO, appliedAmount, onRemoveCallback);
 *   appliedVouchersContainer.getChildren().add(card);
 */
public class VoucherAppliedCardController {

    // ── FXML bindings ─────────────────────────────────────────────────────────
    @FXML private Label lblVoucherCode;       // Mã voucher  (VD: "WELCOME50K")
    @FXML private Label lblDiscountSummary;   // Tóm tắt dưới tên (VD: "-50.000đ" nhỏ màu cam)
    @FXML private Label lblDiscountAmount;    // Số tiền giảm lớn bên phải (VD: "-50.000đ" màu teal)
    @FXML private Button btnRemove;           // Nút × để xóa voucher khỏi đơn

    // ── State ─────────────────────────────────────────────────────────────────
    private Object currentVoucher;            // TODO: thay Object → VoucherDTO
    private Runnable onRemoveCallback;        // Callback về CustomerDialogController khi xóa

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        btnRemove.setOnAction(e -> handleRemove());
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Điền dữ liệu voucher đã áp dụng vào card.
     *
     * @param voucherDTO      TODO: thay Object → VoucherDTO
     * @param appliedAmount   số tiền giảm thực tế đã tính (có thể khác discountValue nếu đơn < giảm)
     * @param onRemove        Runnable sẽ gọi khi người dùng bấm × để xóa card này
     */
    public void setData(Object voucherDTO, java.math.BigDecimal appliedAmount, Runnable onRemove) {
        this.currentVoucher = voucherDTO;
        this.onRemoveCallback = onRemove;

        // TODO: lblVoucherCode.setText(voucherDTO.getCode());

        // TODO: Tóm tắt giảm giá nhỏ (dưới tên voucher):
        //   if ("FIXED".equals(voucherDTO.getDiscountType()))
        //       lblDiscountSummary.setText("-" + FormatUtils.formatPrice(voucherDTO.getDiscountValue()));
        //   else
        //       lblDiscountSummary.setText("-" + voucherDTO.getDiscountValue().intValue() + "%");

        // TODO: Số tiền giảm thực tế (lớn, bên phải):
        //   lblDiscountAmount.setText("-" + FormatUtils.formatPrice(appliedAmount));
    }

    // ── Handlers ──────────────────────────────────────────────────────────────

    /**
     * Gọi callback để CustomerDialogController xóa voucher này khỏi đơn và cập nhật tổng tiền.
     */
    private void handleRemove() {
        if (onRemoveCallback != null) {
            onRemoveCallback.run();
        }
    }
}
