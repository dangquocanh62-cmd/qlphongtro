package com.nhatro.app.utils;

public class Constants {
    // Máy chủ Cloud Render 24/7
    public static final String DEFAULT_BASE_URL = "https://qlphongtro-2y3o.onrender.com/api/";
    
    public static final String PREF_NAME = "QLPhongTroPrefs";
    public static final String KEY_TOKEN = "key_token";
    public static final String KEY_USER_EMAIL = "key_user_email";
    public static final String KEY_USER_NAME = "key_user_name";
    public static final String KEY_USER_ROLE = "key_user_role";
    public static final String KEY_SERVER_URL = "key_server_url";

    // Trạng thái phòng
    public static final String STATUS_AVAILABLE = "AVAILABLE";
    public static final String STATUS_OCCUPIED = "OCCUPIED";
    public static final String STATUS_MAINTENANCE = "MAINTENANCE";

    // Trạng thái hóa đơn
    public static final String INVOICE_UNPAID = "UNPAID";
    public static final String INVOICE_PARTIAL = "PARTIAL";
    public static final String INVOICE_PAID = "PAID";

    // Phương thức thanh toán
    public static final String PAYMENT_CASH = "CASH";
    public static final String PAYMENT_TRANSFER = "TRANSFER";
}
