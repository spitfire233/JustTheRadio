package com.justtheradio.ui.viewmodel.player;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.justtheradio.model.radio.RadioStation;

public class PlayerViewModel extends ViewModel {
    private final MutableLiveData<RadioStation> selectedRadioStation;

    public PlayerViewModel() {
        selectedRadioStation = new MutableLiveData<>();
    }

    public MutableLiveData<RadioStation> getSelectedRadioStation() {
        return selectedRadioStation;
    }

    public void setRadioStation(RadioStation radioStation) {
        selectedRadioStation.setValue(radioStation);
    }
}
