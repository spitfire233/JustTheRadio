package com.justtheradio.source.radiobrowser;

import com.justtheradio.source.radiobrowser.callback.RadioCallback;

public abstract class BaseRadioBrowserRemoteDataSource {

    protected RadioCallback radioCallback;

    public void setRadioBrowserCallback(RadioCallback radioCallback) {
        this.radioCallback = radioCallback;
    }

    public abstract void getCountryCodes();
}
