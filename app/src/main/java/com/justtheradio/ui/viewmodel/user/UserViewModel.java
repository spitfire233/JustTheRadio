package com.justtheradio.ui.viewmodel.user;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.justtheradio.model.Result;
import com.justtheradio.repository.UserRepository;

public class UserViewModel extends ViewModel {
    private final UserRepository userRepository;

    public UserViewModel(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public MutableLiveData<Result> saveCountryPreference(String country) {
        return this.userRepository.saveCountryPreference(country);
    }



}
