package com.nhatro.app.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.nhatro.app.data.api.ApiClient;
import com.nhatro.app.data.api.ApiService;
import com.nhatro.app.data.model.ApiResponse;
import com.nhatro.app.data.model.Contract;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContractRepository {
    private final ApiService apiService;

    public ContractRepository(Context context) {
        this.apiService = ApiClient.getApiService(context);
    }

    public LiveData<Resource<List<Contract>>> getContracts(String status, Long roomId, Long tenantId) {
        return getContracts(status, roomId, tenantId, null);
    }

    public LiveData<Resource<List<Contract>>> getContracts(String status, Long roomId, Long tenantId, Boolean expiring) {
        MutableLiveData<Resource<List<Contract>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        apiService.getContracts(status, roomId, tenantId, expiring).enqueue(new Callback<ApiResponse<List<Contract>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Contract>>> call, Response<ApiResponse<List<Contract>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(Resource.success(response.body().getData()));
                } else {
                    result.setValue(Resource.error(response.body() != null ? response.body().getMessage() : "Lỗi tải hợp đồng"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Contract>>> call, Throwable t) {
                result.setValue(Resource.error("Lỗi kết nối: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<Resource<Contract>> getContractById(Long id) {
        MutableLiveData<Resource<Contract>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        apiService.getContractById(id).enqueue(new Callback<ApiResponse<Contract>>() {
            @Override
            public void onResponse(Call<ApiResponse<Contract>> call, Response<ApiResponse<Contract>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(Resource.success(response.body().getData()));
                } else {
                    result.setValue(Resource.error("Không tìm thấy hợp đồng"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Contract>> call, Throwable t) {
                result.setValue(Resource.error("Lỗi kết nối: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<Resource<Contract>> createContract(Long roomId, Long tenantId, String startDate, String endDate, Long deposit, Long rentPrice, List<Long> roommateIds) {
        return createContract(roomId, tenantId, startDate, endDate, deposit, rentPrice, roommateIds, false);
    }

    public LiveData<Resource<Contract>> createContract(Long roomId, Long tenantId, String startDate, String endDate, Long deposit, Long rentPrice, List<Long> roommateIds, boolean forceTerminateExisting) {
        MutableLiveData<Resource<Contract>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        Map<String, Object> body = new HashMap<>();
        body.put("room_id", roomId);
        body.put("tenant_id", tenantId);
        body.put("start_date", startDate);
        body.put("end_date", endDate);
        body.put("deposit", deposit);
        body.put("rent_price", rentPrice);
        body.put("force_terminate_existing", forceTerminateExisting);
        if (roommateIds != null) {
            body.put("roommate_ids", roommateIds);
        }

        apiService.createContract(body).enqueue(new Callback<ApiResponse<Contract>>() {
            @Override
            public void onResponse(Call<ApiResponse<Contract>> call, Response<ApiResponse<Contract>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(Resource.success(response.body().getData()));
                } else {
                    String msg = "Lỗi lập hợp đồng";
                    if (response.body() != null && response.body().getMessage() != null) {
                        msg = response.body().getMessage();
                    } else if (response.errorBody() != null) {
                        try {
                            String errStr = response.errorBody().string();
                            org.json.JSONObject obj = new org.json.JSONObject(errStr);
                            if (obj.has("message")) {
                                msg = obj.getString("message");
                            }
                        } catch (Exception ignored) {}
                    }
                    result.setValue(Resource.error(msg));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Contract>> call, Throwable t) {
                result.setValue(Resource.error("Lỗi kết nối: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<Resource<Contract>> endContract(Long id) {
        MutableLiveData<Resource<Contract>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        apiService.endContract(id).enqueue(new Callback<ApiResponse<Contract>>() {
            @Override
            public void onResponse(Call<ApiResponse<Contract>> call, Response<ApiResponse<Contract>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(Resource.success(response.body().getData()));
                } else {
                    result.setValue(Resource.error(response.body() != null ? response.body().getMessage() : "Lỗi kết thúc hợp đồng"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Contract>> call, Throwable t) {
                result.setValue(Resource.error("Lỗi kết nối: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<Resource<Void>> addRoommate(Long contractId, Long tenantId) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        Map<String, Object> body = new HashMap<>();
        body.put("tenant_id", tenantId);

        apiService.addRoommate(contractId, body).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(Resource.success(null));
                } else {
                    result.setValue(Resource.error(response.body() != null ? response.body().getMessage() : "Lỗi thêm người ở cùng"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                result.setValue(Resource.error("Lỗi kết nối: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<Resource<Contract>> extendContract(Long id, String newEndDate, Long rentPrice) {
        MutableLiveData<Resource<Contract>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        Map<String, Object> body = new HashMap<>();
        body.put("new_end_date", newEndDate);
        if (rentPrice != null) {
            body.put("rent_price", rentPrice);
        }

        apiService.extendContract(id, body).enqueue(new Callback<ApiResponse<Contract>>() {
            @Override
            public void onResponse(Call<ApiResponse<Contract>> call, Response<ApiResponse<Contract>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(Resource.success(response.body().getData()));
                } else {
                    result.setValue(Resource.error(response.body() != null ? response.body().getMessage() : "Lỗi gia hạn hợp đồng"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Contract>> call, Throwable t) {
                result.setValue(Resource.error("Lỗi kết nối: " + t.getMessage()));
            }
        });

        return result;
    }
}
