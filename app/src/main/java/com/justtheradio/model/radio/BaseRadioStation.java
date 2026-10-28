package com.justtheradio.model.radio;

import androidx.annotation.NonNull;

import java.util.UUID;

public abstract class BaseRadioStation {

    @NonNull
    private UUID UUID;
    private String faviconURL;
    private String name;
    private String URL;

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

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }
}
