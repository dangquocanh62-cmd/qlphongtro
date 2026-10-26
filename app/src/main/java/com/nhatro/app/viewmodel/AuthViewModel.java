package com.nhatro.app.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.nhatro.app.data.model.LoginResponse;
import com.nhatro.app.data.repository.AuthRepository;
import com.nhatro.app.data.repository.Resource;

public class AuthViewModel extends AndroidViewModel {
    private final AuthRepository authRepository;

    public AuthViewModel(@NonNull Application application) {
        super(application);
        this.authRepository = new AuthRepository(application);
    }

    public LiveData<Resource<LoginResponse>> login(String email, String password) {
        return authRepository.login(email, password);
    }

    public LiveData<Resource<LoginResponse>> register(String email, String password, String fullName, String phone) {
        return authRepository.register(email, password, fullName, phone);
    }
}
