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

    /**
     * Tạo nội dung bill theo kiểu hóa đơn cửa hàng / máy in nhiệt.
     */
    private String buildReceiptContent() {
        StringBuilder builder = new StringBuilder();

        builder.append(center("VTEA COFFEE")).append("\n");
        builder.append(center("HÓA ĐƠN THANH TOÁN")).append("\n");
        builder.append(line()).append("\n");

        builder.append(leftRight("Mã HĐ: #" + bill.getOrderId(), formatDateTime())).append("\n");
        builder.append("Nhân viên: ").append(nullToDefault(bill.getStaffName(), "Không rõ")).append("\n");
        builder.append("Khách hàng: ").append(bill.getCustomerName()).append("\n");

        if (bill.getCustomerPhone() != null && !bill.getCustomerPhone().isBlank()) {
            builder.append("SĐT: ").append(bill.getCustomerPhone()).append("\n");
        }

        builder.append("Thanh toán: ").append(nullToDefault(bill.getPaymentMethod(), "Không rõ")).append("\n");
        builder.append(line()).append("\n");

        builder.append(String.format("%-22s %3s %7s %8s", "TÊN MÓN", "SL", "GIÁ", "TIỀN")).append("\n");
        builder.append(line()).append("\n");

        BigDecimal subtotal = BigDecimal.ZERO;

        for (BillItemDTO item : bill.getItems()) {
            BigDecimal productTotal = item.getProductTotal();
            subtotal = subtotal.add(productTotal);

            appendItemLine(
                    builder,
                    item.getProductName(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    productTotal
            );

            for (BillToppingDTO topping : item.getToppings()) {
                BigDecimal toppingTotal = topping.getTotalPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity()));

                subtotal = subtotal.add(toppingTotal);

                appendItemLine(
                        builder,
                        "  + " + topping.getToppingName(),
                        topping.getQuantity() * item.getQuantity(),
                        topping.getUnitPrice(),
                        toppingTotal
                );
            }
        }

        BigDecimal vat = subtotal.subtract(bill.getTierDiscountAmount()).subtract(bill.getPointDiscountAmount()).multiply(new BigDecimal("0.10"));
        if (vat.compareTo(BigDecimal.ZERO) < 0) vat = BigDecimal.ZERO;
        BigDecimal total = bill.getTotalAmount();

        builder.append(line()).append("\n");
        builder.append(moneyLine("Tạm tính:", subtotal)).append("\n");
        if (bill.getTierDiscountAmount() != null && bill.getTierDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            builder.append(moneyLine("Hạng " + bill.getTierName() + ":", bill.getTierDiscountAmount().negate())).append("\n");
        }
        if (bill.getPointDiscountAmount() != null && bill.getPointDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            builder.append(moneyLine("Trừ điểm:", bill.getPointDiscountAmount().negate())).append("\n");
        }
        builder.append(moneyLine("VAT 10%:", vat)).append("\n");
        builder.append(moneyLine("Tổng cộng:", total)).append("\n");
        builder.append(line()).append("\n");

        builder.append(center("Cảm ơn quý khách!")).append("\n");
        builder.append(center("Hẹn gặp lại")).append("\n");

        return builder.toString();
    }

    /**
     * Thêm một dòng món/topping vào bill.
     * Nếu tên quá dài thì tự cắt để không vỡ cột.
     */
    private void appendItemLine(
            StringBuilder builder,
            String name,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal total
    ) {
        String safeName = shorten(name, 22);

        builder.append(String.format(
                "%-22s %3d %7s %8s",
                safeName,
                quantity,
                formatShortMoney(unitPrice),
                formatShortMoney(total)
        )).append("\n");
    }

    /**
     * Format dòng tiền bên dưới bill như tạm tính, VAT, tổng cộng.
     */
    private String moneyLine(String label, BigDecimal amount) {
        return leftRight(label, formatMoney(amount));
    }

    /**
     * Căn trái - phải trên cùng một dòng.
     */
    private String leftRight(String left, String right) {
        if (left == null) {
            left = "";
        }

        if (right == null) {
            right = "";
        }

        int space = BILL_WIDTH - left.length() - right.length();

        if (space < 1) {
            space = 1;
        }

        return left + " ".repeat(space) + right;
    }

    /**
     * Căn giữa tiêu đề bill.
     */
    private String center(String text) {
        if (text == null) {
            text = "";
        }

        if (text.length() >= BILL_WIDTH) {
            return text;
        }

        int leftPadding = (BILL_WIDTH - text.length()) / 2;
        return " ".repeat(leftPadding) + text;
    }

    /**
     * Tạo đường kẻ ngang cho bill.
     */
    private String line() {
        return "-".repeat(BILL_WIDTH);
    }

    /**
     * Cắt ngắn tên món nếu tên quá dài làm lệch cột.
     */
    private String shorten(String text, int maxLength) {
        if (text == null) {
            return "";
        }

        if (text.length() <= maxLength) {
            return text;
        }

        return text.substring(0, maxLength - 1) + ".";
    }

    private String formatDateTime() {
        if (bill.getCreatedAt() == null) {
            return "";
        }

        return bill.getCreatedAt().format(dateTimeFormatter);
    }

    /**
     * Format tiền đầy đủ cho phần tổng tiền.
     */
    private String formatMoney(BigDecimal amount) {
        if (amount == null) {
            return "0 đ";
        }

        return String.format("%,.0f đ", amount);
    }

    /**
     * Format tiền ngắn cho bảng món để không bị lệch cột.
     */
    private String formatShortMoney(BigDecimal amount) {
        if (amount == null) {
            return "0";
        }

        return String.format("%,.0f", amount);
    }

    private String nullToDefault(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return value;
    }

    /**
     * Tạm thời báo chưa làm PDF.
     * Bước sau sẽ nối method này với BillPdfService.
     */
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