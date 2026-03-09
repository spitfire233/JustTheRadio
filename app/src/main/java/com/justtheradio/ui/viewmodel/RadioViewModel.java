package com.justtheradio.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.justtheradio.model.RadioStation;
import com.justtheradio.repository.RadioStationsRepository;

import java.util.List;

public class RadioViewModel extends ViewModel {
    private final RadioStationsRepository radioStationsRepository;

    public RadioViewModel(RadioStationsRepository radioStationsRepository) {
        this.radioStationsRepository = radioStationsRepository;
    }

    public LiveData<List<RadioStation>>
    getNationalRadioStations(String countryCode, int offset, int limit) {
        return radioStationsRepository.fetchNationalRadioStations(countryCode, offset, limit);
    }
}
