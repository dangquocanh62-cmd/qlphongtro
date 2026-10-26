package com.nhatro.app.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.nhatro.app.data.api.ApiClient;
import com.nhatro.app.data.api.ApiService;
import com.nhatro.app.data.model.ApiResponse;
import com.nhatro.app.data.model.Payment;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentRepository {
    private final ApiService apiService;

    public PaymentRepository(Context context) {
        this.apiService = ApiClient.getApiService(context);
    }

    public LiveData<Resource<List<Payment>>> getPayments(Long invoiceId) {
        MutableLiveData<Resource<List<Payment>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        apiService.getPayments(invoiceId).enqueue(new Callback<ApiResponse<List<Payment>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Payment>>> call, Response<ApiResponse<List<Payment>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(Resource.success(response.body().getData()));
                } else {
                    result.setValue(Resource.error(response.body() != null ? response.body().getMessage() : "Lỗi tải lịch sử thanh toán"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Payment>>> call, Throwable t) {
                result.setValue(Resource.error("Lỗi kết nối: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<Resource<Map<String, Object>>> createPayment(Payment payment) {
        MutableLiveData<Resource<Map<String, Object>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        apiService.createPayment(payment).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(Resource.success(response.body().getData()));
                } else {
                    result.setValue(Resource.error(response.body() != null ? response.body().getMessage() : "Lỗi ghi nhận thanh toán"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                result.setValue(Resource.error("Lỗi kết nối: " + t.getMessage()));
            }
        });

        return result;
    }
}
