package com.nhatro.app.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.nhatro.app.data.api.ApiClient;
import com.nhatro.app.data.api.ApiService;
import com.nhatro.app.data.model.ApiResponse;
import com.nhatro.app.data.model.CreateInvoiceRequest;
import com.nhatro.app.data.model.Invoice;
import com.nhatro.app.data.model.InvoiceItem;
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

    private String parseErrorMessage(Response<?> response, String defaultMsg) {
        if (response == null) return defaultMsg;
        if (response.body() instanceof ApiResponse) {
            ApiResponse<?> body = (ApiResponse<?>) response.body();
            if (body.getMessage() != null && !body.getMessage().trim().isEmpty()) {
                return body.getMessage();
            }
        }
        if (response.errorBody() != null) {
            try {
                String errStr = response.errorBody().string();
                org.json.JSONObject obj = new org.json.JSONObject(errStr);
                if (obj.has("message") && !obj.isNull("message")) {
                    return obj.getString("message");
                }
            } catch (Exception ignored) {}
        }
        return defaultMsg;
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
                    result.setValue(Resource.error(parseErrorMessage(response, "Lỗi tải hóa đơn")));
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
                    result.setValue(Resource.error(parseErrorMessage(response, "Không tìm thấy thông tin hóa đơn")));
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
                    result.setValue(Resource.error(parseErrorMessage(response, "Lỗi lập hóa đơn")));
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
                    result.setValue(Resource.error(parseErrorMessage(response, "Lỗi tính tiền hóa đơn")));
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
                    result.setValue(Resource.error(parseErrorMessage(response, "Lỗi xóa hóa đơn")));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                result.setValue(Resource.error("Lỗi kết nối: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<Resource<Map<String, Object>>> bulkGenerateInvoices(Integer month, Integer year, String dueDate) {
        MutableLiveData<Resource<Map<String, Object>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        Map<String, Object> body = new HashMap<>();
        body.put("month", month);
        body.put("year", year);
        if (dueDate != null && !dueDate.trim().isEmpty()) {
            body.put("due_date", dueDate);
        }

        apiService.bulkGenerateInvoices(body).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(Resource.success(response.body().getData()));
                } else {
                    result.setValue(Resource.error(parseErrorMessage(response, "Lỗi lập hóa đơn hàng loạt")));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                result.setValue(Resource.error("Lỗi kết nối: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<Resource<InvoiceItem>> addInvoiceItem(Long invoiceId, String description, Double quantity, Long unitPrice) {
        MutableLiveData<Resource<InvoiceItem>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        Map<String, Object> body = new HashMap<>();
        body.put("description", description);
        body.put("quantity", quantity != null ? quantity : 1.0);
        body.put("unit_price", unitPrice);

        apiService.addInvoiceItem(invoiceId, body).enqueue(new Callback<ApiResponse<InvoiceItem>>() {
            @Override
            public void onResponse(Call<ApiResponse<InvoiceItem>> call, Response<ApiResponse<InvoiceItem>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(Resource.success(response.body().getData()));
                } else {
                    result.setValue(Resource.error(parseErrorMessage(response, "Lỗi thêm mục vào hóa đơn")));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<InvoiceItem>> call, Throwable t) {
                result.setValue(Resource.error("Lỗi kết nối: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<Resource<Void>> removeInvoiceItem(Long invoiceId, Long itemId) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        apiService.removeInvoiceItem(invoiceId, itemId).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(Resource.success(null));
                } else {
                    result.setValue(Resource.error(parseErrorMessage(response, "Lỗi xóa mục chi phí")));
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
