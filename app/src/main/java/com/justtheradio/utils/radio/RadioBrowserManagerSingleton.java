package com.justtheradio.utils.radio;

import static com.justtheradio.utils.constant.Constants.ERROR_ENDPOINT_NOT_FOUND;

import com.justtheradio.exceptions.EndpointNotFoundException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import de.sfuhrm.radiobrowser4j.AdvancedSearch;
import de.sfuhrm.radiobrowser4j.ConnectionParams;
import de.sfuhrm.radiobrowser4j.EndpointDiscovery;
import de.sfuhrm.radiobrowser4j.Paging;
import de.sfuhrm.radiobrowser4j.RadioBrowser;
import de.sfuhrm.radiobrowser4j.Station;

public class RadioBrowserManagerSingleton {

    private static volatile RadioBrowserManagerSingleton INSTANCE;
    private RadioBrowser browser;
    private final String agent;
    private final int timeout;

    private RadioBrowserManagerSingleton(String agent, int timeout) {
        this.agent = agent;
        this.timeout = timeout;
    }

    public static RadioBrowserManagerSingleton getInstance(String agent, int timeout) throws IOException,
            EndpointNotFoundException {
        if (INSTANCE == null) {
            synchronized (RadioBrowserManagerSingleton.class) {
                if (INSTANCE == null)
                    INSTANCE = new RadioBrowserManagerSingleton(agent, timeout);
            }
        }
        return INSTANCE;
    }

    private RadioBrowser buildRadioBrowser(String agent, int timeout) throws IOException,
            EndpointNotFoundException {
        // Discover the endpoint
        Optional<String> endpoint = new EndpointDiscovery(agent).discover();

        // If endpoint discovery wasn't successful, throw exception
        if (endpoint.isEmpty())
            throw new EndpointNotFoundException(ERROR_ENDPOINT_NOT_FOUND);

        return new RadioBrowser(ConnectionParams.builder()
                .apiUrl(endpoint.get())
                .userAgent(agent)
                .timeout(timeout)
                .build()
        );
    }

    public List<Station> getStationsByCountryWithPaging(String countryCode, int offset, int limit) throws IOException, EndpointNotFoundException {
        if (browser == null)
            browser = buildRadioBrowser(agent, timeout);

        AdvancedSearch advancedSearch = AdvancedSearch.builder()
                .countryCode(countryCode)
                .hideBroken(true)
                .build();
        Paging paging = Paging.at(offset, limit);
        return browser.listStationsWithAdvancedSearch(paging, advancedSearch);
    }

    public List<String> getCountryCodes() {
        Map<String, Integer> countryCodesMap = browser.listCountryCodes();
        List<String> countryCodes = new ArrayList<>(countryCodesMap.keySet());
        Collections.sort(countryCodes);
        return countryCodes;
    }

}
