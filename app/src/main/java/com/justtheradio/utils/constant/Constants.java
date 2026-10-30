package com.justtheradio.utils.constant;

/**
 * Utility class that holds generic constants used by the app.
 */
public class Constants {
    /** URL of the main repository of the project, opened in the browser from the intro screen. */
    public static final String MAIN_REPOSITORY = "https://github.com/spitfire233/JustTheRadio";
    /**
     * Format of the path of the flag image of a country, which is stored in the assets of the
     * app. The placeholder {@code %s} is replaced by the country code.
     */
    public static final String FLAG_PATH_FORMAT_STRING = "file:///android_asset/flags/%s.png";
    /** Regular expression of a valid country code: exactly two uppercase letters (ISO 3166-1 alpha-2). */
    public static final String ISO_COUNTRY_REGEX = "^[A-Z]{2}$";
}
