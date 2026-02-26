package com.justtheradio.source;

import static com.justtheradio.util.constants.Constants.AGENT;
import static com.justtheradio.util.constants.Constants.ERROR_IN_BUILDING_RADIO_BROWSER;
import static com.justtheradio.util.constants.Constants.ERROR_IN_DISCOVERING_ENDPOINT;
import static com.justtheradio.util.constants.Constants.NUMBER_OF_RETRIES;
import static com.justtheradio.util.constants.Constants.RADIO_REQUESTS_THREAD_POOL_SIZE;
import static com.justtheradio.util.constants.Constants.TIMEOUT;

import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.sfuhrm.radiobrowser4j.AdvancedSearch;
import de.sfuhrm.radiobrowser4j.ConnectionParams;
import de.sfuhrm.radiobrowser4j.EndpointDiscovery;
import de.sfuhrm.radiobrowser4j.Paging;
import de.sfuhrm.radiobrowser4j.Parameter;
import de.sfuhrm.radiobrowser4j.RadioBrowser;
import de.sfuhrm.radiobrowser4j.Station;

public class RadioBrowserRemoteDataSource extends BaseRadioBrowserRemoteDataSource {

    private final ExecutorService executorService;

    private RadioBrowser radioBrowser;

    public RadioBrowserRemoteDataSource() {
        executorService = Executors.newFixedThreadPool(RADIO_REQUESTS_THREAD_POOL_SIZE);
    }

    public void getNationalRadios(String countryCode, int offset, int limit) {
        executorService.execute(() -> {
            try {
                buildRadioBrowser();
                // Create a paging object to limit results
                Paging paging = Paging.at(offset, limit);
                AdvancedSearch advancedSearch = AdvancedSearch.builder()
                        .countryCode(countryCode)
                        .build();
                List<Station> stations =
                        radioBrowser.listStationsWithAdvancedSearch(paging, advancedSearch);
                callback.onSuccessRetrievingNationalRadioStations(stations);
            } catch (IOException e) {
                Log.e(TAG, ERROR_IN_DISCOVERING_ENDPOINT);
                callback.onFailureOnBuildingRadioBrowser(e);
            }
        });
    }

    // TODO: See if there's a better way to do this; for now, it will do
    private synchronized void buildRadioBrowser() throws IOException {
        if (radioBrowser != null) return; // If already initialized, return
        // Get server endpoint
        Optional<String> endpoint = new EndpointDiscovery(AGENT).discover();
        // Build connection parameters
        if(endpoint.isPresent()) {
            ConnectionParams params = ConnectionParams.builder()
                    .apiUrl(endpoint.get())
                    .userAgent(AGENT)
                    .timeout(TIMEOUT)
                    .retries(NUMBER_OF_RETRIES)
                    .build();
            // Build new RadioBrowser endpoint
            radioBrowser = new RadioBrowser(params);
        } else {
            throw new IOException("Something went wrong!"); // TODO: Replace maybe with custom exception
        }
    }


}
