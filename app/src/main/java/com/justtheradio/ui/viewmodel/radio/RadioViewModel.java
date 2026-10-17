package com.justtheradio.ui.viewmodel.radio;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.justtheradio.model.Result;
import com.justtheradio.repository.RadioRepository;

public class RadioViewModel extends ViewModel {

    private final RadioRepository radioRepository;

    public RadioViewModel(RadioRepository radioRepository) {
        this.radioRepository = radioRepository;
    }

    public LiveData<Result> fetchCountryCodes() {
        return this.radioRepository.fetchCountryCodes();
    }

    public LiveData<Result> fetchRadioStations(String country) {
        return this.radioRepository.fetchRadioStations(country);
    }



}
