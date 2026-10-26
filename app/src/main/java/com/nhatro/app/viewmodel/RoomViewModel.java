package com.nhatro.app.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.nhatro.app.data.model.Room;
import com.nhatro.app.data.repository.Resource;
import com.nhatro.app.data.repository.RoomRepository;
import java.util.List;

public class RoomViewModel extends AndroidViewModel {
    private final RoomRepository repository;
    private final MutableLiveData<FilterParams> filterParams = new MutableLiveData<>();
    private final LiveData<Resource<List<Room>>> rooms;

    public static class FilterParams {
        String status;
        String query;
        FilterParams(String status, String query) {
            this.status = status;
            this.query = query;
        }
    }

    public RoomViewModel(@NonNull Application application) {
        super(application);
        this.repository = new RoomRepository(application);
        this.rooms = Transformations.switchMap(filterParams, params ->
                repository.getRooms(params.status, params.query)
        );
        // Default filter
        filterParams.setValue(new FilterParams(null, null));
    }

    public LiveData<Resource<List<Room>>> getRooms() {
        return rooms;
    }

    public void loadRooms(String status, String query) {
        filterParams.setValue(new FilterParams(status, query));
    }

    public void refresh() {
        FilterParams current = filterParams.getValue();
        if (current != null) {
            filterParams.setValue(current);
        } else {
            filterParams.setValue(new FilterParams(null, null));
        }
    }

    public LiveData<Resource<Room>> getRoomById(Long id) {
        return repository.getRoomById(id);
    }

    public LiveData<Resource<Room>> createRoom(Room room) {
        return repository.createRoom(room);
    }

    public LiveData<Resource<Room>> updateRoom(Long id, Room room) {
        return repository.updateRoom(id, room);
    }

    public LiveData<Resource<Void>> deleteRoom(Long id) {
        return repository.deleteRoom(id);
    }
}
