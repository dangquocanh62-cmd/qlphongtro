package com.nhatro.app;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import com.nhatro.app.ui.base.BaseActivity;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Enforce Light Mode across the app to prevent dark mode background mismatch
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        // Enable vector drawable support on all Android versions
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        // Normalize fontScale on Application level
        BaseActivity.adjustFontScale(this);

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                BaseActivity.adjustFontScale(activity);
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {}

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                BaseActivity.adjustFontScale(activity);
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {}

            @Override
            public void onActivityStopped(@NonNull Activity activity) {}

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {}
        });
    }
}
