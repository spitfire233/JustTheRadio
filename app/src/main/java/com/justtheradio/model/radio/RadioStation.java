package com.justtheradio.model.radio;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.UUID;

/**
 * Room entity for a station in the cached list loaded from the network.
 */
@Entity
public class RadioStation extends BaseRadioStation{
    /**
     * Auto-generated primary key.
     */
    @PrimaryKey(autoGenerate = true)
    private int ID;
    /**
     * Empty constructor for {@link RadioStation}.
     * Required by Room to create instances when reading from the database.
     */
    public RadioStation() { }
    /**
     * Constructor for {@link RadioStation}.
     *  Ignored by Room, which uses the empty constructor
     * @param UUID The unique identifier of the station
     * @param URL The station's audio stream URL
     * @param faviconURL The station's logo URL
     * @param name The station's name
     */
    @Ignore
    public RadioStation(UUID UUID, String URL, String faviconURL, String name) {
        setUUID(UUID);
        setURL(URL);
        setFaviconURL(faviconURL);
        setName(name);
    }
    /**
     * Setter for the station's ID
     * @param id The station's ID
     */
    public void setID(int id) {
        this.ID = id;
    }
    /**
     * Getter for the station's ID
     * @return The station's ID
     */
    public int getID() {
        return ID;
    }
}
