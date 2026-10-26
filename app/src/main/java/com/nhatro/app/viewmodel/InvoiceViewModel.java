package com.nhatro.app.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.nhatro.app.data.model.CreateInvoiceRequest;
import com.nhatro.app.data.model.Invoice;
import com.nhatro.app.data.repository.InvoiceRepository;
import com.nhatro.app.data.repository.Resource;
import java.util.List;

public class InvoiceViewModel extends AndroidViewModel {
    private final InvoiceRepository repository;
    private final MutableLiveData<FilterParams> filterParams = new MutableLiveData<>();
    private final LiveData<Resource<List<Invoice>>> invoices;

    public static class FilterParams {
        String status;
        Long contractId;
        Integer month;
        Integer year;
        FilterParams(String status, Long contractId, Integer month, Integer year) {
            this.status = status;
            this.contractId = contractId;
            this.month = month;
            this.year = year;
        }
    }

    public InvoiceViewModel(@NonNull Application application) {
        super(application);
        this.repository = new InvoiceRepository(application);
        this.invoices = Transformations.switchMap(filterParams, params ->
                repository.getInvoices(params.status, params.contractId, params.month, params.year)
        );
        filterParams.setValue(new FilterParams(null, null, null, null));
    }

    public LiveData<Resource<List<Invoice>>> getInvoices() {
        return invoices;
    }

    public void filter(String status, Long contractId, Integer month, Integer year) {
        filterParams.setValue(new FilterParams(status, contractId, month, year));
    }

    public void refresh() {
        FilterParams current = filterParams.getValue();
        filterParams.setValue(current != null ? current : new FilterParams(null, null, null, null));
    }

    public LiveData<Resource<Invoice>> getInvoiceById(Long id) {
        return repository.getInvoiceById(id);
    }

    public LiveData<Resource<Invoice>> createInvoice(CreateInvoiceRequest request) {
        return repository.createInvoice(request);
    }

    public LiveData<Resource<Invoice>> generateMonthlyInvoice(Long contractId, Integer month, Integer year, String dueDate) {
        return repository.generateMonthlyInvoice(contractId, month, year, dueDate);
    }

    public LiveData<Resource<Void>> deleteInvoice(Long id) {
        return repository.deleteInvoice(id);
    }
}
