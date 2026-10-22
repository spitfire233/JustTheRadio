package com.justtheradio.source.radio;

import androidx.datastore.preferences.core.Preferences;
import androidx.paging.PagingSource;

import com.justtheradio.model.RadioRemoteKey;
import com.justtheradio.model.RadioStation;
import com.justtheradio.source.radio.callback.RadioCallback;

import java.util.List;

import io.reactivex.rxjava3.core.Single;

public abstract class BaseRadioLocalDataSource {
    protected RadioCallback radioCallback;

    public void setCallback(RadioCallback radioCallback) {
        this.radioCallback = radioCallback;
    }

    public abstract Single<Preferences> saveLastTimeFetched(long lastTimeUpdated);

    public abstract Single<String> getLastTimeFetched();

    public abstract RadioRemoteKey getRadioRemoteKey();

    public abstract void savePage(boolean isRefresh, List<RadioStation> stations, RadioRemoteKey key);

    public abstract List<RadioStation> getFavouriteStations();

    public abstract void getFavouriteStationsAsync();

    public abstract void saveRadioStations(List<RadioStation> radioStationList);

    public abstract PagingSource<Integer, RadioStation> getRadioDAOPagingSource();

    public abstract void onDestroy();
}
