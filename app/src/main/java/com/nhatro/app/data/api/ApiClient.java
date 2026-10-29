package com.nhatro.app.data.api;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nhatro.app.utils.SharedPrefsHelper;
import java.io.IOException;
import java.net.ConnectException;
import java.util.concurrent.TimeUnit;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static ApiService apiService;
    private static String currentBaseUrl;

    public static synchronized ApiService getApiService(Context context) {
        String baseUrl = SharedPrefsHelper.getInstance(context).getServerUrl();

        if (apiService == null || !baseUrl.equals(currentBaseUrl)) {
            currentBaseUrl = baseUrl;

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Interceptor tự động dự phòng kết nối giữa 127.0.0.1 và 10.0.2.2 cho máy ảo/máy thật
            Interceptor fallbackInterceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request request = chain.request();
                    try {
                        return chain.proceed(request);
                    } catch (ConnectException e) {
                        HttpUrl url = request.url();
                        if ("127.0.0.1".equals(url.host()) && url.port() == 3000) {
                            HttpUrl fallbackUrl = url.newBuilder().host("10.0.2.2").build();
                            return chain.proceed(request.newBuilder().url(fallbackUrl).build());
                        } else if ("10.0.2.2".equals(url.host()) && url.port() == 3000) {
                            HttpUrl fallbackUrl = url.newBuilder().host("127.0.0.1").build();
                            return chain.proceed(request.newBuilder().url(fallbackUrl).build());
                        }
                        throw e;
                    }
                }
            };

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(fallbackInterceptor)
                    .addInterceptor(new AuthInterceptor(context))
                    .addInterceptor(logging)
                    .connectTimeout(45, TimeUnit.SECONDS)
                    .readTimeout(45, TimeUnit.SECONDS)
                    .writeTimeout(45, TimeUnit.SECONDS)
                    .build();

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();

            apiService = retrofit.create(ApiService.class);
        }

        return apiService;
    }

    public static void resetClient() {
        apiService = null;
    }
}
