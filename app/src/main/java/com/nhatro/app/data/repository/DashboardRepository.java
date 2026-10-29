package com.nhatro.app.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.nhatro.app.data.api.ApiClient;
import com.nhatro.app.data.api.ApiService;
import com.nhatro.app.data.model.ApiResponse;
import com.nhatro.app.data.model.DashboardStats;
import com.nhatro.app.data.model.MonthlyRevenue;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardRepository {
    private final ApiService apiService;

    public DashboardRepository(Context context) {
        this.apiService = ApiClient.getApiService(context);
    }

    public LiveData<Resource<DashboardStats>> getStats() {
        return getStats(null, null);
    }

    public LiveData<Resource<DashboardStats>> getStats(Integer month, Integer year) {
        MutableLiveData<Resource<DashboardStats>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        apiService.getDashboardStats(month, year).enqueue(new Callback<ApiResponse<DashboardStats>>() {
            @Override
            public void onResponse(Call<ApiResponse<DashboardStats>> call, Response<ApiResponse<DashboardStats>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(Resource.success(response.body().getData()));
                } else {
                    result.setValue(Resource.error(response.body() != null ? response.body().getMessage() : "Lỗi tải số liệu thống kê"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<DashboardStats>> call, Throwable t) {
                result.setValue(Resource.error("Lỗi kết nối: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<Resource<List<MonthlyRevenue>>> getMonthlyRevenue(int months) {
        MutableLiveData<Resource<List<MonthlyRevenue>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        apiService.getMonthlyRevenue(months).enqueue(new Callback<ApiResponse<List<MonthlyRevenue>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<MonthlyRevenue>>> call, Response<ApiResponse<List<MonthlyRevenue>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(Resource.success(response.body().getData()));
                } else {
                    result.setValue(Resource.error(response.body() != null ? response.body().getMessage() : "Lỗi tải báo cáo doanh thu"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<MonthlyRevenue>>> call, Throwable t) {
                result.setValue(Resource.error("Lỗi kết nối: " + t.getMessage()));
            }
        });

        return result;
    }
}
