package com.justtheradio.model;

import de.sfuhrm.radiobrowser4j.Station;

public class RadioStation {
    private Station station;
    private boolean isFavourite;

    public RadioStation(Station station) {
        this(station, false);
    }

    public RadioStation(Station station, boolean isFavourite) {
        setStation(station);
        setFavourite(isFavourite);
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
}
