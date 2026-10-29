package com.justtheradio.model.radio;

import androidx.annotation.NonNull;

import java.util.UUID;

/**
 * Base class that holds the fields shared by every radio station model
 * ({@link RadioStation} and {@link FavouriteStation}).
 */
public abstract class BaseRadioStation {
    /** Unique identifier of the station */
    @NonNull
    private UUID uuid;
    /** URL of the station's logo. */
    private String faviconURL;
    /** Name of the station. */
    private String name;
    /** URL of the audio stream. */
    private String URL;

    /** Setter method for the station's {@link uuid}
     * @param uuid the unique identifier of the station, never null
     */
    public void setUUID(@NonNull UUID uuid) {
        this.uuid = uuid;
    }
    /** Getter method for the station's unique identifier
     * @return The unique {@link UUID} of the station
     */
    @NonNull
    public UUID getUUID() {
        return uuid;
    }
    /** Setter method for the station's logo URL
     * @param faviconURL The URL of the station's favicon
     */
    public void setFaviconURL(String faviconURL) {
        this.faviconURL = faviconURL;
    }
    /**
     * Getter method for the station's logo URL
     * @return The favicon URL
     */
    public String getFaviconURL() {
        return faviconURL;
    }
    /** Setter method for the station's name
     * @param name The name of the station
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * Getter method for the station's name
     * @return The name of the station
     */
    public String getName() {
        return name;
    }

    /**
     * Setter method for the station's audio stream URL
     * @param URL The station's audio stream URL
     */
    public void setURL(String URL) {
        this.URL = URL;
    }
    /**
     *
     * @return The station's audio stream URL
     */
    public String getURL() {
        return URL;
    }
}
