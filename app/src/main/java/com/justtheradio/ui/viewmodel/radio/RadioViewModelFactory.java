package com.justtheradio.ui.viewmodel.radio;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.justtheradio.repository.radio.RadioRepository;

public class RadioViewModelFactory implements ViewModelProvider.Factory {
    private final RadioRepository radioRepository;

    public RadioViewModelFactory(RadioRepository radioRepository) {
        this.radioRepository = radioRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new RadioViewModel(radioRepository);
    }

}
