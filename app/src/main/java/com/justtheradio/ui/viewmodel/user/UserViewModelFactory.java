package com.justtheradio.ui.viewmodel.user;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.justtheradio.repository.RadioRepository;
import com.justtheradio.repository.UserRepository;
import com.justtheradio.ui.viewmodel.radio.RadioViewModel;

public class UserViewModelFactory implements ViewModelProvider.Factory {
    private final UserRepository userRepository;

    public UserViewModelFactory(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new UserViewModel(userRepository);
    }
}
