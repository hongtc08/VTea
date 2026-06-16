package com.vtea.controller;

import com.vtea.dto.BillDTO;
import com.vtea.dto.BillItemDTO;
import com.vtea.dto.BillToppingDTO;
import com.vtea.service.BillPdfService;
import com.vtea.service.BillReceiptFormatter;
import com.vtea.utils.DialogHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import java.io.File;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

/**
 * Controller hiển thị bill preview dạng hóa đơn in.
 * Chỉ dùng một TextArea để render bill giống hóa đơn cửa hàng.
 */
public class BillPreviewController {

    private static final int BILL_WIDTH = 42;

    @FXML
    private TextArea billContentTextArea;

    private BillDTO bill;

    private final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final BillReceiptFormatter receiptFormatter = new BillReceiptFormatter();
    private final BillPdfService billPdfService = new BillPdfService();

    /**
     * Nhận dữ liệu hóa đơn từ POSController hoặc màn lịch sử hóa đơn.
     */
    public void setBill(BillDTO bill) {
        this.bill = bill;
        renderBill();
    }

    /**
     * Render toàn bộ nội dung bill lên TextArea.
     */
    private void renderBill() {
        if (bill == null) {
            return;
        }

        billContentTextArea.setText(receiptFormatter.format(bill));
        billContentTextArea.positionCaret(0);
    }


    @FXML
    private void handleExportPdf() {
        if (bill == null) {
            DialogHelper.showInfo("Thông báo", "Không có dữ liệu hóa đơn để xuất PDF.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu hóa đơn PDF");
        fileChooser.setInitialFileName("bill-" + bill.getOrderId() + ".pdf");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF files", "*.pdf")
        );

        File file = fileChooser.showSaveDialog(billContentTextArea.getScene().getWindow());

        if (file == null) {
            return;
        }

        try {
            billPdfService.exportBillToPdf(bill, file);
            DialogHelper.showInfo("Thành công", "Xuất PDF hóa đơn thành công.");
        } catch (Exception e) {
            e.printStackTrace();
            DialogHelper.showInfo("Lỗi", "Không thể xuất PDF: " + e.getMessage());
        }
    }

    /**
     * Đóng cửa sổ bill preview.
     */
    @FXML
    private void handleClose() {
        billContentTextArea.getScene().getWindow().hide();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}