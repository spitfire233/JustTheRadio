package com.justtheradio.source;

import com.justtheradio.repository.IRadioBrowserResponseCallback;

public abstract class BaseRadioBrowserRemoteDataSource {

    protected static String TAG = BaseRadioBrowserRemoteDataSource.class.getSimpleName();
    protected IRadioBrowserResponseCallback callback;

    public void setCallback(IRadioBrowserResponseCallback callback) {
        this.callback = callback;
    }

    public abstract void getNationalRadios(String countryCode, int offset, int limit);
}
