package com.justtheradio.util.source;

import com.justtheradio.repository.RadioStationsRepository;
import com.justtheradio.source.BaseRadioStationsRemoteDataSource;
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

    public RadioStationsRepository getRadioStationsRepository() {
        BaseRadioStationsRemoteDataSource radioBrowserRemoteDataSource
                = new RadioStationsRemoteDataSource();
        return new RadioStationsRepository(radioBrowserRemoteDataSource);
    }



}
