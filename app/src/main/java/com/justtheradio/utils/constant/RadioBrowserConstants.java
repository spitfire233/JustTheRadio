package com.justtheradio.utils.constant;

/**
 * Utility class that holds the constants used by radio browser
 */
public class RadioBrowserConstants {
    /** Timeout of the requests to the Radio Browser API, 5 milliseconds */
    public static final int RADIO_BROWSER_TIMEOUT_MS = 5000;
    /** Version of the application */
    public static final String VERSION = "1.0.1";
    /** User agent that identifies the app to the Radio Browser API, with the app version. */
    public static final String RADIO_BROWSER_AGENT = "JustTheRadio/" + VERSION;
    /** Message of the error raised when no Radio Browser endpoint is found for the agent. */
    public static final String ERROR_ENDPOINT_NOT_FOUND = "No endpoint found for agent" +
                                                          RADIO_BROWSER_AGENT;
    /** Number of stations requested for each page. */
    public static final int RADIO_BROWSER_PAGING_LIMIT = 64;
    /** Number of stations requested for the first page, twice the size of a normal page. */
    public static final int RADIO_BROWSER_INITIAL_LOADING_SIZE = RADIO_BROWSER_PAGING_LIMIT * 2;
    /**
     * Distance from the end of the loaded stations at which the next page starts loading,
     * half the size of a page.
     */
    public static final int RADIO_BROWSER_PREFETCH_DISTANCE = RADIO_BROWSER_PAGING_LIMIT / 2;
    /**
     * Time after which the cached stations are considered expired and are downloaded
     * again, in milliseconds (10 minutes).
     */
    public static final long RADIO_BROWSER_EXPIRED_THRESHOLD = 600000;
}
