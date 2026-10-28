package com.justtheradio.model.radio;

import androidx.room.Entity;
import androidx.room.Ignore;

@Entity(primaryKeys = "UUID")
public class FavouriteStation extends BaseRadioStation{

    private long addedAt;

    public FavouriteStation() {
        // Required empty public constructor
    }

    @Ignore
    public FavouriteStation(BaseRadioStation station, long addedAt) {
        setUUID(station.getUUID());
        setName(station.getName());
        setURL(station.getURL());
        setFaviconURL(station.getFaviconURL());
        setAddedAt(addedAt);
    }

    public long getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(long addedAt) {
        this.addedAt = addedAt;
    }
}
