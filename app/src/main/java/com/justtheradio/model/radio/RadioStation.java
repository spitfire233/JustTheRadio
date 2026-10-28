package com.justtheradio.model.radio;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity
public class RadioStation extends BaseRadioStation{

    @PrimaryKey(autoGenerate = true)
    int ID;

    public RadioStation() {
        // Required empty constructor
    }

    @Ignore
    public RadioStation(UUID UUID, String URL, String faviconURL, String name) {
        setUUID(UUID);
        setURL(URL);
        setFaviconURL(faviconURL);
        setName(name);
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }
}
