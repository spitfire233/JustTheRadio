package com.justtheradio.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import de.sfuhrm.radiobrowser4j.Station;

@Entity
public class RadioStation {

    @PrimaryKey(autoGenerate = true)
    public long id;

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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public StationType getStationType() {
        return stationType;
    }

    public void setStationType(StationType stationType) {
        this.stationType = stationType;
    }

}
