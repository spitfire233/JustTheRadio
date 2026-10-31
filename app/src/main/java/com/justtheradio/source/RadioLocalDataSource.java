package com.justtheradio.source;

import static com.justtheradio.utils.constant.LocalCacheConstants.CHOSEN_COUNTRY_KEY;
import static com.justtheradio.utils.constant.LocalCacheConstants.LAST_TIME_UPDATED_KEY;
import static com.justtheradio.utils.constant.LocalCacheConstants.NO_KEY_WITH_THAT_NAME_IN_DATASTORE;

import android.app.Application;

import androidx.annotation.Nullable;
import androidx.datastore.preferences.core.Preferences;
import androidx.paging.PagingSource;

import com.justtheradio.database.RadioDAO;
import com.justtheradio.database.RadioRoomDatabase;
import com.justtheradio.model.radio.BaseRadioStation;
import com.justtheradio.model.radio.FavouriteStation;
import com.justtheradio.model.radio.RadioRemoteKey;
import com.justtheradio.model.radio.RadioStation;
import com.justtheradio.utils.datastore.DataStoreManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * Implementation of {@link BaseRadioLocalDataSource}.
 * Local data source that interacts with the Room database and the device cache.
 */
public class RadioLocalDataSource extends BaseRadioLocalDataSource  {
    /** Data access object of the Room database. */
    private final RadioDAO radioDAO;
    /** Manager of the DataStore */
    private final DataStoreManager datastore;
    /** Holds the active subscriptions, so that they can be canceled all together. */
    private final CompositeDisposable disposables;
    /**
     * Constructor for the {@link RadioLocalDataSource} class
     * @param application The application, used to get the datastore manager
     * @param radioRoomDatabase The Room database from which the DAO is obtained
     */
    public RadioLocalDataSource(Application application, RadioRoomDatabase radioRoomDatabase) {
        this.radioDAO = radioRoomDatabase.radioDAO();
        datastore = DataStoreManager.getInstance(application);
        disposables = new CompositeDisposable();
    }
    /**
     * Implementation of {@link BaseRadioLocalDataSource#saveLastTimeFetched(long)}.
     * Saves the time of the last refresh of the cached stations in the DataStore, stored
     * as a string.
     *
     * @param lastTimeUpdated The time of the refresh, as a timestamp in milliseconds
     * @return A {@link Single} emitting the updated {@link Preferences} once the value has
     * been saved
     */
    @Override
    public Single<Preferences> saveLastTimeFetched(long lastTimeUpdated) {
        return datastore.saveStringResource(LAST_TIME_UPDATED_KEY, Long.toString(lastTimeUpdated));
    }
    /**
     * Implementation of {@link BaseRadioLocalDataSource#getRadioRemoteKey()}
     * Reads the remote key from the database.
     * This is a blocking call and must not be made from the main thread.
     *
     * @return The stored {@link RadioRemoteKey}, or {@code null} if nothing has been
     * cached yet
     */
    @Override
    @Nullable
    public RadioRemoteKey getRadioRemoteKey() {
        return radioDAO.getRadioRemoteKey();
    }
    /**
     * Implementation of {@link BaseRadioLocalDataSource#savePage(boolean, List, RadioRemoteKey)}
     * Saves a page of stations and the remote key in the database, delegating to the DAO.
     * This is a blocking call and must not be made from the main thread.
     *
     * @param isRefresh {@code true} if the page comes from a refresh, in which case the
     *                  previously cached data is replaced; {@code false} if the page is
     *                  appended to it
     * @param stations  The stations of the page
     * @param key       The key describing where the next page starts
     */
    @Override
    public void savePage(boolean isRefresh, List<RadioStation> stations, RadioRemoteKey key) {
        radioDAO.savePage(isRefresh, stations, key);
    }
    /**
     * Implementation of {@link BaseRadioLocalDataSource#saveCountryPreference(String)}.
     * The outcome is reported through the {@link com.justtheradio.source.callback.RadioCallback}
     *
     * @param country The country code to save
     */
    @Override
    public void saveCountryPreference(String country) {
        Disposable disposable = datastore.saveStringResource(CHOSEN_COUNTRY_KEY, country)
                .subscribe(preferences -> this.radioCallback.onSavingCountryPreferenceSuccess(),
                        throwable -> this.radioCallback.onFailureSavingCountryPreference(
                                new Exception(throwable))
                );
        disposables.add(disposable);
    }
    /**
     * Implementation of {@link BaseRadioLocalDataSource#getCountryPreferenceAsync()}.
     * Reads the chosen country from the DataStore.
     * The outcome is reported through the {@link com.justtheradio.source.callback.RadioCallback}
     */
    @Override
    public void getCountryPreferenceAsync() {
        Disposable disposable = datastore.getStringResource(CHOSEN_COUNTRY_KEY)
                .subscribe(string -> {
                    if (string != null)
                        this.radioCallback.onGettingCountryPreferenceSuccess(string);
                    else
                        this.radioCallback.onFailureGettingCountryPreference(
                                new Exception(NO_KEY_WITH_THAT_NAME_IN_DATASTORE)
                        );
                }, throwable -> this.radioCallback.onFailureGettingCountryPreference(
                        new Exception(throwable))
                );
        disposables.add(disposable);
    }
    /**
     * Implementation of {@link BaseRadioLocalDataSource#getCountryPreference()}.
     * Reads the chosen country from the DataStore, returning the result as a reactive
     * stream
     *
     * @return A {@link Single} emitting the saved country code
     */
    @Override
    public Single<String> getCountryPreference() {
        return datastore.getStringResource(CHOSEN_COUNTRY_KEY);
    }
    /**
     * Implementation of {@link BaseRadioLocalDataSource#getRadioDAOPagingSource()}.
     * Provides the {@link PagingSource} that reads the cached stations from the database.
     * This is blocking call and should not be called from the main thread
     * @return A new {@link PagingSource} created by the DAO
     */
    @Override
    public PagingSource<Integer, RadioStation> getRadioDAOPagingSource() {
        return radioDAO.pagingSource();
    }
    /**
     * Implementation of {@link BaseRadioLocalDataSource#observeFavouriteUUIDs()}.
     * Observes the identifiers of the favourite stations.
     * The list emitted by the DAO is converted into a {@link Set}.
     * @return A {@link Flowable} emitting the set of favourite station UUIDs every time it changes
     */
    @Override
    public Flowable<Set<UUID>> observeFavouriteUUIDs() {
        return radioDAO.observeFavouritesUUIDs().map(HashSet::new);
    }
    /**
     * Implementation of {@link BaseRadioLocalDataSource#observeFavourites()}.
     * Observes the full list of favourite stations.
     * @return A {@link Flowable} emitting the list of favourites every time it changes
     */
    @Override
    public Flowable<List<FavouriteStation>> observeFavourites() {
        return radioDAO.observeFavourites();
    }
    /**
     * Implementation of {@link BaseRadioLocalDataSource#addFavourite(BaseRadioStation)}.
     * Adds a station to the favourites, on the database write executor. The station is saved
     * together with the time at which it was added. The outcome of the operation is reported
     * through the {@link com.justtheradio.source.callback.RadioCallback RadioCallback}
     *
     * @param station The station to mark as favourite
     */
    @Override
    public void addFavourite(BaseRadioStation station) {
        assert RadioRoomDatabase.getDatabaseWriteExecutor() != null;
        RadioRoomDatabase.getDatabaseWriteExecutor().execute(() -> {
            try {
                FavouriteStation favouriteStation = new FavouriteStation(station, System.currentTimeMillis());
                radioDAO.insertFavourite(favouriteStation);
                radioCallback.onRadioStationModifiedFavouriteStateSuccess(favouriteStation);
            } catch (Exception e) {
                radioCallback.onFailureModifyingStationSavedState(e);
            }
        });
    }
    /**
     * Implementation of {@link BaseRadioLocalDataSource#removeFavourite(BaseRadioStation)}.
     * Removes a station from the favourites, on the database write executor. The outcome is
     * reported through the {@link com.justtheradio.source.callback.RadioCallback RadioCallback}
     *
     * @param station The station to remove from the favourites
     */
    @Override
    public void removeFavourite(BaseRadioStation station) {
        assert RadioRoomDatabase.getDatabaseWriteExecutor() != null;
        RadioRoomDatabase.getDatabaseWriteExecutor().execute(() -> {
            try {
                radioDAO.deleteFavourite(station.getUUID());
                radioCallback.onRadioStationModifiedFavouriteStateSuccess(station);
            } catch (Exception e) {
                radioCallback.onFailureModifyingStationSavedState(e);
            }
        });
    }
    /**
     * Implementation of {@link BaseRadioLocalDataSource#onDestroy()}
     * Cancels the pending DataStore subscriptions and wipes completely the Room database.
     */
    @Override
    public void onDestroy() {
        assert RadioRoomDatabase.getDatabaseWriteExecutor() != null;
        RadioRoomDatabase.getDatabaseWriteExecutor().execute(radioDAO::clearDatabase);
        disposables.clear();
    }
}
