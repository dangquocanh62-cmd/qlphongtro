package com.nhatro.app.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.nhatro.app.data.api.ApiClient;
import com.nhatro.app.data.api.ApiService;
import com.nhatro.app.data.model.ApiResponse;
import com.nhatro.app.data.model.LoginRequest;
import com.nhatro.app.data.model.LoginResponse;
import com.nhatro.app.utils.SharedPrefsHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.nhatro.app.data.model.RegisterRequest;

public class AuthRepository {
    private final ApiService apiService;
    private final Context context;

    public AuthRepository(Context context) {
        this.context = context.getApplicationContext();
        this.apiService = ApiClient.getApiService(context);
    }

    public LiveData<Resource<LoginResponse>> login(String email, String password) {
        MutableLiveData<Resource<LoginResponse>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        apiService.login(new LoginRequest(email, password)).enqueue(new Callback<ApiResponse<LoginResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<LoginResponse>> call, Response<ApiResponse<LoginResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    LoginResponse loginData = response.body().getData();
                    // Save to SharedPreferences
                    if (loginData != null && loginData.getUser() != null) {
                        SharedPrefsHelper.getInstance(context).saveAuth(
                                loginData.getToken(),
                                loginData.getUser().getEmail(),
                                loginData.getUser().getFullName(),
                                loginData.getUser().getRole()
                        );
                    }
                    result.setValue(Resource.success(loginData));
                } else {
                    String msg = "Đăng nhập không thành công";
                    if (response.body() != null && response.body().getMessage() != null) {
                        msg = response.body().getMessage();
                    } else if (response.code() == 401) {
                        msg = "Sai email hoặc mật khẩu";
                    }
                    result.setValue(Resource.error(msg));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<LoginResponse>> call, Throwable t) {
                result.setValue(Resource.error("Lỗi kết nối máy chủ: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<Resource<LoginResponse>> register(String email, String password, String fullName, String phone) {
        MutableLiveData<Resource<LoginResponse>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        RegisterRequest request = new RegisterRequest(email, password, fullName, phone, "OWNER");
        apiService.register(request).enqueue(new Callback<ApiResponse<LoginResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<LoginResponse>> call, Response<ApiResponse<LoginResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    LoginResponse loginData = response.body().getData();
                    if (loginData != null && loginData.getToken() != null && loginData.getUser() != null) {
                        SharedPrefsHelper.getInstance(context).saveAuth(
                                loginData.getToken(),
                                loginData.getUser().getEmail(),
                                loginData.getUser().getFullName(),
                                loginData.getUser().getRole()
                        );
                        result.setValue(Resource.success(loginData));
                    } else {
                        // If token wasn't included directly, immediately perform login
                        apiService.login(new LoginRequest(email, password)).enqueue(new Callback<ApiResponse<LoginResponse>>() {
                            @Override
                            public void onResponse(Call<ApiResponse<LoginResponse>> call2, Response<ApiResponse<LoginResponse>> res2) {
                                if (res2.isSuccessful() && res2.body() != null && res2.body().isSuccess()) {
                                    LoginResponse data2 = res2.body().getData();
                                    if (data2 != null && data2.getUser() != null) {
                                        SharedPrefsHelper.getInstance(context).saveAuth(
                                                data2.getToken(),
                                                data2.getUser().getEmail(),
                                                data2.getUser().getFullName(),
                                                data2.getUser().getRole()
                                        );
                                    }
                                    result.setValue(Resource.success(data2));
                                } else {
                                    result.setValue(Resource.success(loginData));
                                }
                            }

                            @Override
                            public void onFailure(Call<ApiResponse<LoginResponse>> call2, Throwable t) {
                                result.setValue(Resource.success(loginData));
                            }
                        });
                    }
                } else {
                    String msg = "Đăng ký không thành công";
                    if (response.body() != null && response.body().getMessage() != null) {
                        msg = response.body().getMessage();
                    }
                    result.setValue(Resource.error(msg));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<LoginResponse>> call, Throwable t) {
                result.setValue(Resource.error("Lỗi kết nối máy chủ: " + t.getMessage()));
            }
        });

        return result;
    }
}
