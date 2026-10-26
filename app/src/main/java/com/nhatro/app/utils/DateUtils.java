package com.nhatro.app.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    private static final SimpleDateFormat ISO_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final SimpleDateFormat VN_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public static String formatDateVN(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return "---";
        try {
            // Handle ISO string or YYYY-MM-DD
            String clean = dateStr.length() > 10 ? dateStr.substring(0, 10) : dateStr;
            Date d = ISO_FORMAT.parse(clean);
            if (d != null) {
                return VN_FORMAT.format(d);
            }
        } catch (ParseException ignored) {}
        return dateStr;
    }

    public static String getTodayIso() {
        return ISO_FORMAT.format(new Date());
    }
}
