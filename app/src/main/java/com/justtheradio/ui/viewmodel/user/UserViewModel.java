package com.justtheradio.ui.viewmodel.user;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.justtheradio.model.Result;
import com.justtheradio.repository.UserRepository;

public class UserViewModel extends ViewModel {
    private final UserRepository userRepository;

    public UserViewModel(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public LiveData<Result> saveUserPreference(String key, String country) {
        return this.userRepository.saveUserPreference(key, country);
    }

    public LiveData<Result> getUserPreference(String key) {
        return this.userRepository.getUserPreference(key);
    }

    @Override
    protected void onCleared() {
        this.userRepository.clearData();
    }
}
