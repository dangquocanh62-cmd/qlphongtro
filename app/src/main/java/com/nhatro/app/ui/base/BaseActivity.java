package com.nhatro.app.ui.base;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import androidx.appcompat.app.AppCompatActivity;

/**
 * BaseActivity chuẩn hóa tỉ lệ fontScale (cỡ chữ hệ thống)
 * và kích thước hiển thị giúp giao diện ứng dụng luôn vừa vặn,
 * tránh tình trạng vỡ layout hoặc chữ tràn, chồng chéo lên nhau
 * khi người dùng cài đặt cỡ chữ hiển thị quá lớn trên điện thoại.
 */
public abstract class BaseActivity extends AppCompatActivity {

    public static final float MAX_FONT_SCALE = 1.05f;
    public static final float MIN_FONT_SCALE = 0.90f;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(adjustFontScale(newBase));
    }

    public static Context adjustFontScale(Context context) {
        if (context == null) return null;
        Configuration configuration = context.getResources().getConfiguration();
        float currentScale = configuration.fontScale;

        // Nếu máy đặt cỡ chữ vượt ngưỡng an toàn, giới hạn lại trong khoảng [0.90 - 1.05]
        if (currentScale > MAX_FONT_SCALE || currentScale < MIN_FONT_SCALE) {
            float targetScale = Math.max(MIN_FONT_SCALE, Math.min(currentScale, MAX_FONT_SCALE));
            Configuration config = new Configuration(configuration);
            config.fontScale = targetScale;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                return context.createConfigurationContext(config);
            } else {
                context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
            }
        }
        return context;
    }

    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config = res.getConfiguration();
        if (config.fontScale > MAX_FONT_SCALE || config.fontScale < MIN_FONT_SCALE) {
            config.fontScale = Math.max(MIN_FONT_SCALE, Math.min(config.fontScale, MAX_FONT_SCALE));
            res.updateConfiguration(config, res.getDisplayMetrics());
        }
        return res;
    }
}
