package com.vtea.controller;

import com.vtea.model.Voucher;
import com.vtea.service.VoucherService;
import com.vtea.utils.DialogHelper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * VoucherController – Màn hình quản lý Voucher (chỉ Admin).
 * Được load bởi MainLayoutController.loadView("voucher").
 */
public class VoucherController {

    @FXML private TextField searchField;
    @FXML private VBox voucherListContainer;
    @FXML private VBox emptyState;
    @FXML private Button btnAddVoucher;

    private final VoucherService voucherService = new VoucherService();
    private List<Voucher> allVouchers;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        // Tìm kiếm real-time khi người dùng gõ
        searchField.textProperty().addListener((obs, old, keyword) -> filterAndRender(keyword));

        // Nút thêm voucher mới
        btnAddVoucher.setOnAction(e -> handleAddVoucher());

        // Load dữ liệu ban đầu
        loadData();
    }

    // ── Public API (VoucherRowController gọi sau CRUD) ────────────────────────

    /**
     * Tải lại toàn bộ danh sách từ DB và render lại màn hình.
     */
    public void loadData() {
        // Load trên background thread để không đứng hình UI
        Thread thread = new Thread(() -> {
            List<Voucher> list = voucherService.getAllVouchers();
            Platform.runLater(() -> {
                allVouchers = list;
                filterAndRender(searchField.getText());
            });
        });
        thread.setDaemon(true);
        thread.start();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Lọc danh sách theo từ khóa (mã voucher) và render lại từng VoucherRow.
     */
    private void filterAndRender(String keyword) {
        voucherListContainer.getChildren().clear();

        if (allVouchers == null || allVouchers.isEmpty()) {
            showEmptyState(true);
            return;
        }

        List<Voucher> filtered = allVouchers;
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim().toLowerCase();
            filtered = allVouchers.stream()
                    .filter(v -> v.getCode().toLowerCase().contains(kw))
                    .collect(Collectors.toList());
        }

        if (filtered.isEmpty()) {
            showEmptyState(true);
        } else {
            showEmptyState(false);
            for (Voucher v : filtered) {
                renderRow(v);
            }
        }
    }

    /**
     * Load 1 VoucherRow.fxml, gán data, thêm vào danh sách.
     */
    private void renderRow(Voucher voucher) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vtea/view/VoucherRow.fxml"));
            Parent row = loader.load();
            VoucherRowController ctrl = loader.getController();
            ctrl.setData(voucher, this);
            voucherListContainer.getChildren().add(row);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Hiện/ẩn trạng thái "Không có voucher nào".
     */
    private void showEmptyState(boolean show) {
        emptyState.setVisible(show);
        emptyState.setManaged(show);
    }

    /**
     * Mở VoucherForm để thêm voucher mới.
     */
    @FXML
    private void handleAddVoucher() {
        openFormStage(null);
    }

    /**
     * Mở Stage modal chứa VoucherForm.
     * @param voucher null = thêm mới, != null = chỉnh sửa
     */
    public void openFormStage(Voucher voucher) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vtea/view/VoucherForm.fxml"));
            Parent root = loader.load();
            VoucherFormController ctrl = loader.getController();
            ctrl.setVoucher(voucher, this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);

            DialogHelper.applyBlurBackground(true);
            DialogHelper.animateDialog(root);
            stage.showAndWait();
            DialogHelper.applyBlurBackground(false);
        } catch (IOException e) {
            e.printStackTrace();
            DialogHelper.showInfo("Lỗi", "Không thể mở form voucher: " + e.getMessage());
        }
    }
}
