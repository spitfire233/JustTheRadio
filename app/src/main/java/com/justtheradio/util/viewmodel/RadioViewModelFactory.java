package com.justtheradio.util.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.justtheradio.repository.RadioStationsRepository;

public class RadioViewModelFactory implements ViewModelProvider.Factory {

    private final RadioStationsRepository radioStationsRepository;

    public RadioViewModelFactory(RadioStationsRepository radioStationsRepository) {
        this.radioStationsRepository = radioStationsRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new RadioViewModel(radioStationsRepository);
    }


}
