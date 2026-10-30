package com.justtheradio.utils.radio;

import androidx.annotation.Nullable;

import com.justtheradio.model.radio.RadioStation;
import com.justtheradio.utils.security.SecurityUtils;

import de.sfuhrm.radiobrowser4j.Station;

/**
 * Utility class that converts the stations returned by the Radio Browser API into the model
 * used by the app.
 * The data comes from an external source and cannot be trusted, so the URLs of a station
 * are validated with {@link SecurityUtils} during the conversion, and stations that do not
 * have valid URLs are discarded.
 */
public final class RadioStationMapper {
    /**
     * Converts a {@link Station} of the Radio Browser API into a {@link RadioStation}.
     * <ul>
     *   <li>For the stream, the resolved URL is preferred and the original one is used only
     *       if the resolved one is not valid. The stream can be served with HTTP</li>
     *   <li>The favicon (the logo of the station) must also be a valid URL. The
     *       favicon must be served with HTTPS
     *   </li>
     * </ul>
     * If there is no valid stream URL or the favicon is not valid, the station is discarded.
     *
     * @param raw the station returned by the API
     * @return the converted station, or {@code null} if the station has no valid stream URL
     * or favicon and must be skipped
     */
    @Nullable
    public static RadioStation fromStationToRadioStationModel(Station raw) {
        RadioStation radioStation = null;
        String resolved = raw.getUrlResolved();
        String unresolved = raw.getUrl();
        // Prefer the resolved URL, fall back to the original one,
        // otherwise there is no usable stream
        String url = SecurityUtils.isValidUrl(resolved, false) ? resolved :
                     SecurityUtils.isValidUrl(unresolved, false) ? unresolved : null;
        if (url != null && SecurityUtils.isValidUrl(raw.getFavicon(), true)) {
            radioStation = new RadioStation(
                    raw.getStationUUID(),
                    url, raw.getFavicon(),
                    raw.getName()
            );
        }
        return radioStation;
    }
}
