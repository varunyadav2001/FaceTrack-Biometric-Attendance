package com.example.attendance_system.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.attendance_system.models.User;
import com.example.attendance_system.repository.AuthRepository;

public class AuthViewModel extends AndroidViewModel {
    private final AuthRepository authRepository;

    private final MutableLiveData<User> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> passwordResetLiveData = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository();
    }

    public LiveData<User> getUserLiveData() {
        return userLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public LiveData<Boolean> getLoadingLiveData() {
        return loadingLiveData;
    }

    public LiveData<Boolean> getPasswordResetLiveData() {
        return passwordResetLiveData;
    }

    public void login(String email, String password, String selectedRole) {
        loadingLiveData.setValue(true);
        authRepository.login(getApplication().getApplicationContext(), email, password, selectedRole, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                loadingLiveData.setValue(false);
                userLiveData.setValue(user);
            }

            @Override
            public void onError(String message) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(message);
            }
        });
    }

    public void resetPassword(String email) {
        loadingLiveData.setValue(true);
        authRepository.resetPassword(email, new AuthRepository.ResetPasswordCallback() {
            @Override
            public void onSuccess() {
                loadingLiveData.setValue(false);
                passwordResetLiveData.setValue(true);
            }

            @Override
            public void onError(String message) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(message);
            }
        });
    }
}
