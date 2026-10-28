package com.justtheradio.source.callback;

import com.justtheradio.model.radio.BaseRadioStation;

import java.util.List;

public interface RadioCallback {
    void onCountryCodesFetchSuccess(List<String> countryCodes);
    void onSavingCountryPreferenceSuccess();
    void onGettingCountryPreferenceSuccess(String country);
    void onRadioStationModifiedFavouriteStateSuccess(BaseRadioStation station);
    void onFailureFetchingCountryCodes(Exception exception);
    void onFailureSavingCountryPreference(Exception exception);
    void onFailureGettingCountryPreference(Exception exception);
    void onFailureModifyingStationSavedStATE(Exception e);
}
