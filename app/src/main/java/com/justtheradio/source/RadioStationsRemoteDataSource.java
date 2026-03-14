package com.justtheradio.source;

import static com.justtheradio.util.constants.Constants.AGENT;
import static com.justtheradio.util.constants.Constants.ERROR_IN_DISCOVERING_ENDPOINT;
import static com.justtheradio.util.constants.Constants.NUMBER_OF_RETRIES;
import static com.justtheradio.util.constants.Constants.TIMEOUT;

import android.util.Log;

import com.justtheradio.model.RadioStation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.sfuhrm.radiobrowser4j.AdvancedSearch;
import de.sfuhrm.radiobrowser4j.ConnectionParams;
import de.sfuhrm.radiobrowser4j.EndpointDiscovery;
import de.sfuhrm.radiobrowser4j.Paging;
import de.sfuhrm.radiobrowser4j.RadioBrowser;
import de.sfuhrm.radiobrowser4j.Station;

public class RadioStationsRemoteDataSource extends BaseRadioStationsRemoteDataSource {

    private final ExecutorService executorService;

    private RadioBrowser radioBrowser;

    public RadioStationsRemoteDataSource() {
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @Override
    public void getNationalRadioStations(String countryCode, int offset, int limit) {
        executorService.execute(() -> {
            try {
                callback.onSuccessFetchingNationalRadioStations(
                        getRadioStations(countryCode, offset, limit)
                );
            } catch (IOException e) {
                Log.e(TAG, ERROR_IN_DISCOVERING_ENDPOINT);
                callback.onFailureFetchingNationalRadioStations(e);
            }
        });
    }

    @Override
    public void getInternationalRadioStations(List<String> countryCodes, int offset, int limit) {
        executorService.execute(() -> {
            try {
                List<RadioStation> radioStations = new ArrayList<>();
                for (String countryCode : countryCodes)
                    radioStations.addAll(getRadioStations(countryCode, offset, limit));
                callback.onSuccessFetchingInternationalRadioStations(radioStations);
            } catch (IOException e) {
                Log.e(TAG, ERROR_IN_DISCOVERING_ENDPOINT);
                callback.onFailureFetchingInternationalRadioStations(e);
            }
        });
    }

    private List<RadioStation> getRadioStations(String countryCode,
                                                int offset, int limit) throws IOException {
        buildRadioBrowser();
        // Create a paging object to limit results
        Paging paging = Paging.at(offset, limit);
        AdvancedSearch advancedSearch = AdvancedSearch.builder()
                .countryCode(countryCode)
                .build();
        List<Station> results =
                radioBrowser.listStationsWithAdvancedSearch(paging, advancedSearch);
        List<RadioStation> radioStations = new ArrayList<>();
        for(Station station : results)
            radioStations.add(new RadioStation(station));
        return radioStations;
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
