package com.nhatro.app.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.nhatro.app.data.api.ApiClient;
import com.nhatro.app.data.api.ApiService;
import com.nhatro.app.data.model.ApiResponse;
import com.nhatro.app.data.model.MeterReading;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MeterReadingRepository {
    private final ApiService apiService;

    public MeterReadingRepository(Context context) {
        this.apiService = ApiClient.getApiService(context);
    }

    public LiveData<Resource<List<MeterReading>>> getMeterReadings(Long roomId, Long serviceId, Integer month, Integer year) {
        MutableLiveData<Resource<List<MeterReading>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        apiService.getMeterReadings(roomId, serviceId, month, year).enqueue(new Callback<ApiResponse<List<MeterReading>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<MeterReading>>> call, Response<ApiResponse<List<MeterReading>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(Resource.success(response.body().getData()));
                } else {
                    result.setValue(Resource.error(response.body() != null ? response.body().getMessage() : "Lỗi tải chỉ số"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<MeterReading>>> call, Throwable t) {
                result.setValue(Resource.error("Lỗi kết nối: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<Resource<MeterReading>> getLatestReading(Long roomId, Long serviceId) {
        MutableLiveData<Resource<MeterReading>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        apiService.getLatestReading(roomId, serviceId).enqueue(new Callback<ApiResponse<MeterReading>>() {
            @Override
            public void onResponse(Call<ApiResponse<MeterReading>> call, Response<ApiResponse<MeterReading>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(Resource.success(response.body().getData()));
                } else {
                    result.setValue(Resource.success(null)); // Not error, just no previous reading
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<MeterReading>> call, Throwable t) {
                result.setValue(Resource.error("Lỗi kết nối: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<Resource<MeterReading>> createMeterReading(MeterReading reading) {
        MutableLiveData<Resource<MeterReading>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        apiService.createMeterReading(reading).enqueue(new Callback<ApiResponse<MeterReading>>() {
            @Override
            public void onResponse(Call<ApiResponse<MeterReading>> call, Response<ApiResponse<MeterReading>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(Resource.success(response.body().getData()));
                } else {
                    result.setValue(Resource.error(response.body() != null ? response.body().getMessage() : "Lỗi ghi nhận chỉ số"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<MeterReading>> call, Throwable t) {
                result.setValue(Resource.error("Lỗi kết nối: " + t.getMessage()));
            }
        });

        return result;
    }
}
