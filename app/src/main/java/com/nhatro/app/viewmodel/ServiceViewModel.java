package com.nhatro.app.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.nhatro.app.data.model.ServiceItem;
import com.nhatro.app.data.repository.Resource;
import com.nhatro.app.data.repository.ServiceRepository;
import java.util.List;

public class ServiceViewModel extends AndroidViewModel {
    private final ServiceRepository repository;
    private final MutableLiveData<Boolean> refreshTrigger = new MutableLiveData<>(true);
    private final LiveData<Resource<List<ServiceItem>>> services;

    public ServiceViewModel(@NonNull Application application) {
        super(application);
        this.repository = new ServiceRepository(application);
        this.services = Transformations.switchMap(refreshTrigger, trigger -> repository.getServices());
    }

    public LiveData<Resource<List<ServiceItem>>> getServices() {
        return services;
    }

    public void refresh() {
        refreshTrigger.setValue(true);
    }

    public LiveData<Resource<ServiceItem>> createService(ServiceItem service) {
        return repository.createService(service);
    }

    public LiveData<Resource<ServiceItem>> updateService(Long id, ServiceItem service) {
        return repository.updateService(id, service);
    }

    public LiveData<Resource<Void>> deleteService(Long id) {
        return repository.deleteService(id);
    }
}
