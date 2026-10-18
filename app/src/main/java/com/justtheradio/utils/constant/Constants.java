package com.justtheradio.utils.constant;

public class Constants {
    public static final String MAIN_REPOSITORY = "https://github.com/spitfire233/JustTheRadio";
    public static final String VERSION = "1.0.1";
    public static final String AGENT = "JustTheRadio/" + VERSION;
    public static final int RADIO_BROWSER_TIMEOUT_MS = 5000;

    public static final String ERROR_ENDPOINT_NOT_FOUND = "No endpoint found for agent" + AGENT;

    public static final String FLAG_PATH_FORMAT_STRING = "file:///android_asset/flags/%s.png";

    public static final String BLOCKED_URL_ERROR = "Blocked URL: ";
    public static final String BLOCKED_PRIVATE_ADDRESS_ERROR = "Blocker private address for: ";
    public static final String NOT_AN_IMAGE_ERROR = "Not an image: ";
    public static final String IMAGE_TOO_LARGE_ERROR = "Image to large: ";
    public static final String NO_KEY_WITH_THAT_NAME_IN_DATASTORE = "No key with that name in local cache!";

    public static final String CHOSEN_COUNTRY_KEY = "country";
    public static final String LAST_TIME_UPDATED_KEY = "last_time_updated";
    public static final String DATASTORE_NAME = "datastore";

    public static final int MAX_URL_LENGTH = 2048;
    public static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024; // 5MB
    public static final int GLIDE_CONNECTION_TIMEOUT = 10;
    public static final int GLIDE_READ_CONNECTION_TIMEOUT = 15;
    public static final int RADIO_BROWSER_PAGING_LIMIT = 64;
    public static final long RADIO_BROWSER_EXPIRED_THRESHOLD = 600000; // 10 minutes

    public static final int ROOM_DATABASE_VERSION = 2;
    public static final String ROOM_DATABASE_NAME = "radio-db";



}
