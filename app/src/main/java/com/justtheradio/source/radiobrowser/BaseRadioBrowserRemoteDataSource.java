package com.justtheradio.source.radiobrowser;

import com.justtheradio.source.radiobrowser.callback.RadioBrowserCallback;

public abstract class BaseRadioBrowserRemoteDataSource {

    protected RadioBrowserCallback radioBrowserCallback;

    public void setRadioBrowserCallback(RadioBrowserCallback radioBrowserCallback) {
        this.radioBrowserCallback = radioBrowserCallback;
    }

    public abstract void getCountryCodes();
}
