package com.nhatro.app.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.nhatro.app.data.model.MeterReading;
import com.nhatro.app.data.repository.MeterReadingRepository;
import com.nhatro.app.data.repository.Resource;
import java.util.List;

public class MeterReadingViewModel extends AndroidViewModel {
    private final MeterReadingRepository repository;
    private final MutableLiveData<FilterParams> filterParams = new MutableLiveData<>();
    private final LiveData<Resource<List<MeterReading>>> meterReadings;

    public static class FilterParams {
        Long roomId;
        Long serviceId;
        Integer month;
        Integer year;
        FilterParams(Long roomId, Long serviceId, Integer month, Integer year) {
            this.roomId = roomId;
            this.serviceId = serviceId;
            this.month = month;
            this.year = year;
        }
    }

    public MeterReadingViewModel(@NonNull Application application) {
        super(application);
        this.repository = new MeterReadingRepository(application);
        this.meterReadings = Transformations.switchMap(filterParams, params ->
                repository.getMeterReadings(params.roomId, params.serviceId, params.month, params.year)
        );
        filterParams.setValue(new FilterParams(null, null, null, null));
    }

    public LiveData<Resource<List<MeterReading>>> getMeterReadings() {
        return meterReadings;
    }

    public void filter(Long roomId, Long serviceId, Integer month, Integer year) {
        filterParams.setValue(new FilterParams(roomId, serviceId, month, year));
    }

    public void refresh() {
        FilterParams current = filterParams.getValue();
        filterParams.setValue(current != null ? current : new FilterParams(null, null, null, null));
    }

    public LiveData<Resource<MeterReading>> getLatestReading(Long roomId, Long serviceId) {
        return repository.getLatestReading(roomId, serviceId);
    }

    public LiveData<Resource<MeterReading>> createMeterReading(MeterReading reading) {
        return repository.createMeterReading(reading);
    }
}
