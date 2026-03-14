package com.justtheradio.model;

import static com.justtheradio.util.constants.Constants.RADIO_STATIONS_DATABASE_STATION_PREFIX;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import de.sfuhrm.radiobrowser4j.Station;

@Entity
public class RadioStation {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @Embedded(prefix = RADIO_STATIONS_DATABASE_STATION_PREFIX)
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

}
