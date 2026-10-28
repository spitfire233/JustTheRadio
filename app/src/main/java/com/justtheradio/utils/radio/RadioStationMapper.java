package com.justtheradio.utils.radio;

import androidx.annotation.Nullable;

import com.justtheradio.model.radio.RadioStation;
import com.justtheradio.utils.security.SecurityUtils;

import de.sfuhrm.radiobrowser4j.Station;

public final class RadioStationMapper {

    @Nullable
    public static RadioStation fromStationToRadioStationModel(Station raw) {
        RadioStation radioStation = null;
        String resolved = raw.getUrlResolved();
        String unresolved = raw.getUrl();
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
