package com.vtea.controller;

import com.vtea.dto.BillDTO;
import com.vtea.dto.BillItemDTO;
import com.vtea.dto.BillToppingDTO;
import com.vtea.service.BillPdfService;
import com.vtea.service.BillReceiptFormatter;
import com.vtea.utils.DialogHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import java.io.File;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

/**
 * Controller hiển thị bill preview dạng hóa đơn in.
 * Sử dụng TextFlow để giữ đúng định dạng cũ nhưng in đậm một số phần.
 */
public class BillPreviewController {

    private static final int BILL_WIDTH = 42;

    @FXML
    private TextFlow billTextFlow;

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
     * Render toàn bộ nội dung bill lên TextFlow.
     */
    private void renderBill() {
        if (bill == null) {
            return;
        }
        
        billTextFlow.getChildren().clear();

        Text header = new Text(receiptFormatter.formatHeader());
        header.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        Text body = new Text(receiptFormatter.formatBodyOnly(bill));

        Text total = new Text(receiptFormatter.formatTotal(bill));
        total.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        Text footer = new Text(receiptFormatter.formatFooter());

        billTextFlow.getChildren().addAll(header, body, total, footer);
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

        File file = fileChooser.showSaveDialog(billTextFlow.getScene().getWindow());

        if (file == null) {
            return;
        }

        try {
            billPdfService.exportBillToPdf(bill, file);
            DialogHelper.showInfo("Thành công", "Đã xuất hóa đơn ra file PDF: " + file.getName());
        } catch (Exception e) {
            e.printStackTrace();
            DialogHelper.showInfo("Lỗi xuất file", "Có lỗi xảy ra: " + e.getMessage());
        }
    }

    /**
     * Đóng màn hình preview.
     */
    @FXML
    private void handleClose() {
        billTextFlow.getScene().getWindow().hide();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}