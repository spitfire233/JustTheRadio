package com.justtheradio.source;

import com.justtheradio.repository.IRadioStationsResponseCallback;

import java.util.List;

public abstract class BaseRadioStationsRemoteDataSource {

    protected static String TAG = BaseRadioStationsRemoteDataSource.class.getSimpleName();
    protected IRadioStationsResponseCallback callback;

    public void setCallback(IRadioStationsResponseCallback callback) {
        this.callback = callback;
    }

    public abstract void getNationalRadioStations(String countryCode, int offset, int limit);

    public abstract void getInternationalRadioStations(List<String> countryCodes, int offset, int limit);
}
