package com.justtheradio.utils.source;

import static com.justtheradio.utils.constant.Constants.AGENT;
import static com.justtheradio.utils.constant.Constants.RADIO_BROWSER_TIMEOUT_MS;

import com.justtheradio.repository.radio.RadioRepository;
import com.justtheradio.source.radiobrowser.BaseRadioBrowserRemoteDataSource;
import com.justtheradio.source.radiobrowser.BaseRadioLocalDataSource;
import com.justtheradio.source.radiobrowser.RadioBrowserRemoteDataSource;
import com.justtheradio.source.radiobrowser.RadioLocalDataSource;

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
        BaseRadioLocalDataSource radioLocalDataSource = new RadioLocalDataSource();
        return new RadioRepository(radioBrowserRemoteDataSource, radioLocalDataSource);
    }



}
