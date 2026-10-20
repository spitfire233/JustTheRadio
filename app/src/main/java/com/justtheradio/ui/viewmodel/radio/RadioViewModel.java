package com.justtheradio.ui.viewmodel.radio;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.PagingData;
import androidx.paging.PagingLiveData;

import com.justtheradio.model.RadioStation;
import com.justtheradio.model.Result;
import com.justtheradio.repository.RadioRepository;

public class RadioViewModel extends ViewModel {

    private final RadioRepository radioRepository;
    private LiveData<PagingData<RadioStation>> radioStations;

    public RadioViewModel(RadioRepository radioRepository) {
        this.radioRepository = radioRepository;
    }

    public LiveData<PagingData<RadioStation>> getRadioStations(String country) {
        if (radioStations == null) {
            radioStations = PagingLiveData.cachedIn(
                    radioRepository.getRadioStations(country),
                    ViewModelKt.getViewModelScope(this)
            );
        }
        return radioStations;
    }


    public LiveData<Result> fetchCountryCodes() {
        return this.radioRepository.fetchCountryCodes();
    }
}
