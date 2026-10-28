package com.justtheradio.source;

import static com.justtheradio.utils.constant.Constants.CHOSEN_COUNTRY_KEY;
import static com.justtheradio.utils.constant.Constants.LAST_TIME_UPDATED_KEY;
import static com.justtheradio.utils.constant.Constants.NO_KEY_WITH_THAT_NAME_IN_DATASTORE;

import android.app.Application;

import androidx.datastore.preferences.core.Preferences;
import androidx.paging.PagingSource;

import com.justtheradio.database.RadioDAO;
import com.justtheradio.database.RadioRoomDatabase;
import com.justtheradio.model.radio.BaseRadioStation;
import com.justtheradio.model.radio.FavouriteStation;
import com.justtheradio.model.radio.RadioRemoteKey;
import com.justtheradio.model.radio.RadioStation;
import com.justtheradio.utils.datastore.DataStoreManagerSingleton;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

public class RadioLocalDataSource extends BaseRadioLocalDataSource  {

    private final RadioDAO radioDAO;
    private final DataStoreManagerSingleton dataStoreManager;
    private final CompositeDisposable disposables;
    private int offset;

    public RadioLocalDataSource(Application application, RadioRoomDatabase radioRoomDatabase) {
        this.radioDAO = radioRoomDatabase.radioDAO();
        dataStoreManager = DataStoreManagerSingleton.getInstance(application);
        disposables = new CompositeDisposable();
        offset = 0;
    }

    @Override
    public Single<Preferences> saveLastTimeFetched(long lastTimeUpdated) {
        return dataStoreManager.saveStringResource(LAST_TIME_UPDATED_KEY, Long.toString(lastTimeUpdated));
    }

    @Override
    public Single<String> getLastTimeFetched() {
        return dataStoreManager.getStringResource(LAST_TIME_UPDATED_KEY);
    }

    @Override
    public RadioRemoteKey getRadioRemoteKey() {
        return radioDAO.getRadioRemoteKey();
    }

    @Override
    public void savePage(boolean isRefresh, List<RadioStation> stations, RadioRemoteKey key) {
        radioDAO.savePage(isRefresh, stations, key);
    }


    @Override
    public void saveCountryPreference(String country) {
        Disposable disposable = dataStoreManager.saveStringResource(CHOSEN_COUNTRY_KEY, country)
                .subscribe(preferences -> this.radioCallback.onSavingCountryPreferenceSuccess(),
                        throwable -> this.radioCallback.onFailureSavingCountryPreference(new Exception(throwable)));
        disposables.add(disposable);
    }

    @Override
    public void getCountryPreferenceAsync() {
        Disposable disposable = dataStoreManager.getStringResource(CHOSEN_COUNTRY_KEY)
                .subscribe(string -> {
                    if (string != null)
                        this.radioCallback.onGettingCountryPreferenceSuccess(string);
                    else
                        this.radioCallback.onFailureGettingCountryPreference(new Exception(NO_KEY_WITH_THAT_NAME_IN_DATASTORE));
                }, throwable -> this.radioCallback.onFailureGettingCountryPreference(new Exception(throwable)));
        disposables.add(disposable);
    }

    @Override
    public Single<String> getCountryPreference() {
        return dataStoreManager.getStringResource(CHOSEN_COUNTRY_KEY);
    }

    @Override
    public PagingSource<Integer, RadioStation> getRadioDAOPagingSource() {
        return radioDAO.pagingSource();
    }

    @Override
    public Flowable<Set<UUID>> observeFavouriteUUIDs() {
        return radioDAO.observeFavouritesUUIDs().map(HashSet::new);
    }

    @Override
    public Flowable<List<FavouriteStation>> observeFavourites() {
        return radioDAO.observeFavourites();
    }

    @Override
    public void addFavourite(BaseRadioStation station) {
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

    @Override
    public void removeFavourite(BaseRadioStation station) {
        RadioRoomDatabase.getDatabaseWriteExecutor().execute(() -> {
            try {
                radioDAO.deleteFavourite(station.getUUID());
                radioCallback.onRadioStationModifiedFavouriteStateSuccess(station);
            } catch (Exception e) {
                radioCallback.onFailureModifyingStationSavedState(e);
            }
        });
    }

    @Override
    public void onDestroy() {
        this.disposables.clear();
    }



}
