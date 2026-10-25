package com.justtheradio.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
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

    public RadioStation() {
        // Required empty constructor
    }

    @Ignore
    public RadioStation(UUID UUID, String URL, String faviconURL, String name) {
        this(UUID, URL, faviconURL, name, false);
    }

    @Ignore
    public RadioStation(UUID UUID, String URL, String faviconURL, String name, boolean isFavourite) {
        setUUID(UUID);
        setURL(URL);
        setFaviconURL(faviconURL);
        setName(name);
        setFavourite(isFavourite);
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
}
