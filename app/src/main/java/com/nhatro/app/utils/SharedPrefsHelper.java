package com.nhatro.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefsHelper {
    private static SharedPrefsHelper instance;
    private final SharedPreferences prefs;

    private SharedPrefsHelper(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SharedPrefsHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefsHelper(context);
        }
        return instance;
    }

    public void saveAuth(String token, String email, String name, String role) {
        prefs.edit()
                .putString(Constants.KEY_TOKEN, token)
                .putString(Constants.KEY_USER_EMAIL, email)
                .putString(Constants.KEY_USER_NAME, name)
                .putString(Constants.KEY_USER_ROLE, role)
                .apply();
    }

    public String getToken() {
        return prefs.getString(Constants.KEY_TOKEN, "");
    }

    public boolean isLoggedIn() {
        return !getToken().isEmpty();
    }

    public String getUserName() {
        return prefs.getString(Constants.KEY_USER_NAME, "Quản trị viên");
    }

    public String getUserRole() {
        return prefs.getString(Constants.KEY_USER_ROLE, "OWNER");
    }

    public void saveServerUrl(String url) {
        if (!url.endsWith("/")) {
            url += "/";
        }
        prefs.edit().putString(Constants.KEY_SERVER_URL, url).apply();
    }

    public String getServerUrl() {
        return prefs.getString(Constants.KEY_SERVER_URL, Constants.DEFAULT_BASE_URL);
    }

    public void clear() {
        String savedServer = getServerUrl();
        prefs.edit().clear().apply();
        saveServerUrl(savedServer); // Keep server url
    }
}
