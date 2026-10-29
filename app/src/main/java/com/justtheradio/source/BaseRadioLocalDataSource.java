package com.justtheradio.source;

import androidx.annotation.Nullable;
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

/**
 * Base class for the local data sources of the radio stations and the user cached data.
 */
public abstract class BaseRadioLocalDataSource {
    /** Callback notified of the outcome of the asynchronous operations. */
    protected RadioCallback radioCallback;
    /**
     * Registers the callback that will be notified of the outcome of the asynchronous
     * operations.
     *
     * @param radioCallback The callback to notify
     */
    public void setCallback(RadioCallback radioCallback) {
        this.radioCallback = radioCallback;
    }
    /**
     * Saves the time of the last refresh of the cached stations, used to decide when the
     * cache has expired.
     *
     * @param lastTimeUpdated The time of the refresh, as a timestamp in milliseconds
     * @return A {@link Single} emitting the updated {@link Preferences} once the value has
     * been saved
     */
    public abstract Single<Preferences> saveLastTimeFetched(long lastTimeUpdated);
    /**
     * Reads the key that tells where the next page of stations starts and whether the end
     * of the remote list has been reached.
     * This should be implemented as a blocking call and thus
     * must not be made from the main thread.
     *
     * @return The stored {@link RadioRemoteKey}, or {@code null} if nothing has been
     * cached yet
     */
    @Nullable
    public abstract RadioRemoteKey getRadioRemoteKey();
    /**
     * Saves a page of stations together with the key of the next page.
     * This should be implemented aa blocking call and must thus
     * must not be made from the main thread.
     *
     * @param isRefresh {@code true} if the page comes from a refresh, in which case the
     *                  previously cached data is replaced; {@code false} if the page is
     *                  appended to it
     * @param stations  The stations of the page
     * @param key       The key describing where the next page starts
     */
    public abstract void savePage(boolean isRefresh, List<RadioStation> stations, RadioRemoteKey key);
    /**
     * Saves the country whose stations must be shown.
     * The outcome should be reported through the {@link RadioCallback}.
     *
     * @param country The country code to save
     */
    public abstract void saveCountryPreference(String country);
    /**
     * Reads the saved country preference.
     * The outcome should be reported through the {@link RadioCallback}.
     */
    public abstract void getCountryPreferenceAsync();
    /**
     * Reads the saved country preference, returning the result as a reactive {@link Single} stream
     *
     * @return A {@link Single} emitting the saved country code
     */
    public abstract Single<String> getCountryPreference();
    /**
     * Provides the {@link PagingSource} that reads the cached stations from the database.
     * This should be implemented as a blocking call and thus should not be called from the main
     * thread
     *
     * @return A {@link PagingSource} over the cached stations
     */
    public abstract PagingSource<Integer, RadioStation> getRadioDAOPagingSource();
    /**
     * Observes the identifiers of the stations marked as favourite.
     *
     * @return A {@link Flowable} emitting the set of favourite station UUIDs every time it changes
     */
    public abstract Flowable<Set<UUID>> observeFavouriteUUIDs();
    /**
     * Observes the full list of favourite stations.
     *
     * @return A {@link Flowable} emitting the list of favourites every time it changes
     */
    public abstract Flowable<List<FavouriteStation>> observeFavourites();
    /**
     * Adds a station to the favourites.
     * The outcome should be reported through the {@link RadioCallback}
     *
     * @param station The station to mark as favourite
     */
    public abstract void addFavourite(BaseRadioStation station);
    /**
     * Removes a station from the favourites.
     * The outcome should be reported through the {@link RadioCallback}
     *
     * @param station The station to remove from the favourites
     */
    public abstract void removeFavourite(BaseRadioStation station);
    /**
     * Releases the resources held by the data sources.
     * To be called when the data source is no longer needed.
     */
    public abstract void onDestroy();
}
