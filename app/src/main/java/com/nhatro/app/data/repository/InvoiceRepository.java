package com.nhatro.app.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.nhatro.app.data.api.ApiClient;
import com.nhatro.app.data.api.ApiService;
import com.nhatro.app.data.model.ApiResponse;
import com.nhatro.app.data.model.CreateInvoiceRequest;
import com.nhatro.app.data.model.Invoice;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InvoiceRepository {
    private final ApiService apiService;

    public InvoiceRepository(Context context) {
        this.apiService = ApiClient.getApiService(context);
    }

    public LiveData<Resource<List<Invoice>>> getInvoices(String status, Long contractId, Integer month, Integer year) {
        MutableLiveData<Resource<List<Invoice>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        apiService.getInvoices(status, contractId, month, year).enqueue(new Callback<ApiResponse<List<Invoice>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Invoice>>> call, Response<ApiResponse<List<Invoice>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(Resource.success(response.body().getData()));
                } else {
                    result.setValue(Resource.error(response.body() != null ? response.body().getMessage() : "Lỗi tải hóa đơn"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Invoice>>> call, Throwable t) {
                result.setValue(Resource.error("Lỗi kết nối: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<Resource<Invoice>> getInvoiceById(Long id) {
        MutableLiveData<Resource<Invoice>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        apiService.getInvoiceById(id).enqueue(new Callback<ApiResponse<Invoice>>() {
            @Override
            public void onResponse(Call<ApiResponse<Invoice>> call, Response<ApiResponse<Invoice>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(Resource.success(response.body().getData()));
                } else {
                    result.setValue(Resource.error("Không tìm thấy thông tin hóa đơn"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Invoice>> call, Throwable t) {
                result.setValue(Resource.error("Lỗi kết nối: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<Resource<Invoice>> createInvoice(CreateInvoiceRequest request) {
        MutableLiveData<Resource<Invoice>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        apiService.createInvoice(request).enqueue(new Callback<ApiResponse<Invoice>>() {
            @Override
            public void onResponse(Call<ApiResponse<Invoice>> call, Response<ApiResponse<Invoice>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(Resource.success(response.body().getData()));
                } else {
                    result.setValue(Resource.error(response.body() != null ? response.body().getMessage() : "Lỗi lập hóa đơn"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Invoice>> call, Throwable t) {
                result.setValue(Resource.error("Lỗi kết nối: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<Resource<Invoice>> generateMonthlyInvoice(Long contractId, Integer month, Integer year, String dueDate) {
        MutableLiveData<Resource<Invoice>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        Map<String, Object> body = new HashMap<>();
        body.put("contract_id", contractId);
        body.put("month", month);
        body.put("year", year);
        body.put("due_date", dueDate);

        apiService.generateMonthlyInvoice(body).enqueue(new Callback<ApiResponse<Invoice>>() {
            @Override
            public void onResponse(Call<ApiResponse<Invoice>> call, Response<ApiResponse<Invoice>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(Resource.success(response.body().getData()));
                } else {
                    result.setValue(Resource.error(response.body() != null ? response.body().getMessage() : "Lỗi tính tiền hóa đơn"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Invoice>> call, Throwable t) {
                result.setValue(Resource.error("Lỗi kết nối: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<Resource<Void>> deleteInvoice(Long id) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        apiService.deleteInvoice(id).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(Resource.success(null));
                } else {
                    result.setValue(Resource.error(response.body() != null ? response.body().getMessage() : "Lỗi xóa hóa đơn"));
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
