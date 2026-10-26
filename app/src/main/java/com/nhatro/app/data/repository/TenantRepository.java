package com.nhatro.app.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.nhatro.app.data.api.ApiClient;
import com.nhatro.app.data.api.ApiService;
import com.nhatro.app.data.model.ApiResponse;
import com.nhatro.app.data.model.Tenant;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TenantRepository {
    private final ApiService apiService;

    public TenantRepository(Context context) {
        this.apiService = ApiClient.getApiService(context);
    }

    public LiveData<Resource<List<Tenant>>> getTenants(String query) {
        MutableLiveData<Resource<List<Tenant>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        apiService.getTenants(query).enqueue(new Callback<ApiResponse<List<Tenant>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Tenant>>> call, Response<ApiResponse<List<Tenant>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(Resource.success(response.body().getData()));
                } else {
                    result.setValue(Resource.error(response.body() != null ? response.body().getMessage() : "Lỗi tải khách thuê"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Tenant>>> call, Throwable t) {
                result.setValue(Resource.error("Lỗi kết nối: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<Resource<Tenant>> getTenantById(Long id) {
        MutableLiveData<Resource<Tenant>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        apiService.getTenantById(id).enqueue(new Callback<ApiResponse<Tenant>>() {
            @Override
            public void onResponse(Call<ApiResponse<Tenant>> call, Response<ApiResponse<Tenant>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(Resource.success(response.body().getData()));
                } else {
                    result.setValue(Resource.error("Không tìm thấy khách thuê"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Tenant>> call, Throwable t) {
                result.setValue(Resource.error("Lỗi kết nối: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<Resource<Tenant>> createTenant(Tenant tenant) {
        MutableLiveData<Resource<Tenant>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        apiService.createTenant(tenant).enqueue(new Callback<ApiResponse<Tenant>>() {
            @Override
            public void onResponse(Call<ApiResponse<Tenant>> call, Response<ApiResponse<Tenant>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(Resource.success(response.body().getData()));
                } else {
                    result.setValue(Resource.error(response.body() != null ? response.body().getMessage() : "Lỗi thêm khách thuê"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Tenant>> call, Throwable t) {
                result.setValue(Resource.error("Lỗi kết nối: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<Resource<Tenant>> updateTenant(Long id, Tenant tenant) {
        MutableLiveData<Resource<Tenant>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        apiService.updateTenant(id, tenant).enqueue(new Callback<ApiResponse<Tenant>>() {
            @Override
            public void onResponse(Call<ApiResponse<Tenant>> call, Response<ApiResponse<Tenant>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(Resource.success(response.body().getData()));
                } else {
                    result.setValue(Resource.error(response.body() != null ? response.body().getMessage() : "Lỗi cập nhật khách thuê"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Tenant>> call, Throwable t) {
                result.setValue(Resource.error("Lỗi kết nối: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<Resource<Void>> deleteTenant(Long id) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        apiService.deleteTenant(id).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(Resource.success(null));
                } else {
                    result.setValue(Resource.error(response.body() != null ? response.body().getMessage() : "Không thể xóa khách thuê"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                result.setValue(Resource.error("Lỗi kết nối: " + t.getMessage()));
            }
        });

        return result;
    }
}
