package com.justtheradio.source.radio;

import com.justtheradio.source.radio.callback.RadioCallback;

public abstract class BaseRadioBrowserRemoteDataSource {

    protected RadioCallback radioCallback;

    public void setCallback(RadioCallback radioCallback) {
        this.radioCallback = radioCallback;
    }

    public abstract void getCountryCodes();

    public abstract void getRadioStationsByCountryWithPaging(String countryCode);

    public abstract void updatePaging();
}
