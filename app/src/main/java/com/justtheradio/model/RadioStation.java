package com.justtheradio.model;

import de.sfuhrm.radiobrowser4j.Station;

public class RadioStation {
    private Station station;
    private boolean isFavourite;
    private StationType stationType;

    public RadioStation(Station station) {
        this(station, false);
    }

    public RadioStation(Station station, boolean isFavourite) {
        this(station, isFavourite, null);
    }

    public RadioStation(Station station, boolean isFavourite, StationType stationType) {
        setStation(station);
        setFavourite(isFavourite);
        setStationType(stationType);
    }

    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }

    public boolean isFavourite() {
        return isFavourite;
    }

    public void setFavourite(boolean favourite) {
        isFavourite = favourite;
    }

    public StationType getStationType() {
        return stationType;
    }

    public void setStationType(StationType stationType) {
        this.stationType = stationType;
    }

}
