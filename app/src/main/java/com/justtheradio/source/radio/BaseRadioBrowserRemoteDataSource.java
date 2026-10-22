package com.justtheradio.source.radio;

import com.justtheradio.exceptions.EndpointNotFoundException;
import com.justtheradio.source.radio.callback.RadioCallback;

import java.io.IOException;
import java.util.List;

import de.sfuhrm.radiobrowser4j.Station;

public abstract class BaseRadioBrowserRemoteDataSource {

    protected RadioCallback radioCallback;

    public void setCallback(RadioCallback radioCallback) {
        this.radioCallback = radioCallback;
    }

    public abstract void getCountryCodes();

    public abstract List<Station> getRadioStationsByCountry(String country, int offset, int limit) throws IOException, EndpointNotFoundException;

    public abstract List<Station> getRadioStationsByCountry(String country, String query, int offset, int limit) throws IOException, EndpointNotFoundException;
}
