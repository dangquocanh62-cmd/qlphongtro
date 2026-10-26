package com.nhatro.app.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.nhatro.app.data.model.Payment;
import com.nhatro.app.data.repository.PaymentRepository;
import com.nhatro.app.data.repository.Resource;
import java.util.List;
import java.util.Map;

public class PaymentViewModel extends AndroidViewModel {
    private final PaymentRepository repository;

    public PaymentViewModel(@NonNull Application application) {
        super(application);
        this.repository = new PaymentRepository(application);
    }

    public LiveData<Resource<List<Payment>>> getPayments(Long invoiceId) {
        return repository.getPayments(invoiceId);
    }

    public LiveData<Resource<Map<String, Object>>> createPayment(Payment payment) {
        return repository.createPayment(payment);
    }
}
