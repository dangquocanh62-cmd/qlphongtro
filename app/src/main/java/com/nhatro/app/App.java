package com.nhatro.app;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Enforce Light Mode across the app to prevent dark mode background mismatch
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        // Enable vector drawable support on all Android versions
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }
}
