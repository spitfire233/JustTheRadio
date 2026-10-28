package com.justtheradio.source;

import androidx.datastore.preferences.core.Preferences;
import androidx.paging.PagingSource;

import com.justtheradio.model.radio.BaseRadioStation;
import com.justtheradio.model.radio.FavouriteStation;
import com.justtheradio.model.radio.RadioRemoteKey;
import com.justtheradio.model.radio.RadioStation;
import com.justtheradio.source.callback.RadioCallback;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import io.reactivex.rxjava3.core.Flowable;
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

    public abstract void saveCountryPreference(String country);

    public abstract void getCountryPreferenceAsync();

    public abstract Single<String> getCountryPreference();

    public abstract PagingSource<Integer, RadioStation> getRadioDAOPagingSource();

    public abstract Flowable<Set<UUID>> observeFavouriteUUIDs();

    public abstract Flowable<List<FavouriteStation>> observeFavourites();

    public abstract void addFavourite(BaseRadioStation station);

    public abstract void removeFavourite(BaseRadioStation station);

    public abstract void onDestroy();
}
