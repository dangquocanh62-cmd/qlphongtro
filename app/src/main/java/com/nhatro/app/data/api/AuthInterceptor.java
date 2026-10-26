package com.nhatro.app.data.api;

import android.content.Context;
import androidx.annotation.NonNull;
import com.nhatro.app.utils.SharedPrefsHelper;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private final Context context;

    public AuthInterceptor(Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request original = chain.request();
        String token = SharedPrefsHelper.getInstance(context).getToken();

        Request.Builder requestBuilder = original.newBuilder()
                .header("Accept", "application/json");

        if (token != null && !token.isEmpty()) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }

        return chain.proceed(requestBuilder.build());
    }
}
