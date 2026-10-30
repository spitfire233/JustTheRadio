package com.justtheradio.ui.viewmodel.player;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.justtheradio.model.radio.BaseRadioStation;

/**
 * {@link ViewModel} that holds the radio station currently selected for playback.
 * It is shared between the activity and the fragments: the screens that list stations call
 * {@link #setRadioStation(BaseRadioStation)} when the user taps one, and the main activity
 * observes the selection to update the player and start playing the station.
 */
public class PlayerViewModel extends ViewModel {
    /** The station selected for playback; has no value until the user selects one. */
    private final MutableLiveData<BaseRadioStation> selectedRadioStation;
    /**
     * Constructor for the {@link PlayerViewModel} class
     */
    public PlayerViewModel() {
        selectedRadioStation = new MutableLiveData<>();
    }
    /**
     * Getter for the selected station, so that it can be observed.
     *
     * @return A {@link MutableLiveData} holding the selected station, which has no value
     * until a station is selected
     */
    public MutableLiveData<BaseRadioStation> getSelectedRadioStation() {
        return selectedRadioStation;
    }
    /**
     * Setter for the currently playing radio station.
     * Setting a station with this method notifies all observers attached to
     * {@link selectedRadioStation}
     *
     * @param radioStation The station to play
     */
    public void setRadioStation(BaseRadioStation radioStation) {
        selectedRadioStation.setValue(radioStation);
    }
}
