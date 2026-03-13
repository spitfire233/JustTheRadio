package com.justtheradio.source;

import com.justtheradio.repository.IRadioStationsResponseCallback;

public abstract class BaseRadioStationsRemoteDataSource {

    protected static String TAG = BaseRadioStationsRemoteDataSource.class.getSimpleName();
    protected IRadioStationsResponseCallback callback;

    public void setCallback(IRadioStationsResponseCallback callback) {
        this.callback = callback;
    }

    public abstract void getNationalRadios(String countryCode, int offset, int limit);
}
