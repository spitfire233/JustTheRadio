package com.justtheradio.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.justtheradio.model.RadioStation;
import com.justtheradio.source.BaseRadioStationsRemoteDataSource;

import java.util.List;

public class RadioStationsRepository implements IRadioStationsResponseCallback {
    private final MutableLiveData<List<RadioStation>> nationalRadiosMutableLiveData;
    private final BaseRadioStationsRemoteDataSource radioBrowserRemoteDataSource;


    public RadioStationsRepository(BaseRadioStationsRemoteDataSource radioBrowserRemoteDataSource) {
        nationalRadiosMutableLiveData = new MutableLiveData<List<RadioStation>>();
        this.radioBrowserRemoteDataSource = radioBrowserRemoteDataSource;
        radioBrowserRemoteDataSource.setCallback(this);
    }

    public LiveData<List<RadioStation>>
    fetchNationalRadioStations(String countryCode, int offset, int limit) {
        radioBrowserRemoteDataSource.getNationalRadios(countryCode, offset, limit);
        return nationalRadiosMutableLiveData;
    }


    @Override
    public void onSuccessFromRemote(List<RadioStation> stations) {
        nationalRadiosMutableLiveData.postValue(stations);
    }

    @Override
    public void onFailureFromRemote(Exception exception) {

    }
}
