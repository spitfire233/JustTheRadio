package com.justtheradio.ui.viewmodel.radio;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.justtheradio.model.Result;
import com.justtheradio.repository.radio.RadioRepository;

public class RadioViewModel extends ViewModel {

    private final RadioRepository radioRepository;

    public RadioViewModel(RadioRepository radioRepository) {
        this.radioRepository = radioRepository;
    }

    public MutableLiveData<Result> fetchCountryCodes() {
        return this.radioRepository.fetchCountryCodes();
    }


}
