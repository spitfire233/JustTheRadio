package com.justtheradio.utils.constant;

/**
 * Utility class that holds the constants used by the local cache: the DataStore, which
 * stores the simple preferences, and the Room database, which stores the stations.
 */
public class LocalCacheConstants {
    /** Message of the error raised when a key is not present in the DataStore. */
    public static final String NO_KEY_WITH_THAT_NAME_IN_DATASTORE = "No key with that name in local cache!";
    /** Key under which the country chosen by the user is stored in the DataStore. */
    public static final String CHOSEN_COUNTRY_KEY = "country";
    /** Key under which the time of the last refresh of the cached stations is stored in
     * the DataStore.
     */
    public static final String LAST_TIME_UPDATED_KEY = "last_time_updated";
    /** Name of the DataStore file. */
    public static final String DATASTORE_NAME = "datastore";
    /** Version of the Room database schema; to be increased when the schema changes. */
    public static final int ROOM_DATABASE_VERSION = 1;
    /** Name of the Room database file. */
    public static final String ROOM_DATABASE_NAME = "radio-db";
}
