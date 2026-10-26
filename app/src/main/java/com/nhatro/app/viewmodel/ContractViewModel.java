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
    private final MutableLiveData<String> statusFilter = new MutableLiveData<>("ACTIVE");
    private final LiveData<Resource<List<Contract>>> contracts;

    public ContractViewModel(@NonNull Application application) {
        super(application);
        this.repository = new ContractRepository(application);
        this.contracts = Transformations.switchMap(statusFilter, status ->
                repository.getContracts(status, null, null)
        );
    }

    public LiveData<Resource<List<Contract>>> getContracts() {
        return contracts;
    }

    public void filterByStatus(String status) {
        statusFilter.setValue(status);
    }

    public void refresh() {
        statusFilter.setValue(statusFilter.getValue());
    }

    public LiveData<Resource<Contract>> getContractById(Long id) {
        return repository.getContractById(id);
    }

    public LiveData<Resource<Contract>> createContract(Long roomId, Long tenantId, String startDate, String endDate, Long deposit, Long rentPrice, List<Long> roommateIds) {
        return repository.createContract(roomId, tenantId, startDate, endDate, deposit, rentPrice, roommateIds);
    }

    public LiveData<Resource<Contract>> endContract(Long id) {
        return repository.endContract(id);
    }

    public LiveData<Resource<Void>> addRoommate(Long contractId, Long tenantId) {
        return repository.addRoommate(contractId, tenantId);
    }
}
