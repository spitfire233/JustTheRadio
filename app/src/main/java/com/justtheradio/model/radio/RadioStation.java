package com.justtheradio.model.radio;

import androidx.annotation.NonNull;
import androidx.room3.Entity;
import androidx.room3.PrimaryKey;

import java.util.UUID;

import de.sfuhrm.radiobrowser4j.Station;

@Entity
public class RadioStation {

    @PrimaryKey
    @NonNull
    private UUID UUID;
    private String faviconURL;
    private String name;
    private String URL;
    private boolean isFavourite;

    public RadioStation() {
        // Required empty constructor
    }

    public RadioStation(Station station, boolean isFavourite) {
        setName(station.getName());
        setFaviconURL(station.getFavicon().trim());
        setFavourite(isFavourite);
        setUUID(station.getStationUUID());
        String url = station.getUrlResolved() != null && !station.getUrlResolved().isEmpty() ?
                station.getUrlResolved() :
                station.getUrl();
        setURL(url.trim());
    }

    public RadioStation(Station station) {
        this(station, false);
    }

    @NonNull
    public UUID getUUID() {
        return UUID;
    }

    public void setUUID(UUID UUID) {
        this.UUID = UUID;
    }

    public String getFaviconURL() {
        return faviconURL;
    }

    public void setFaviconURL(String faviconURL) {
        this.faviconURL = faviconURL;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isFavourite() {
        return isFavourite;
    }

    public void setFavourite(boolean favourite) {
        isFavourite = favourite;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }
}
