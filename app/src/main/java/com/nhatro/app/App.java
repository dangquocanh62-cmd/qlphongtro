package com.nhatro.app;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Enable vector drawable support on all Android versions
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }
}
