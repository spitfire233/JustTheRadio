package com.justtheradio.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.justtheradio.source.BaseRadioBrowserRemoteDataSource;

import java.util.List;

import de.sfuhrm.radiobrowser4j.Station;

public class RadioStationsRepository implements IRadioBrowserResponseCallback{
    private final MutableLiveData<List<Station>> nationalRadiosMutableLiveData;
    private final BaseRadioBrowserRemoteDataSource radioBrowserRemoteDataSource;


    public RadioStationsRepository(BaseRadioBrowserRemoteDataSource radioBrowserRemoteDataSource) {
        nationalRadiosMutableLiveData = new MutableLiveData<>();
        this.radioBrowserRemoteDataSource = radioBrowserRemoteDataSource;
        radioBrowserRemoteDataSource.setCallback(this);
    }

    public LiveData<List<Station>>
    fetchNationalRadioStations(String countryCode, int offset, int limit) {
        radioBrowserRemoteDataSource.getNationalRadios(countryCode, offset, limit);
        return nationalRadiosMutableLiveData;
    }


    @Override
    public void onSuccessRetrievingNationalRadioStations(List<Station> stations) {
        nationalRadiosMutableLiveData.postValue(stations);
    }

    @Override
    public void onFailureOnBuildingRadioBrowser(Exception exception) {

    }
}
