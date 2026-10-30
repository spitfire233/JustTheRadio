package com.justtheradio.utils.constant;

/**
 * Utility class that holds the constants used for the network requests: the error messages
 * of the checks on the URLs and on the images, the limits on their size, and the timeouts
 * of the HTTP clients.
 */
public class NetworkConstants {
    /** Message prefix of the error raised when a URL is blocked; should be followed by the URL. */
    public static final String BLOCKED_URL_ERROR = "Blocked URL: ";
    /**
     * Message prefix of the error raised when a request is blocked because it points to a
     * private address; should be followed by the URL.
     */
    public static final String BLOCKED_PRIVATE_ADDRESS_ERROR = "Blocker private address for: ";
    /** Message prefix of the error raised when an image is larger than the limit;
     * should followed by the URL.
     */
    public static final String NOT_AN_IMAGE_ERROR = "Not an image: ";
    /** Maximum length accepted for a URL, in characters. */
    public static final String IMAGE_TOO_LARGE_ERROR = "Image to large: ";
    /** Maximum length accepted for a URL, in characters. */
    public static final int MAX_URL_LENGTH = 2048;
    /** Maximum size accepted for an image, in bytes (5 MB). */
    public static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;
    /** Timeout for establishing the connection when Glide downloads an image, in seconds. */
    public static final int GLIDE_CONNECTION_TIMEOUT = 10;
    /** Timeout for reading data when Glide downloads an image, in seconds. */
    public static final int GLIDE_READ_CONNECTION_TIMEOUT = 15;
    /** Timeout for establishing the connection of the OkHttp client, 10 seconds. */
    public static final int OKHTTP_CONNECTION_TIMEOUT = 10;
    /** Timeout for reading data with the OkHttp client, 30 seconds. */
    public static final int OKHTTP_READ_TIMEOUT = 30;
}
