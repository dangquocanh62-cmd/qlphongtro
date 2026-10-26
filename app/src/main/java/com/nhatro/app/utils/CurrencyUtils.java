package com.nhatro.app.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class CurrencyUtils {
    public static String formatVND(long amount) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setGroupingSeparator('.');
        DecimalFormat formatter = new DecimalFormat("#,###", symbols);
        return formatter.format(amount) + " đ";
    }

    public static String formatVND(Long amount) {
        if (amount == null) return "0 đ";
        return formatVND(amount.longValue());
    }

    public static String formatVND(double amount) {
        return formatVND((long) amount);
    }
}
