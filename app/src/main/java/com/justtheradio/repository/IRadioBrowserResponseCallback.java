package com.justtheradio.repository;

import java.util.List;

import de.sfuhrm.radiobrowser4j.Station;

public interface IRadioBrowserResponseCallback {
    public void onSuccessRetrievingNationalRadioStations(List<Station> stations);
    public void onFailureOnBuildingRadioBrowser(Exception exception);

}
