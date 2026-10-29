package com.nhatro.app.utils;

public class Constants {
    // Kết nối trực tiếp máy chủ Node.js qua adb reverse tcp:3000 tcp:3000
    public static final String DEFAULT_BASE_URL = "http://127.0.0.1:3000/api/";
    
    public static final String PREF_NAME = "QLPhongTroPrefs";
    public static final String KEY_TOKEN = "key_token";
    public static final String KEY_USER_EMAIL = "key_user_email";
    public static final String KEY_USER_NAME = "key_user_name";
    public static final String KEY_USER_ROLE = "key_user_role";
    public static final String KEY_SERVER_URL = "key_server_url";
    public static final String KEY_BANK_NAME = "key_bank_name";
    public static final String KEY_BANK_ACCOUNT = "key_bank_account";
    public static final String KEY_BANK_OWNER = "key_bank_owner";

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
