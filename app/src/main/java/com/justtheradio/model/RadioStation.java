package com.justtheradio.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

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
    private int position;

    public RadioStation() {
        // Required empty constructor
    }

    public RadioStation(Station station, boolean isFavourite, int offset) {
        setName(station.getName());
        setFaviconURL(station.getFavicon().trim());
        setFavourite(isFavourite);
        setUUID(station.getStationUUID());
        String url = station.getUrlResolved() != null && !station.getUrlResolved().isEmpty() ?
                station.getUrlResolved() :
                station.getUrl();
        setURL(url.trim());
        setPosition(offset);
    }

    public RadioStation(Station station, int offset) {
        this(station, false, offset);
    }

    @NonNull
    public UUID getUUID() {
        return UUID;
    }

    public void setUUID(@NonNull UUID UUID) {
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

    public int getPosition() {
        return position;
    }

    public void setPosition(int offset) {
        this.position = offset;
    }
}
