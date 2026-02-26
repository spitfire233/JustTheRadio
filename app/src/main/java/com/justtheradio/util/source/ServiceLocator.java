package com.justtheradio.util.source;

import static com.justtheradio.util.constants.Constants.AGENT;
import static com.justtheradio.util.constants.Constants.NUMBER_OF_RETRIES;
import static com.justtheradio.util.constants.Constants.TIMEOUT;

import com.justtheradio.repository.RadioStationsRepository;
import com.justtheradio.source.BaseRadioBrowserRemoteDataSource;
import com.justtheradio.source.RadioBrowserRemoteDataSource;

import java.io.IOException;
import java.util.Optional;

import de.sfuhrm.radiobrowser4j.ConnectionParams;
import de.sfuhrm.radiobrowser4j.EndpointDiscovery;
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
        BaseRadioBrowserRemoteDataSource radioBrowserRemoteDataSource
                = new RadioBrowserRemoteDataSource();
        return new RadioStationsRepository(radioBrowserRemoteDataSource);
    }



}
