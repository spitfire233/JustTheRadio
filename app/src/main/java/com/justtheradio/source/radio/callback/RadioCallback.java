package com.justtheradio.source.radio.callback;

import com.justtheradio.model.radio.RadioStation;

import java.util.List;

public interface RadioCallback {
    void onCountryCodesFetchSuccess(List<String> countryCodes);
    void onLastTimeUpdatedGetSuccess(String lastTimeUpdated);
    void onLastTimeUpdatedNotPresent();
    void onFetchingRadioStationsSuccess(List<RadioStation> radioStations);
    void onSavingRadioStationSuccess(List<RadioStation> radioStations);
    void onGettingRadioStationsFromLocal(List<RadioStation> radioStations);
    void onFailureFetchingCountryCodes(Exception exception);
    void onFailureFetchingStationsFromRemote(Exception exception);
    void onFailureFromLocal(Exception exception);
}
