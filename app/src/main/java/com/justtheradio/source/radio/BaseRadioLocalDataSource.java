package com.justtheradio.source.radio;

import androidx.paging.PagingSource;

import com.justtheradio.model.RadioStation;
import com.justtheradio.source.radio.callback.RadioCallback;

import java.util.List;

public abstract class BaseRadioLocalDataSource {
    protected RadioCallback radioCallback;

    public void setCallback(RadioCallback radioCallback) {
        this.radioCallback = radioCallback;
    }

    public abstract void saveLastTimeFetched(long lastTimeUpdated);

    public abstract void saveRadioStations(List<RadioStation> radioStationList);

    public abstract PagingSource<Integer, RadioStation> getRadioDAOPagingSource();

    public abstract void onDestroy();
}
