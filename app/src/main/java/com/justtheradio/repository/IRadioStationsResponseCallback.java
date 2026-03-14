package com.justtheradio.repository;

import com.justtheradio.model.RadioStation;

import java.util.List;

public interface IRadioStationsResponseCallback {
    public void onSuccessFetchingNationalRadioStations(List<RadioStation> stations);
    public void onSuccessFetchingInternationalRadioStations(List<RadioStation> stations);
    public void onSuccessSavingFromLocal(List<RadioStation> radioStations, boolean isSavingNational);
    public void onFailureFetchingNationalRadioStations(Exception exception);
    public void onFailureFetchingInternationalRadioStations(Exception exception);
    public void onFailureFromLocal(Exception exception, boolean isSavingNational);
}
