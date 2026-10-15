package com.justtheradio.utils.source;

import static com.justtheradio.utils.constant.Constants.AGENT;
import static com.justtheradio.utils.constant.Constants.RADIO_BROWSER_TIMEOUT_MS;

import android.app.Application;

import com.justtheradio.repository.RadioRepository;
import com.justtheradio.repository.UserRepository;
import com.justtheradio.source.radiobrowser.BaseRadioBrowserRemoteDataSource;
import com.justtheradio.source.user.BaseUserPreferencesLocalDataSource;
import com.justtheradio.source.radiobrowser.RadioBrowserRemoteDataSource;
import com.justtheradio.source.user.UserPreferencesLocalDataSource;

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

    public RadioRepository getRadioRepository() {
        BaseRadioBrowserRemoteDataSource radioBrowserRemoteDataSource =
                new RadioBrowserRemoteDataSource(AGENT, RADIO_BROWSER_TIMEOUT_MS);
        return new RadioRepository(radioBrowserRemoteDataSource);
    }

    public UserRepository getUserRepository(Application application) {
        BaseUserPreferencesLocalDataSource userPreferencesLocalDataSource =
                new UserPreferencesLocalDataSource(application);
        return new UserRepository(userPreferencesLocalDataSource);
    }

}
