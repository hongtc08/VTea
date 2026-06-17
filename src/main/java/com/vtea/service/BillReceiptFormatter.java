package com.vtea.service;

import com.vtea.dto.BillDTO;
import com.vtea.dto.BillItemDTO;
import com.vtea.dto.BillToppingDTO;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Format hóa đơn
 */
public class BillReceiptFormatter {

    //Rộng 42 ký tự
    private static final int BILL_WIDTH = 42;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ========================================

    /**
     * Lấy nội dung từ DTO
     */
    public String format(BillDTO bill) {
        if (bill == null) return "";
        StringBuilder builder = new StringBuilder();

        builder.append(formatHeader());
        builder.append(formatBodyOnly(bill));
        builder.append(formatTotal(bill));
        builder.append(formatFooter());

        return builder.toString();
    }

    public String formatHeader() {
        return lineDouble() + "\n" +
               center("VTEA COFFEE") + "\n" +
               center("HÓA ĐƠN THANH TOÁN") + "\n" +
               lineDouble() + "\n";
    }

    public String formatTotal(BillDTO bill) {
        return lineDouble() + "\n" +
               leftRight("TỔNG CỘNG:", formatMoneyFull(bill.getTotalAmount())) + "\n" +
               lineDouble() + "\n";
    }

    public String formatFooter() {
        return center("Cảm ơn quý khách!") + "\n" +
               center("Hẹn gặp lại") + "\n";
    }

    public String formatBodyOnly(BillDTO bill) {
        StringBuilder builder = new StringBuilder();

        // --- Thông tin hóa đơn, nhân viên, khách hàng
        builder.append(leftRight("Mã HĐ: #" + bill.getOrderId(), formatDateTime(bill))).append("\n");
        builder.append("Nhân viên: ").append(nullToDefault(bill.getStaffName(), "Không rõ")).append("\n");
        builder.append("Khách hàng: ").append(bill.getCustomerName()).append("\n");

        if (bill.getCustomerPhone() != null && !bill.getCustomerPhone().isBlank()) {
            builder.append("SĐT: ").append(bill.getCustomerPhone()).append("\n");
        }

        if (bill.getTierName() != null && !bill.getTierName().isBlank()) {
            builder.append("Hạng TV: ")
                    .append(bill.getTierName())
                    .append(" (")
                    .append(bill.getDiscountPercent())
                    .append("%)")
                    .append("\n");
        }

        builder.append("Thanh toán: ").append(nullToDefault(bill.getPaymentMethod(), "Không rõ")).append("\n");
        builder.append(line()).append("\n");

        // --- Dòng tiêu đề món ---
        builder.append(String.format("%-22s %3s %7s %8s", "TÊN MÓN", "SL", "GIÁ", "TIỀN")).append("\n");
        builder.append(line()).append("\n");

        // --- List món ---
        BigDecimal subtotal = BigDecimal.ZERO;

        for (BillItemDTO item : buildDisplayItems(bill.getItems())) {
            BigDecimal productTotal = item.getProductTotal();
            subtotal = subtotal.add(productTotal);

            // In dòng món chính
            appendItemLine(
                    builder,
                    item.getProductName(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    productTotal
            );

            // In các dòng topping (nếu có)
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

        // --- Tính tiền ---
        BigDecimal tierDiscount = bill.getTierDiscountAmount();
        BigDecimal pointDiscount = bill.getPointDiscountAmount();
        BigDecimal voucherDiscount = bill.getVoucherDiscountAmount();

        BigDecimal discountTotal = tierDiscount.add(pointDiscount).add(voucherDiscount);
        BigDecimal amountAfterDiscount = subtotal.subtract(discountTotal);

        if (amountAfterDiscount.compareTo(BigDecimal.ZERO) < 0) {
            amountAfterDiscount = BigDecimal.ZERO;
        }

        BigDecimal vat = amountAfterDiscount.multiply(new BigDecimal("0.10"));
        BigDecimal total = bill.getTotalAmount();

        builder.append(line()).append("\n");
        builder.append(moneyLine("Tạm tính:", subtotal)).append("\n");

        if (tierDiscount.compareTo(BigDecimal.ZERO) > 0) {
            builder.append(moneyLine("Ưu đãi hạng TV:", tierDiscount.negate())).append("\n");
        }

        if (pointDiscount.compareTo(BigDecimal.ZERO) > 0) {
            builder.append(moneyLine("Giảm điểm:", pointDiscount.negate())).append("\n");
        }

        if (voucherDiscount.compareTo(BigDecimal.ZERO) > 0) {
            builder.append(moneyLine("Giảm giá Voucher:", voucherDiscount.negate())).append("\n");
        }

        builder.append(moneyLine("VAT 10%:", vat)).append("\n");
        
        return builder.toString();
    }

    // ========================================

    /**
     * 2 món giống nhau gom số lượng
     */
    private List<BillItemDTO> buildDisplayItems(List<BillItemDTO> items) {
        List<BillItemDTO> displayItems = new ArrayList<>();
        Map<String, BillItemDTO> groupedItems = new LinkedHashMap<>();

        for (BillItemDTO item : items) {
            boolean hasTopping = item.getToppings() != null && !item.getToppings().isEmpty();

            //Có topping -> ko gộp
            if (hasTopping) {
                displayItems.add(item);
                continue;
            }

            //Tạo key
            String key = item.getProductId() + "|" + item.getUnitPrice();

            if (groupedItems.containsKey(key)) {
                // Đã có món này rồi -> Cộng dồn số lượng
                BillItemDTO existingItem = groupedItems.get(key);
                existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
            } else {
                // Chưa có -> Tạo bản sao và thêm vào danh sách gộp
                BillItemDTO copiedItem = new BillItemDTO(
                        item.getDetailId(),
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getUnitPrice()
                );

                copiedItem.setToppings(new ArrayList<>());
                groupedItems.put(key, copiedItem);
                displayItems.add(copiedItem);
            }
        }

        return displayItems;
    }

    // ====================CĂN LỀ====================

    /**
     * Căn trái - phải trên cùng một dòng.
     */
    private String leftRight(String left, String right) {
        if (left == null) left = "";
        if (right == null) right = "";

        int space = BILL_WIDTH - left.length() - right.length();
        if (space < 1) space = 1;

        return left + " ".repeat(space) + right;
    }

    /**
     * Căn giữa văn bản dựa trên độ rộng hóa đơn.
     */
    private String center(String text) {
        if (text == null) text = "";
        if (text.length() >= BILL_WIDTH) return text;

        int leftPadding = (BILL_WIDTH - text.length()) / 2;
        return " ".repeat(leftPadding) + text;
    }

    /**
     * Vẽ một đường gạch ngang chia cắt (---)
     */
    private String line() {
        return "-".repeat(BILL_WIDTH);
    }

    /**
     * Vẽ một đường gạch đôi nhấn mạnh (===)
     */
    private String lineDouble() {
        return "=".repeat(BILL_WIDTH);
    }

    // ==================== FORMAT DỮ LIỆU ====================

    /**
     * Thêm một dòng hiển thị món ăn hoặc topping theo đúng cột
     */
    private void appendItemLine(StringBuilder builder, String name, int quantity, BigDecimal unitPrice, BigDecimal total) {
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
     * Cắt ngắn tên quá dài để không bị vỡ bố cục cột của hóa đơn
     */
    private String shorten(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 1) + ".";
    }

    /**
     * Format dòng tiền tệ ở phần tổng kết cuối hóa đơn
     */
    private String moneyLine(String label, BigDecimal amount) {
        return leftRight(label, formatMoney(amount));
    }

    private String formatDateTime(BillDTO bill) {
        if (bill.getCreatedAt() == null) return "";
        return bill.getCreatedAt().format(DATE_TIME_FORMATTER);
    }

    private String formatMoneyFull(BigDecimal amount) {
        if (amount == null) return "0 VND";
        return String.format("%,.0f VND", amount);
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null) return "0 đ";
        return String.format("%,.0f đ", amount);
    }

    private String formatShortMoney(BigDecimal amount) {
        if (amount == null) return "0";
        return String.format("%,.0f", amount);
    }

    private String nullToDefault(String value, String defaultValue) {
        if (value == null || value.isBlank()) return defaultValue;
        return value;
    }
}