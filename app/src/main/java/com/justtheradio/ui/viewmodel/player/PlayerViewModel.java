package com.justtheradio.ui.viewmodel.player;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.justtheradio.model.radio.BaseRadioStation;

public class PlayerViewModel extends ViewModel {
    private final MutableLiveData<BaseRadioStation> selectedRadioStation;

    public PlayerViewModel() {
        selectedRadioStation = new MutableLiveData<>();
    }

    public MutableLiveData<BaseRadioStation> getSelectedRadioStation() {
        return selectedRadioStation;
    }

    public void setRadioStation(BaseRadioStation radioStation) {
        selectedRadioStation.setValue(radioStation);
    }
}
