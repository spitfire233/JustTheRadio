package com.justtheradio.source.radio.callback;

import com.justtheradio.model.RadioStation;

import java.util.List;

public interface RadioCallback {
    void onCountryCodesFetchSuccess(List<String> countryCodes);
    void onSavingRadioStationSuccess(List<RadioStation> radioStations);
    void onFailureFetchingCountryCodes(Exception exception);
    void onFailureFromLocal(Exception exception);
}
