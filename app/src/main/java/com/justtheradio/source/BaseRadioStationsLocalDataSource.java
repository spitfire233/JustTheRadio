package com.justtheradio.source;

import com.justtheradio.model.RadioStation;
import com.justtheradio.repository.IRadioStationsResponseCallback;

import java.util.List;

public abstract class BaseRadioStationsLocalDataSource {

    protected static final String TAG = BaseRadioStationsLocalDataSource.class.getSimpleName();

    protected IRadioStationsResponseCallback callback;

    public void setCallback(IRadioStationsResponseCallback callback) {
        this.callback = callback;
    }

    public abstract void saveRadioStations(List<RadioStation> radioStationList, boolean isSavingNational);
}
