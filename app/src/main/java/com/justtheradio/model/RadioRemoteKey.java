package com.justtheradio.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class RadioRemoteKey {

    public RadioRemoteKey() {

    }

    public RadioRemoteKey(int nextOffset, boolean endReached) {
        setNextOffset(nextOffset);
        setEndReached(endReached);
    }

    @PrimaryKey private int id = 1;
    private int nextOffset;
    private boolean endReached;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNextOffset() {
        return nextOffset;
    }

    public void setNextOffset(int nextOffset) {
        this.nextOffset = nextOffset;
    }

    public boolean isEndReached() {
        return endReached;
    }

    public void setEndReached(boolean endReached) {
        this.endReached = endReached;
    }


}
