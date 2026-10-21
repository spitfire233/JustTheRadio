package com.justtheradio.source.radio;

import static com.justtheradio.utils.constant.Constants.LAST_TIME_UPDATED_KEY;

import android.app.Application;

import androidx.paging.PagingSource;

import com.justtheradio.database.RadioDAO;
import com.justtheradio.database.RadioRoomDatabase;
import com.justtheradio.model.RadioStation;
import com.justtheradio.utils.datastore.DataStoreManagerSingleton;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

public class RadioLocalDataSource extends BaseRadioLocalDataSource  {

    private final RadioDAO radioDAO;
    private RadioRoomDatabase radioRoomDatabase;
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
    public void saveLastTimeFetched(long lastTimeUpdated) {
        Disposable disposable = dataStoreManager
                .saveStringResource(LAST_TIME_UPDATED_KEY,Long.toString(lastTimeUpdated))
                .subscribe(pref -> { },
                        throwable -> { } );
        disposables.add(disposable);
    }

    @Override
    public void saveRadioStations(List<RadioStation> radioStationList) {
        RadioRoomDatabase.getDatabaseWriteExecutor().execute(() -> {
            try {
                List<RadioStation> favouriteStations = radioDAO.getFavouriteStations();
                Set<UUID> favouriteUUIDs = favouriteStations.stream()
                        .map(RadioStation::getUUID)
                        .collect(Collectors.toSet());
                for (RadioStation radioStation : radioStationList) { // Avg O(n + m)
                    if (favouriteUUIDs.contains(radioStation.getUUID()))
                        radioStation.setFavourite(true);
                }
                radioDAO.insertAll(radioStationList);
                this.radioCallback.onSavingRadioStationSuccess(radioStationList);
            } catch (Exception e) {
                // TODO: This might need some disambiguation!
                this.radioCallback.onFailureFromLocal(e);
            }
        });
    }

    @Override
    public PagingSource<Integer, RadioStation> getRadioDAOPagingSource() {
        return radioDAO.pagingSource();
    }



    @Override
    public void onDestroy() {
        this.disposables.clear();
    }



}
