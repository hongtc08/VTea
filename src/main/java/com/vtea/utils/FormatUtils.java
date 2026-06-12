package com.vtea.utils;

import java.math.BigDecimal;

/**
 * Tiện ích hỗ trợ định dạng dữ liệu (tiền tệ, ngày tháng, ...) dùng chung.
 */
public class FormatUtils {

    /**
     * Định dạng số tiền sang chuẩn Việt Nam (VNĐ).
     * @param price Số tiền
     * @return Chuỗi đã định dạng (VD: 15,000 đ)
     */
    public static String formatPrice(BigDecimal price) {
        if (price == null) {
            return "0 đ";
        }
        return String.format("%,.0f đ", price);
    }
}
