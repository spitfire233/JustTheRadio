package com.justtheradio.util.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.justtheradio.repository.RadioStationsRepository;

import java.util.List;

import de.sfuhrm.radiobrowser4j.Station;

public class RadioViewModel extends ViewModel {
    private final RadioStationsRepository radioStationsRepository;

    public RadioViewModel(RadioStationsRepository radioStationsRepository) {
        this.radioStationsRepository = radioStationsRepository;
    }

    public LiveData<List<Station>>
    getNationalRadioStations(String countryCode, int limit, int offset) {
        return radioStationsRepository.fetchNationalRadioStations(countryCode, limit, offset);
    }
}
