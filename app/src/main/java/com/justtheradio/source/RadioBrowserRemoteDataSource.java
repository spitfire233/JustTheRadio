package com.justtheradio.source;


import static com.justtheradio.utils.constant.RadioBrowserConstants.ERROR_ENDPOINT_NOT_FOUND;
import static com.justtheradio.utils.constant.Constants.ISO_COUNTRY_REGEX;

import com.justtheradio.exceptions.EndpointNotFoundException;
import com.justtheradio.utils.source.ServiceLocator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

import de.sfuhrm.radiobrowser4j.AdvancedSearch;
import de.sfuhrm.radiobrowser4j.ConnectionParams;
import de.sfuhrm.radiobrowser4j.EndpointDiscovery;
import de.sfuhrm.radiobrowser4j.Paging;
import de.sfuhrm.radiobrowser4j.RadioBrowser;
import de.sfuhrm.radiobrowser4j.Station;

/**
 * Remote data source that that implements {@link BaseRadioBrowserRemoteDataSource}.
 * This class implements both asynchronous and synchronous methods to interact with the Radio Browser
 * API
 */
public class RadioBrowserRemoteDataSource extends BaseRadioBrowserRemoteDataSource{
    /** Client of the Radio Browser API; {@code null} until the first request is made. */
    private volatile RadioBrowser radioBrowser = null;
    /** Executor used to run the asynchronous requests off the main thread. */
    private final ExecutorService executorService;
    /** User agent sent to the API, also used to discover the endpoint. */
    private final String agent;
    /** Timeout of the requests to the API. */
    private final int timeout;
    /** Pattern of a valid country code: exactly two uppercase letters (ISO 3166-1 alpha-2). */
    private final Pattern ISO_PATTERN;
    /**
     * Constructor for the {@link RadioBrowserRemoteDataSource} class
     * @param agent The agent string to send to the remote API
     * @param timeout A timeout for the requests sent to the API
     */
    public RadioBrowserRemoteDataSource(String agent, int timeout) {
        this.executorService = ServiceLocator.getInstance().getExecutorService();
        this.agent = agent;
        this.timeout = timeout;
        ISO_PATTERN = Pattern.compile(ISO_COUNTRY_REGEX);
    }
    /**
     * Implementation of {@link BaseRadioBrowserRemoteDataSource#getCountryCodes()}.
     * Fetches the list of the country codes known by the API, on a background thread.
     * The codes are sorted alphabetically and the malformed ones are removed. The outcome is
     * reported through the {@link com.justtheradio.source.callback.RadioCallback RadioCallback}
     */
    @Override
    public void getCountryCodes() {
        executorService.execute(() -> {
            try {
                if (radioBrowser == null) // Lazy initialization of the API entrypoint
                    radioBrowser = buildRadioBrowser(agent, timeout);
                // Fetch the country codes
                Map<String, Integer> countriesMap = radioBrowser.listCountryCodes();
                // Construct the list and sort it alphabetically
                List<String> countryCodes = new ArrayList<>(countriesMap.keySet());
                Collections.sort(countryCodes);
                // Security check: remove malformed country codes
                countryCodes.removeIf(code -> !ISO_PATTERN.matcher(code).matches());
                // Publish success
                radioCallback.onCountryCodesFetchSuccess(countryCodes);
            } catch (IOException | EndpointNotFoundException e) {
                // Publish failure
                radioCallback.onFailureFetchingCountryCodes(e);
            }
        });
    }
    /**
     * Implementation
     * of {@link BaseRadioBrowserRemoteDataSource#getRadioStationsByCountry(String, int, int)}
     * Fetches a page of the stations of a country.
     * This is a blocking call and should not be made from the main thread
     *
     * @param country The country code of the stations to fetch
     * @param offset  Index of the first station to fetch
     * @param limit   Maximum number of stations to fetch
     * @return The stations of the requested page
     * @throws IOException                  If a network error occurs
     * @throws EndpointNotFoundException    If no Radio Browser server could be reached
     */
    @Override
    public List<Station> getRadioStationsByCountry(String country, int offset, int limit)
            throws IOException, EndpointNotFoundException {
        return getRadioStationsByCountry(country, null, offset, limit);
    }
    /**
     * Implementation of
     * {@link BaseRadioBrowserRemoteDataSource#getRadioStationsByCountry(String, String, int, int)}
     * Fetches a page of the stations of a country whose name matches a certain query.
     * This is a blocking call and should not be made from the main thread
     *
     * @param country The country code of the stations to search in
     * @param query   The search query
     * @param offset  Index of the first station to fetch
     * @param limit   Maximum number of stations to fetch
     * @return The station of the requested page
     * @throws IOException                  If a network error occurs
     * @throws EndpointNotFoundException    If no Radio Browser server could be reached
     */
    @Override
    public List<Station> getRadioStationsByCountry(String country, String query, int offset,
                                                   int limit) throws IOException,
                                                                     EndpointNotFoundException {
        if (radioBrowser == null) // Lazy initialization of the radio browser entry point
            radioBrowser = buildRadioBrowser(agent, timeout);
        // Build the search parameters to send to the API
        AdvancedSearch searchParams = AdvancedSearch.builder()
                .countryCode(country)
                .hideBroken(true)
                .name(query)
                .build();
        return radioBrowser.listStationsWithAdvancedSearch(Paging.at(offset, limit), searchParams);
    }
    /**
     * Discovers an available Radio Browser endpoint and builds a client connected to it.
     * The method is synchronized so that concurrent requests do not run the discovery at
     * the same time.
     *
     * @param agent   User agent to identify the app to the API
     * @param timeout Timeout of the requests made by the client
     * @return A {@link RadioBrowser} client ready to query the API
     * @throws IOException               If a network error occurs during the discovery
     * @throws EndpointNotFoundException If the discovery does not find any endpoint
     */
    private synchronized RadioBrowser buildRadioBrowser(String agent, int timeout)
            throws IOException, EndpointNotFoundException {
        // Discover the endpoint
        Optional<String> endpoint = new EndpointDiscovery(agent).discover();

        // If endpoint discovery wasn't successful, throw exception
        if (endpoint.isEmpty())
            throw new EndpointNotFoundException(ERROR_ENDPOINT_NOT_FOUND);

        // Return client
        return new RadioBrowser(ConnectionParams.builder()
                .apiUrl(endpoint.get())
                .userAgent(agent)
                .timeout(timeout)
                .build()
        );
    }
}
