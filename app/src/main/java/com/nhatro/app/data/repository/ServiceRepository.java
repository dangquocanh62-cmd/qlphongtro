package com.nhatro.app.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.nhatro.app.data.api.ApiClient;
import com.nhatro.app.data.api.ApiService;
import com.nhatro.app.data.model.ApiResponse;
import com.nhatro.app.data.model.ServiceItem;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServiceRepository {
    private final ApiService apiService;

    public ServiceRepository(Context context) {
        this.apiService = ApiClient.getApiService(context);
    }

    public LiveData<Resource<List<ServiceItem>>> getServices() {
        MutableLiveData<Resource<List<ServiceItem>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        apiService.getServices().enqueue(new Callback<ApiResponse<List<ServiceItem>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ServiceItem>>> call, Response<ApiResponse<List<ServiceItem>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(Resource.success(response.body().getData()));
                } else {
                    result.setValue(Resource.error(response.body() != null ? response.body().getMessage() : "Lỗi tải dịch vụ"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ServiceItem>>> call, Throwable t) {
                result.setValue(Resource.error("Lỗi kết nối: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<Resource<ServiceItem>> createService(ServiceItem service) {
        MutableLiveData<Resource<ServiceItem>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        apiService.createService(service).enqueue(new Callback<ApiResponse<ServiceItem>>() {
            @Override
            public void onResponse(Call<ApiResponse<ServiceItem>> call, Response<ApiResponse<ServiceItem>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(Resource.success(response.body().getData()));
                } else {
                    result.setValue(Resource.error(response.body() != null ? response.body().getMessage() : "Lỗi thêm dịch vụ"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ServiceItem>> call, Throwable t) {
                result.setValue(Resource.error("Lỗi kết nối: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<Resource<ServiceItem>> updateService(Long id, ServiceItem service) {
        MutableLiveData<Resource<ServiceItem>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        apiService.updateService(id, service).enqueue(new Callback<ApiResponse<ServiceItem>>() {
            @Override
            public void onResponse(Call<ApiResponse<ServiceItem>> call, Response<ApiResponse<ServiceItem>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(Resource.success(response.body().getData()));
                } else {
                    result.setValue(Resource.error(response.body() != null ? response.body().getMessage() : "Lỗi cập nhật dịch vụ"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ServiceItem>> call, Throwable t) {
                result.setValue(Resource.error("Lỗi kết nối: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<Resource<Void>> deleteService(Long id) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        apiService.deleteService(id).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(Resource.success(null));
                } else {
                    result.setValue(Resource.error(response.body() != null ? response.body().getMessage() : "Không thể xóa dịch vụ"));
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
