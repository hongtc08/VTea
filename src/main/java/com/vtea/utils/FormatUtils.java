package com.vtea.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class FormatUtils {
    public static String formatPrice(BigDecimal price) {
        if (price == null) return "0đ";
        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(price) + "đ";
    }

    public static String formatNumber(double number) {
        return new DecimalFormat("#,###.##").format(number);
    }
}
