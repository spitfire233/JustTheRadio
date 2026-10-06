package com.justtheradio.ui.viewmodel.radio;

import androidx.lifecycle.ViewModel;

import com.justtheradio.repository.radiobrowser.RadioRepository;

public class RadioViewModel extends ViewModel {

    private final RadioRepository radioRepository;

    public RadioViewModel(RadioRepository radioRepository) {
        this.radioRepository = radioRepository;
    }
}
