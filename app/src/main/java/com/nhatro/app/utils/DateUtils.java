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

    /**
     * Lấy ngày hiện tại định dạng tiếng Việt đầy đủ (Ví dụ: "Thứ Tư, 28/10/2026")
     */
    public static String getTodayFullVN() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale("vi", "VN"));
        String dateStr = sdf.format(new Date());
        if (!dateStr.isEmpty()) {
            dateStr = Character.toUpperCase(dateStr.charAt(0)) + dateStr.substring(1);
        }
        if (dateStr.startsWith("Thứ ") && dateStr.length() > 4) {
            dateStr = "Thứ " + Character.toUpperCase(dateStr.charAt(4)) + dateStr.substring(5);
        }
        return dateStr;
    }

    /**
     * Tính số ngày còn lại từ hôm nay đến ngày kết thúc (dương: còn hạn, âm: quá hạn)
     */
    public static int getDaysRemaining(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return Integer.MAX_VALUE;
        try {
            String clean = dateStr.length() > 10 ? dateStr.substring(0, 10) : dateStr;
            Date d = ISO_FORMAT.parse(clean);
            if (d != null) {
                // Đặt thời gian về 0h để tính chính xác theo ngày
                java.util.Calendar calNow = java.util.Calendar.getInstance();
                calNow.set(java.util.Calendar.HOUR_OF_DAY, 0);
                calNow.set(java.util.Calendar.MINUTE, 0);
                calNow.set(java.util.Calendar.SECOND, 0);
                calNow.set(java.util.Calendar.MILLISECOND, 0);

                java.util.Calendar calEnd = java.util.Calendar.getInstance();
                calEnd.setTime(d);
                calEnd.set(java.util.Calendar.HOUR_OF_DAY, 0);
                calEnd.set(java.util.Calendar.MINUTE, 0);
                calEnd.set(java.util.Calendar.SECOND, 0);
                calEnd.set(java.util.Calendar.MILLISECOND, 0);

                long diffMillis = calEnd.getTimeInMillis() - calNow.getTimeInMillis();
                return (int) (diffMillis / (1000 * 60 * 60 * 24));
            }
        } catch (Exception ignored) {}
        return Integer.MAX_VALUE;
    }

    /**
     * Cộng thêm số tháng vào một ngày định dạng ISO (yyyy-MM-dd)
     */
    public static String addMonths(String baseDateStr, int months) {
        try {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            if (baseDateStr != null && !baseDateStr.trim().isEmpty()) {
                String clean = baseDateStr.length() > 10 ? baseDateStr.substring(0, 10) : baseDateStr;
                Date d = ISO_FORMAT.parse(clean);
                if (d != null) cal.setTime(d);
            }
            cal.add(java.util.Calendar.MONTH, months);
            return ISO_FORMAT.format(cal.getTime());
        } catch (Exception e) {
            return getTodayIso();
        }
    }
}
