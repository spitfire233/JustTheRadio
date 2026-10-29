package com.justtheradio.exceptions;

/**
 * Custom checked {@link Exception} for when
 * {@link de.sfuhrm.radiobrowser4j.RadioBrowser RadioBrowser} is unable to get a valid endpoint
 */
public class EndpointNotFoundException extends Exception {
    /**
     * Constructor for the {@link EndpointNotFoundException} exception
     * @param message The error message for the exception
     */
    public EndpointNotFoundException(String message) {
        super(message);
    }
}
