package com.justtheradio.model.radio;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Room entity that stores the paging state for the
 * {@link com.justtheradio.paging.RadioRemoteMediator RemoteMediator}.
 * The table contains a single row (id = 1), which is replaced after every loaded page.
 */
@Entity
public class RadioRemoteKey {
    /**
     * Primary key. It is always 1, so the table holds one row only.
     */
    @PrimaryKey private int id = 1;
    /**
     * Offset to use in the next API request.
     */
    private int nextOffset;
    /**
     * {@code true} when the last page has been loaded and no further requests are needed.
     */
    private boolean endReached;
    /**
     * Empty constructor for {@link RadioRemoteKey}.
     * Required by Room to create instances when reading from the database.
     */
    public RadioRemoteKey() { }
    /**
     * Constructor for the {@link RadioRemoteKey} class.
     * Ignored by Room, which uses the empty constructor
     * @param nextOffset The offset to use for the next request to the API
     * @param endReached true if there are no more pages to load
     */
    @Ignore
    public RadioRemoteKey(int nextOffset, boolean endReached) {
        setNextOffset(nextOffset);
        setEndReached(endReached);
    }
    /**
     * Setter for the unique id of the remote key
     * @param id The primary key
     */
    public void setId(int id) {
        this.id = id;
    }
    /**
     * Getter for the unique id of the remote key
     * @return The key id
     */
    public int getId() {
        return id;
    }
    /**
     * Setter for the next offset to use for the next request to the API
     * @param nextOffset The next offset to use for the next request to the API
     */
    public void setNextOffset(int nextOffset) {
        this.nextOffset = nextOffset;
    }
    /**
     * Getter for the next offset to use for the next request to the API
     * @return The offset to use to load the next page
     */
    public int getNextOffset() {
        return nextOffset;
    }
    /**
     * Setter for the flag that indicates if the last page has been loaded
     * @param endReached {@code true} if there are no more pages to load
     */
    public void setEndReached(boolean endReached) {
        this.endReached = endReached;
    }
    /**
     * Getter for the flag that indicates if the last page has been loaded
     * @return {@code true} if there are no more pages to load
     */
    public boolean isEndReached() {
        return endReached;
    }
}
