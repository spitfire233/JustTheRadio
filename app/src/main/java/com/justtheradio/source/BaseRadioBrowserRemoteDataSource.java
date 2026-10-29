package com.justtheradio.source;

import com.justtheradio.exceptions.EndpointNotFoundException;
import com.justtheradio.source.callback.RadioCallback;

import java.io.IOException;
import java.util.List;

import de.sfuhrm.radiobrowser4j.Station;
/**
 * Base class for the remote data sources that provide radio stations from the
 * Radio Browser API.
 */
public abstract class BaseRadioBrowserRemoteDataSource {
    /** Callback notified of the outcome of the asynchronous operations. */
    protected RadioCallback radioCallback;
    /**
     * Registers the callback that will be notified of the outcome of the asynchronous
     * operations.
     *
     * @param radioCallback The callback to notify
     */
    public void setCallback(RadioCallback radioCallback) {
        this.radioCallback = radioCallback;
    }
    /**
     * Fetches the list of the available country codes.
     * This should be implemented as an asynchronous operation
     */
    public abstract void getCountryCodes();
    /**
     * Fetches a page of the stations of a country.
     * This should be implemented as blocking call and thus should not be made
     * from the main thread.
     *
     * @param country The country code of the stations to fetch
     * @param offset  Index of the first station to fetch
     * @param limit   Maximum number of stations to fetch
     * @return The stations of the requested page
     * @throws IOException               If a network error occurs
     * @throws EndpointNotFoundException If no Radio Browser server could be reached
     */
    public abstract List<Station> getRadioStationsByCountry(String country, int offset, int limit)
                                                            throws IOException,
                                                                   EndpointNotFoundException;
    /**
     * Fetches a page of the stations of a country whose name matches a search query.
     * This should be implemented as a blocking call and thus should not be made from
     * the main thread.
     *
     * @param country The country code of the stations to search in
     * @param query   The search query
     * @param offset  Index of the first station to fetch
     * @param limit   Maximum number of stations to fetch
     * @return The matching stations of the requested page
     * @throws IOException               If a network error occurs
     * @throws EndpointNotFoundException If no Radio Browser server could be reached
     */
    public abstract List<Station> getRadioStationsByCountry(String country,
                                                            String query, int offset, int limit)
                                                            throws IOException,
                                                                   EndpointNotFoundException;
}
