package com.justtheradio.model;

import androidx.annotation.NonNull;
import androidx.room3.Entity;
import androidx.room3.PrimaryKey;

@Entity
public class RadioRemoteKey {
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
