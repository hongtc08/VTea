package com.vtea.controller;

import com.vtea.model.Voucher;
import com.vtea.service.VoucherService;
import com.vtea.utils.DialogHelper;
import com.vtea.utils.FormatUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.javafx.FontIcon;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * VoucherRowController – Controller cho MỖI DÒNG trong danh sách voucher.
 * Được VoucherController khởi tạo động qua FXMLLoader.
 */
public class VoucherRowController {

    @FXML private Label lblCode;
    @FXML private FontIcon iconDiscountType;
    @FXML private Label lblDiscount;
    @FXML private FontIcon iconExpiry;
    @FXML private Label lblExpiry;
    @FXML private ProgressBar progressUsage;
    @FXML private Label lblUsageCount;
    @FXML private StackPane badgeStatus;
    @FXML private Label lblStatus;
    @FXML private FontIcon iconToggle;
    @FXML private Button btnToggle;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    private Voucher currentVoucher;
    private VoucherController parentController;
    private final VoucherService voucherService = new VoucherService();

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ── Public API ────────────────────────────────────────────────────────────

    public void setData(Voucher voucher, VoucherController parent) {
        this.currentVoucher = voucher;
        this.parentController = parent;

        // Mã voucher
        lblCode.setText(voucher.getCode());

        // Loại & giá trị giảm
        if ("FIXED".equalsIgnoreCase(voucher.getDiscountType())) {
            iconDiscountType.setIconLiteral("fth-tag");
            iconDiscountType.setIconColor(javafx.scene.paint.Color.valueOf("#F59E0B"));
            lblDiscount.setText(FormatUtils.formatPrice(voucher.getDiscountValue()));
            lblDiscount.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #F59E0B;");
        } else {
            // PERCENTAGE
            iconDiscountType.setIconLiteral("mdi2p-percent");
            iconDiscountType.setIconColor(javafx.scene.paint.Color.valueOf("#12b6a2"));
            lblDiscount.setText(voucher.getDiscountValue().intValue() + "%");
            lblDiscount.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #12b6a2;");
        }

        // Ngày hết hạn
        boolean isExpired = false;
        if (voucher.getEndDate() != null) {
            isExpired = LocalDateTime.now().isAfter(voucher.getEndDate());
            lblExpiry.setText(voucher.getEndDate().format(DATE_FMT));
            if (isExpired) {
                lblExpiry.setStyle("-fx-font-size: 13px; -fx-text-fill: #ef4444;");
                iconExpiry.setIconColor(javafx.scene.paint.Color.valueOf("#ef4444"));
            } else {
                lblExpiry.setStyle("-fx-font-size: 13px; -fx-text-fill: #57534e;");
                iconExpiry.setIconColor(javafx.scene.paint.Color.valueOf("#78716c"));
            }
        } else {
            lblExpiry.setText("Không giới hạn");
        }

        // Lượt dùng + thanh tiến trình
        int used  = voucher.getUsedCount();
        int limit = voucher.getUsageLimit();
        boolean unlimited = (limit >= 999999);

        if (unlimited) {
            lblUsageCount.setText(used + " / ∞");
            progressUsage.setProgress(0);
        } else {
            lblUsageCount.setText(used + " / " + limit);
            double ratio = (double) used / limit;
            progressUsage.setProgress(Math.min(ratio, 1.0));
        }

        // Trạng thái badge
        boolean active = voucher.isActive() && !isExpired && (unlimited || used < limit);
        updateStatusBadge(active);
        updateToggleIcon(voucher.isActive());

        // Gán sự kiện 3 nút
        btnToggle.setOnAction(e -> handleToggle());
        btnEdit.setOnAction(e -> handleEdit());
        btnDelete.setOnAction(e -> handleDelete());
    }

    // ── Handlers ──────────────────────────────────────────────────────────────

    /**
     * Bật/tắt voucher (đảo is_active).
     * Service chỉ có deactivate nên dùng updateVoucherInfo để bật lại.
     */
    private void handleToggle() {
        boolean newState = !currentVoucher.isActive();
        String action = newState ? "bật" : "tắt";

        boolean confirmed = DialogHelper.showConfirm(
                "Xác nhận",
                "Bạn có chắc muốn " + action + " voucher \"" + currentVoucher.getCode() + "\" không?"
        );
        if (!confirmed) return;

        try {
            currentVoucher.setActive(newState);
            boolean ok = voucherService.updateVoucherInfo(currentVoucher);
            if (ok) {
                DialogHelper.showInfo("Thành công", "Đã " + action + " voucher " + currentVoucher.getCode() + ".");
                parentController.loadData();
            } else {
                currentVoucher.setActive(!newState); // rollback
                DialogHelper.showInfo("Lỗi", "Không thể cập nhật trạng thái voucher.");
            }
        } catch (Exception e) {
            currentVoucher.setActive(!newState);
            DialogHelper.showInfo("Lỗi", e.getMessage());
        }
    }

    /**
     * Mở VoucherForm.fxml để chỉnh sửa voucher hiện tại.
     */
    private void handleEdit() {
        parentController.openFormStage(currentVoucher);
    }

    /**
     * Xác nhận và xóa mềm voucher (deactivate).
     */
    private void handleDelete() {
        boolean confirm = DialogHelper.showConfirm(
                "Xóa voucher",
                "Bạn có chắc muốn xóa voucher \"" + currentVoucher.getCode() + "\" không?\nHành động này sẽ vô hiệu hóa mã giảm giá."
        );
        if (!confirm) return;

        try {
            boolean ok = voucherService.disableVoucher(currentVoucher.getVoucherId());
            if (ok) {
                DialogHelper.showInfo("Thành công", "Đã xóa voucher " + currentVoucher.getCode() + ".");
                parentController.loadData();
            } else {
                DialogHelper.showInfo("Lỗi", "Không thể xóa voucher.");
            }
        } catch (Exception e) {
            DialogHelper.showInfo("Lỗi", e.getMessage());
        }
    }

    // ── UI helpers ─────────────────────────────────────────────────────────────

    private void updateStatusBadge(boolean active) {
        lblStatus.setText(active ? "Hoạt động" : "Tắt");
        if (active) {
            badgeStatus.setStyle("-fx-background-color: #d1fae5; -fx-background-radius: 20; -fx-padding: 4 12;");
            lblStatus.setStyle("-fx-text-fill: #065f46; -fx-font-size: 12px; -fx-font-weight: bold;");
        } else {
            badgeStatus.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 20; -fx-padding: 4 12;");
            lblStatus.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px; -fx-font-weight: bold;");
        }
    }

    private void updateToggleIcon(boolean active) {
        iconToggle.setIconLiteral(active ? "mdi2t-toggle-switch" : "mdi2t-toggle-switch-off");
        iconToggle.setIconColor(
                active ? javafx.scene.paint.Color.valueOf("#12b6a2")
                       : javafx.scene.paint.Color.valueOf("#a8a29e")
        );
    }
}
