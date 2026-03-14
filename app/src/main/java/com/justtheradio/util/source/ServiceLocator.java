package com.justtheradio.util.source;

import android.app.Application;

import com.google.gson.Gson;
import com.justtheradio.database.RadioStationRoomDatabase;
import com.justtheradio.repository.RadioStationsRepository;
import com.justtheradio.source.BaseRadioStationsLocalDataSource;
import com.justtheradio.source.BaseRadioStationsRemoteDataSource;
import com.justtheradio.source.RadioStationsLocalDataSource;
import com.justtheradio.source.RadioStationsRemoteDataSource;

import de.sfuhrm.radiobrowser4j.RadioBrowser;

public class ServiceLocator {

    private static volatile ServiceLocator INSTANCE = null;

    RadioBrowser radioBrowser;

    private ServiceLocator() { }

    public static ServiceLocator getInstance() {
        if (INSTANCE == null) {
            synchronized (ServiceLocator.class) {
                if (INSTANCE == null)
                    INSTANCE = new ServiceLocator();
            }
        }
        return INSTANCE;
    }

    public Gson getGson() {
        return new Gson();
    }

    public RadioStationRoomDatabase getRadioStationsDatabase(Application application) {
        return RadioStationRoomDatabase.getDatabase(application);
    }

    public RadioStationsRepository getRadioStationsRepository(Application application) {
        BaseRadioStationsRemoteDataSource radioBrowserRemoteDataSource
                = new RadioStationsRemoteDataSource();
        BaseRadioStationsLocalDataSource radioStationsLocalDataSource
                = new RadioStationsLocalDataSource(getRadioStationsDatabase(application));
        return new RadioStationsRepository(radioBrowserRemoteDataSource, radioStationsLocalDataSource);
    }



}
