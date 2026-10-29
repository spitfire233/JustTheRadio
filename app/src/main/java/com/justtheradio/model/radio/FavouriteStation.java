package com.justtheradio.model.radio;

import androidx.room.Entity;
import androidx.room.Ignore;

/**
 * Room entity for a station saved by the user as a favourite.
 */
@Entity(primaryKeys = "UUID")
public class FavouriteStation extends BaseRadioStation{
    /** Time the station was added to the favourites. */
    private long addedAt;
    /**
     * Empty constructor for the {@link FavouriteStation} entity.
     * Required by Room to create instances when reading from the database.
     */
    public FavouriteStation() { }
    /**
     * Constructor for the {@link FavouriteStation} entity that copies the data from an existing
     * station. Ignored by Room, which uses the empty constructor.
     *
     * @param station The station to save as a favourite
     * @param addedAt The time it was added, as a timestamp in milliseconds
     */
    @Ignore
    public FavouriteStation(BaseRadioStation station, long addedAt) {
        setUUID(station.getUUID());
        setName(station.getName());
        setURL(station.getURL());
        setFaviconURL(station.getFaviconURL());
        setAddedAt(addedAt);
    }
    /**
     * Setter method for the time added timestamp
     * @param addedAt The time the favourite station was added, as a timestamp in milliseconds
     */
    public void setAddedAt(long addedAt) {
        this.addedAt = addedAt;
    }
    /**
     * Getter method for the added time timestamp
     * @return The time the station was added to the favourites, as a timestamp in milliseconds
     */
    public long getAddedAt() {
        return addedAt;
    }
}
