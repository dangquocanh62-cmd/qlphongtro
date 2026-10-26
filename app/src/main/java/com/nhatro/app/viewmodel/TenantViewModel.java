package com.nhatro.app.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.nhatro.app.data.model.Tenant;
import com.nhatro.app.data.repository.Resource;
import com.nhatro.app.data.repository.TenantRepository;
import java.util.List;

public class TenantViewModel extends AndroidViewModel {
    private final TenantRepository repository;
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final LiveData<Resource<List<Tenant>>> tenants;

    public TenantViewModel(@NonNull Application application) {
        super(application);
        this.repository = new TenantRepository(application);
        this.tenants = Transformations.switchMap(searchQuery, repository::getTenants);
    }

    public LiveData<Resource<List<Tenant>>> getTenants() {
        return tenants;
    }

    public void search(String query) {
        searchQuery.setValue(query);
    }

    public void refresh() {
        searchQuery.setValue(searchQuery.getValue());
    }

    public LiveData<Resource<Tenant>> getTenantById(Long id) {
        return repository.getTenantById(id);
    }

    public LiveData<Resource<Tenant>> createTenant(Tenant tenant) {
        return repository.createTenant(tenant);
    }

    public LiveData<Resource<Tenant>> updateTenant(Long id, Tenant tenant) {
        return repository.updateTenant(id, tenant);
    }

    public LiveData<Resource<Void>> deleteTenant(Long id) {
        return repository.deleteTenant(id);
    }
}
