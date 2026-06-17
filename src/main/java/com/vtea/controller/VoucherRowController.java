package com.vtea.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * VoucherRowController – Controller cho MỖI DÒNG trong danh sách voucher.
 * Được VoucherController khởi tạo động qua FXMLLoader.
 *
 * Chức năng:
 *  1. setData(voucherDTO, parent) – điền dữ liệu vào UI của row
 *  2. handleToggle()   – bật/tắt voucher (is_active)
 *  3. handleEdit()     – mở VoucherForm.fxml để chỉnh sửa
 *  4. handleDelete()   – xác nhận và xóa voucher
 */
public class VoucherRowController {

    // ── FXML bindings ─────────────────────────────────────────────────────────
    @FXML private Label lblName;
    @FXML private Label lblCode;
    @FXML private FontIcon iconDiscountType;   // fth-tag hoặc mdi2p-percent
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

    // ── State ─────────────────────────────────────────────────────────────────
    private Object currentVoucher;         // TODO: thay Object → VoucherDTO
    private VoucherController parentController;

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Điền dữ liệu vào từng ô của row và gán sự kiện cho 3 nút.
     * @param voucherDTO TODO: thay Object bằng VoucherDTO
     * @param parent     tham chiếu về VoucherController để reload sau CRUD
     */
    public void setData(Object voucherDTO, VoucherController parent) {
        this.currentVoucher = voucherDTO;
        this.parentController = parent;

        // TODO: lblName.setText(voucherDTO.getName());
        // TODO: lblCode.setText(voucherDTO.getCode());

        // TODO: Nếu discountType == "FIXED":
        //   iconDiscountType.setIconLiteral("fth-tag");
        //   iconDiscountType.setIconColor(Color.valueOf("#F59E0B"));
        //   lblDiscount.setText(FormatUtils.formatPrice(voucherDTO.getDiscountValue()));
        //   lblDiscount.setStyle("... -fx-text-fill: #F59E0B ...");
        // TODO: Nếu discountType == "PERCENT":
        //   iconDiscountType.setIconLiteral("mdi2p-percent");
        //   iconDiscountType.setIconColor(Color.valueOf("#12b6a2"));
        //   lblDiscount.setText(voucherDTO.getDiscountValue() + "%");
        //   lblDiscount.setStyle("... -fx-text-fill: #12b6a2 ...");

        // TODO: lblExpiry.setText(voucherDTO.getExpiryDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        // TODO: Nếu expiryDate đã qua → iconExpiry.setIconColor(RED) + lblExpiry.setStyle("red")

        // TODO: usedCount / maxUses:
        //   int used = voucherDTO.getUsedCount();
        //   Integer max = voucherDTO.getMaxUses();  ← null = vô hạn
        //   lblUsageCount.setText(max == null ? used + "/∞" : used + "/" + max);
        //   progressUsage.setProgress(max == null ? 0 : (double) used / max);

        // TODO: Trạng thái badge:
        //   boolean active = voucherDTO.isActive();
        //   lblStatus.setText(active ? "Hoạt động" : "Tắt");
        //   badgeStatus.setStyle(active ? "... green ..." : "... gray ...");
        //   iconToggle.setIconLiteral(active ? "mdi2t-toggle-switch" : "mdi2t-toggle-switch-off");
        //   iconToggle.setIconColor(active ? Color.valueOf("#12b6a2") : Color.valueOf("#a8a29e"));

        // Gán sự kiện
        btnToggle.setOnAction(e -> handleToggle());
        btnEdit.setOnAction(this::handleEdit);
        btnDelete.setOnAction(this::handleDelete);
    }

    // ── Handlers ──────────────────────────────────────────────────────────────

    /**
     * Bật / tắt voucher (đảo trạng thái is_active).
     */
    private void handleToggle() {
        // TODO:
        //  boolean newState = !currentVoucher.isActive();
        //  boolean success = voucherService.setActive(currentVoucher.getVoucherId(), newState);
        //  if (success) parentController.loadData();
        //  else DialogHelper.showInfo("Lỗi", "Không thể cập nhật trạng thái voucher.");
    }

    /**
     * Mở VoucherForm.fxml để chỉnh sửa voucher hiện tại.
     */
    private void handleEdit(ActionEvent e) {
        // TODO:
        //  FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vtea/view/VoucherForm.fxml"));
        //  Parent root = loader.load();
        //  VoucherFormController ctrl = loader.getController();
        //  ctrl.setVoucher(currentVoucher, parentController);   ← truyền data cũ vào form
        //  openFormStage(root);
    }

    /**
     * Xác nhận và xóa voucher.
     */
    private void handleDelete(ActionEvent e) {
        // TODO:
        //  boolean confirm = DialogHelper.showConfirm(
        //      "Xóa voucher",
        //      "Bạn có chắc muốn xóa voucher \"" + currentVoucher.getName() + "\" không?\n" +
        //      "Hành động này không thể hoàn tác."
        //  );
        //  if (!confirm) return;
        //  boolean success = voucherService.deleteVoucher(currentVoucher.getVoucherId());
        //  if (success) { DialogHelper.showInfo("Thành công", "Đã xóa voucher."); parentController.loadData(); }
        //  else DialogHelper.showInfo("Lỗi", "Không thể xóa voucher.");
    }

    /**
     * Mở Stage modal chứa form với hiệu ứng blur nền.
     */
    private void openFormStage(Parent root) {
        // TODO:
        //  Stage stage = new Stage();
        //  stage.initModality(Modality.APPLICATION_MODAL);
        //  stage.initStyle(StageStyle.TRANSPARENT);
        //  Scene scene = new Scene(root);
        //  scene.setFill(Color.TRANSPARENT);
        //  stage.setScene(scene);
        //  DialogHelper.applyBlurBackground(true);
        //  DialogHelper.animateDialog(root);
        //  stage.showAndWait();
        //  DialogHelper.applyBlurBackground(false);
    }
}
