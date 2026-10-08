package com.justtheradio.source.radiobrowser;


import static com.justtheradio.utils.constant.Constants.ERROR_ENDPOINT_NOT_FOUND;

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

import de.sfuhrm.radiobrowser4j.ConnectionParams;
import de.sfuhrm.radiobrowser4j.EndpointDiscovery;
import de.sfuhrm.radiobrowser4j.RadioBrowser;

public class RadioBrowserRemoteDataSource extends BaseRadioBrowserRemoteDataSource{

    private RadioBrowser radioBrowser = null;
    private final ExecutorService executorService;
    private final String agent;
    private final int timeout;

    private final Pattern ISO_PATTERN;

    public RadioBrowserRemoteDataSource(String agent, int timeout) {
        this.executorService = ServiceLocator.getInstance().getExecutorService();
        this.agent = agent;
        this.timeout = timeout;
        ISO_PATTERN = Pattern.compile("^[a-z]{2}$");
    }

    @Override
    public void getCountryCodes() {
        executorService.execute(() -> {
            try {
                if (radioBrowser == null)
                    radioBrowser = buildRadioBrowser(agent, timeout);
                Map<String, Integer> countriesMap = radioBrowser.listCountryCodes();
                List<String> countryCodes = new ArrayList<>(countriesMap.keySet());
                Collections.sort(countryCodes);
                // Security check: remove malformed country codes
                countryCodes.removeIf(code -> !ISO_PATTERN.matcher(code).matches());
                this.radioBrowserCallback.onCountryCodesGetSuccess(countryCodes);
            } catch (IOException | EndpointNotFoundException e) {
                this.radioBrowserCallback.onFailureFromRemote(e);
            }
        });
    }

    private RadioBrowser buildRadioBrowser(String agent, int timeout)
            throws IOException, EndpointNotFoundException {
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


}
