package com.justtheradio.utils.source;

import static com.justtheradio.utils.constant.Constants.AGENT;
import static com.justtheradio.utils.constant.Constants.RADIO_BROWSER_PAGING_LIMIT;
import static com.justtheradio.utils.constant.Constants.RADIO_BROWSER_TIMEOUT_MS;

import android.app.Application;

import com.justtheradio.database.RadioRoomDatabase;
import com.justtheradio.exceptions.EndpointNotFoundException;
import com.justtheradio.repository.RadioRepository;
import com.justtheradio.repository.UserRepository;
import com.justtheradio.source.radio.BaseRadioBrowserRemoteDataSource;
import com.justtheradio.source.radio.BaseRadioLocalDataSource;
import com.justtheradio.source.radio.RadioLocalDataSource;
import com.justtheradio.mediator.RadioRemoteMediator;
import com.justtheradio.source.user.BaseUserPreferencesLocalDataSource;
import com.justtheradio.source.radio.RadioBrowserRemoteDataSource;
import com.justtheradio.source.user.UserPreferencesLocalDataSource;
import com.justtheradio.utils.datastore.DataStoreManagerSingleton;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServiceLocator {

    private ServiceLocator() { }

    private static volatile ServiceLocator INSTANCE = null;

    private ExecutorService executorService = null;

    public static ServiceLocator getInstance() {
        if(INSTANCE == null) {
            synchronized (ServiceLocator.class) {
                if(INSTANCE == null)
                    INSTANCE = new ServiceLocator();
            }
        }
        return INSTANCE;
    }

    public ExecutorService getExecutorService() {
        if (this.executorService == null)
            this.executorService =
                    Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        return executorService;
    }

    public RadioRoomDatabase getRadioDatabase(Application application) {
        return RadioRoomDatabase.getInstance(application, getExecutorService());
    }


    public RadioRepository getRadioRepository(Application application) throws EndpointNotFoundException, IOException {
        BaseRadioBrowserRemoteDataSource radioBrowserRemoteDataSource =
                new RadioBrowserRemoteDataSource(
                        AGENT,
                        RADIO_BROWSER_TIMEOUT_MS,
                        RADIO_BROWSER_PAGING_LIMIT
                );
        BaseRadioLocalDataSource radioLocalDataSource =
                new RadioLocalDataSource(application, getRadioDatabase(application));
        RadioRemoteMediator radioRemoteMediator = new RadioRemoteMediator(
                DataStoreManagerSingleton.getInstance(application),
                radioBrowserRemoteDataSource,
                radioLocalDataSource);
        return new RadioRepository(radioBrowserRemoteDataSource, radioLocalDataSource, radioRemoteMediator);
    }

    public UserRepository getUserRepository(Application application) {
        BaseUserPreferencesLocalDataSource userPreferencesLocalDataSource =
                new UserPreferencesLocalDataSource(application);
        return new UserRepository(userPreferencesLocalDataSource);
    }

}
