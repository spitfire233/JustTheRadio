package com.justtheradio.source.callback;

import com.justtheradio.model.radio.BaseRadioStation;

import java.util.List;

/**
 * Callback through which the radio data sources report the outcome of their asynchronous
 * operations to the repository.
 * Every operation has a success method and, when it can fail, a matching failure method.
 */
public interface RadioCallback {
    /**
     * Called when the list of the available country codes has been fetched.
     *
     * @param countryCodes The country codes returned by the remote source
     */
    void onCountryCodesFetchSuccess(List<String> countryCodes);
    /** Called when the country preference has been saved. */
    void onSavingCountryPreferenceSuccess();
    /**
     * Called when the country preference has been read.
     *
     * @param country The saved country code
     */
    void onGettingCountryPreferenceSuccess(String country);
    /**
     * Called when the favourite state of a station has been modified
     * (the station has been added to or removed from the favourites).
     *
     * @param station The station whose favourite state has changed
     */
    void onRadioStationModifiedFavouriteStateSuccess(BaseRadioStation station);
    /**
     * Called when fetching the list of country codes failed.
     *
     * @param exception The error that occurred
     */
    void onFailureFetchingCountryCodes(Exception exception);
    /**
     * Called when saving the country preference failed.
     *
     * @param exception The error that occurred
     */
    void onFailureSavingCountryPreference(Exception exception);
    /**
     * Called when reading the country preference failed.
     *
     * @param exception The error that occurred
     */
    void onFailureGettingCountryPreference(Exception exception);
    /**
     * Called when adding or removing a station from the favourites failed.
     *
     * @param exception The error that occurred
     */
    void onFailureModifyingStationSavedState(Exception exception);
}
