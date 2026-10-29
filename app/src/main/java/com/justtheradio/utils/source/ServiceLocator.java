package com.justtheradio.utils.source;

import static com.justtheradio.utils.constant.Constants.AGENT;
import static com.justtheradio.utils.constant.Constants.RADIO_BROWSER_PAGING_LIMIT;
import static com.justtheradio.utils.constant.Constants.RADIO_BROWSER_TIMEOUT_MS;

import android.app.Application;

import com.justtheradio.database.RadioRoomDatabase;
import com.justtheradio.repository.RadioRepository;
import com.justtheradio.source.BaseRadioBrowserRemoteDataSource;
import com.justtheradio.source.BaseRadioLocalDataSource;
import com.justtheradio.source.RadioLocalDataSource;
import com.justtheradio.paging.RadioRemoteMediator;
import com.justtheradio.source.RadioBrowserRemoteDataSource;
import com.justtheradio.utils.datastore.DataStoreManagerSingleton;

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


    public RadioRepository getRadioRepository(Application application) {
        BaseRadioBrowserRemoteDataSource radioBrowserRemoteDataSource =
                new RadioBrowserRemoteDataSource(
                        AGENT,
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

}
