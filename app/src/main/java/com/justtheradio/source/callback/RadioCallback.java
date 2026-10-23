package com.justtheradio.source.callback;

import java.util.List;

public interface RadioCallback {
    void onCountryCodesFetchSuccess(List<String> countryCodes);
    void onSavingCountryPreferenceSuccess();
    void onGettingCountryPreferenceSuccess(String country);
    void onFailureFetchingCountryCodes(Exception exception);
    void onFailureSavingCountryPreference(Exception exception);
    void onFailureGettingCountryPreference(Exception exception);
}
