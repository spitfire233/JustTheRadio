package com.justtheradio.util.source;

import static com.justtheradio.util.constants.Constants.AGENT;
import static com.justtheradio.util.constants.Constants.NO_ENDPOINT_FOUND_ERROR_MESSAGE;
import static com.justtheradio.util.constants.Constants.TIMEOUT;

import com.justtheradio.util.exceptions.NoEndpointFoundException;

import java.io.IOException;
import java.util.Optional;

import de.sfuhrm.radiobrowser4j.ConnectionParams;
import de.sfuhrm.radiobrowser4j.EndpointDiscovery;
import de.sfuhrm.radiobrowser4j.RadioBrowser;

public class ServiceLocator {

    private static volatile ServiceLocator INSTANCE = null;

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

    public RadioBrowser getRadioBrowser() throws IOException {
        RadioBrowser radioBrowser;
        // Discover the Endpoint
        Optional<String> endpoint = new EndpointDiscovery(AGENT).discover();
        // Check if an endpoint was correctly retrieved and build a radio browser
        if (endpoint.isPresent()) {
            radioBrowser = new RadioBrowser(
                    ConnectionParams.builder()
                            .apiUrl(endpoint.get())
                            .userAgent(AGENT)
                            .timeout(TIMEOUT).build());
        } else {
            throw new NoEndpointFoundException(NO_ENDPOINT_FOUND_ERROR_MESSAGE);
        }
        return radioBrowser;
    }

}
