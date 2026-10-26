package com.nhatro.app.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.nhatro.app.data.api.ApiClient;
import com.nhatro.app.data.api.ApiService;
import com.nhatro.app.data.model.ApiResponse;
import com.nhatro.app.data.model.Room;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoomRepository {
    private final ApiService apiService;

    public RoomRepository(Context context) {
        this.apiService = ApiClient.getApiService(context);
    }

    public LiveData<Resource<List<Room>>> getRooms(String status, String query) {
        MutableLiveData<Resource<List<Room>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        apiService.getRooms(status, query).enqueue(new Callback<ApiResponse<List<Room>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Room>>> call, Response<ApiResponse<List<Room>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(Resource.success(response.body().getData()));
                } else {
                    result.setValue(Resource.error(response.body() != null ? response.body().getMessage() : "Lỗi tải phòng"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Room>>> call, Throwable t) {
                result.setValue(Resource.error("Lỗi kết nối: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<Resource<Room>> getRoomById(Long id) {
        MutableLiveData<Resource<Room>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        apiService.getRoomById(id).enqueue(new Callback<ApiResponse<Room>>() {
            @Override
            public void onResponse(Call<ApiResponse<Room>> call, Response<ApiResponse<Room>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(Resource.success(response.body().getData()));
                } else {
                    result.setValue(Resource.error("Không tìm thấy thông tin phòng"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Room>> call, Throwable t) {
                result.setValue(Resource.error("Lỗi kết nối: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<Resource<Room>> createRoom(Room room) {
        MutableLiveData<Resource<Room>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        apiService.createRoom(room).enqueue(new Callback<ApiResponse<Room>>() {
            @Override
            public void onResponse(Call<ApiResponse<Room>> call, Response<ApiResponse<Room>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(Resource.success(response.body().getData()));
                } else {
                    result.setValue(Resource.error(response.body() != null ? response.body().getMessage() : "Lỗi thêm phòng"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Room>> call, Throwable t) {
                result.setValue(Resource.error("Lỗi kết nối: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<Resource<Room>> updateRoom(Long id, Room room) {
        MutableLiveData<Resource<Room>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        apiService.updateRoom(id, room).enqueue(new Callback<ApiResponse<Room>>() {
            @Override
            public void onResponse(Call<ApiResponse<Room>> call, Response<ApiResponse<Room>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(Resource.success(response.body().getData()));
                } else {
                    result.setValue(Resource.error(response.body() != null ? response.body().getMessage() : "Lỗi cập nhật phòng"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Room>> call, Throwable t) {
                result.setValue(Resource.error("Lỗi kết nối: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<Resource<Void>> deleteRoom(Long id) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        apiService.deleteRoom(id).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(Resource.success(null));
                } else {
                    result.setValue(Resource.error(response.body() != null ? response.body().getMessage() : "Không thể xóa phòng"));
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
