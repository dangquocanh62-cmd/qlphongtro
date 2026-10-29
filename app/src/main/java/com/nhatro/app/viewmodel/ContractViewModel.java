package com.nhatro.app.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.nhatro.app.data.model.Contract;
import com.nhatro.app.data.repository.ContractRepository;
import com.nhatro.app.data.repository.Resource;
import java.util.List;

public class ContractViewModel extends AndroidViewModel {
    private final ContractRepository repository;

    public static class FilterParams {
        String status;
        Boolean expiring;
        public FilterParams(String status, Boolean expiring) {
            this.status = status;
            this.expiring = expiring;
        }
    }

    private final MutableLiveData<FilterParams> filterParams = new MutableLiveData<>(new FilterParams("ACTIVE", null));
    private final LiveData<Resource<List<Contract>>> contracts;

    public ContractViewModel(@NonNull Application application) {
        super(application);
        this.repository = new ContractRepository(application);
        this.contracts = Transformations.switchMap(filterParams, params ->
                repository.getContracts(params.status, null, null, params.expiring)
        );
    }

    public LiveData<Resource<List<Contract>>> getContracts() {
        return contracts;
    }

    public void filterByStatus(String status) {
        filterParams.setValue(new FilterParams(status, null));
    }

    public void filterExpiring() {
        filterParams.setValue(new FilterParams("ACTIVE", true));
    }

    public void refresh() {
        FilterParams current = filterParams.getValue();
        filterParams.setValue(current != null ? current : new FilterParams("ACTIVE", null));
    }

    public LiveData<Resource<Contract>> getContractById(Long id) {
        return repository.getContractById(id);
    }

    public LiveData<Resource<Contract>> createContract(Long roomId, Long tenantId, String startDate, String endDate, Long deposit, Long rentPrice, List<Long> roommateIds) {
        return repository.createContract(roomId, tenantId, startDate, endDate, deposit, rentPrice, roommateIds, false);
    }

    public LiveData<Resource<Contract>> createContract(Long roomId, Long tenantId, String startDate, String endDate, Long deposit, Long rentPrice, List<Long> roommateIds, boolean forceTerminateExisting) {
        return repository.createContract(roomId, tenantId, startDate, endDate, deposit, rentPrice, roommateIds, forceTerminateExisting);
    }

    public LiveData<Resource<Contract>> endContract(Long id) {
        return repository.endContract(id);
    }

    public LiveData<Resource<Contract>> extendContract(Long id, String newEndDate, Long rentPrice) {
        return repository.extendContract(id, newEndDate, rentPrice);
    }

    public LiveData<Resource<Void>> addRoommate(Long contractId, Long tenantId) {
        return repository.addRoommate(contractId, tenantId);
    }
}
