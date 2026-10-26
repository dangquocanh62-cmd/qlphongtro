package com.nhatro.app.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.nhatro.app.data.model.DashboardStats;
import com.nhatro.app.data.repository.DashboardRepository;
import com.nhatro.app.data.repository.Resource;

public class DashboardViewModel extends AndroidViewModel {
    private final DashboardRepository repository;

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        this.repository = new DashboardRepository(application);
    }

    public LiveData<Resource<DashboardStats>> getDashboardStats() {
        return repository.getStats();
    }
}
