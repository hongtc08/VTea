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
 * Service format nội dung hóa đơn dạng text.
 * Dùng chung cho bill preview, lịch sử hóa đơn và xuất PDF.
 */
public class BillReceiptFormatter {

    private static final int BILL_WIDTH = 42;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Tạo nội dung hóa đơn dạng bill cửa hàng.
     */
    public String format(BillDTO bill) {
        if (bill == null) {
            return "";
        }

        StringBuilder builder = new StringBuilder();

        builder.append(center("VTEA COFFEE")).append("\n");
        builder.append(center("HÓA ĐƠN THANH TOÁN")).append("\n");
        builder.append(line()).append("\n");

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

        builder.append(String.format("%-22s %3s %7s %8s", "TÊN MÓN", "SL", "GIÁ", "TIỀN")).append("\n");
        builder.append(line()).append("\n");

        BigDecimal subtotal = BigDecimal.ZERO;

        for (BillItemDTO item : buildDisplayItems(bill.getItems())) {
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

        BigDecimal tierDiscount = bill.getTierDiscountAmount();
        BigDecimal pointDiscount = bill.getPointDiscountAmount();

        BigDecimal discountTotal = tierDiscount.add(pointDiscount);
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

        builder.append(moneyLine("VAT 10%:", vat)).append("\n");
        builder.append(moneyLine("Tổng cộng:", total)).append("\n");
        builder.append(line()).append("\n");

        builder.append(center("Cảm ơn quý khách!")).append("\n");
        builder.append(center("Hẹn gặp lại")).append("\n");

        return builder.toString();
    }

    /**
     * Thêm một dòng món hoặc topping vào bill.
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
     * Format dòng tiền ở cuối bill.
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

    private String line() {
        return "-".repeat(BILL_WIDTH);
    }

    /**
     * Cắt ngắn tên món để không vỡ cột bill.
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
    /**
     * Gom các món giống nhau không có topping để bill hiển thị gọn hơn.
     * Các món có topping vẫn giữ riêng để tránh sai topping từng ly.
     */
    private List<BillItemDTO> buildDisplayItems(List<BillItemDTO> items) {
        List<BillItemDTO> displayItems = new ArrayList<>();
        Map<String, BillItemDTO> groupedItems = new LinkedHashMap<>();

        for (BillItemDTO item : items) {
            boolean hasTopping = item.getToppings() != null && !item.getToppings().isEmpty();

            if (hasTopping) {
                displayItems.add(item);
                continue;
            }

            String key = item.getProductId() + "|" + item.getUnitPrice();

            if (groupedItems.containsKey(key)) {
                BillItemDTO existingItem = groupedItems.get(key);
                existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
            } else {
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
    private String formatDateTime(BillDTO bill) {
        if (bill.getCreatedAt() == null) {
            return "";
        }

        return bill.getCreatedAt().format(DATE_TIME_FORMATTER);
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null) {
            return "0 đ";
        }

        return String.format("%,.0f đ", amount);
    }

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
}