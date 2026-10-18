package com.justtheradio.model;

import androidx.annotation.NonNull;
import androidx.room3.Entity;
import androidx.room3.PrimaryKey;

@Entity
public class RadioRemoteKey {
    @PrimaryKey public int id = 1;
    public int nextOffset;
    public boolean endReached;
}
