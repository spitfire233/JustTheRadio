package com.justtheradio.repository;


import static com.justtheradio.utils.constant.Constants.RADIO_BROWSER_EXPIRED_THRESHOLD;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.justtheradio.model.Result;
import com.justtheradio.model.radio.RadioStation;
import com.justtheradio.source.radio.BaseRadioBrowserRemoteDataSource;
import com.justtheradio.source.radio.BaseRadioLocalDataSource;
import com.justtheradio.source.radio.callback.RadioCallback;

import java.util.List;

public class RadioRepository implements RadioCallback {

    private final BaseRadioBrowserRemoteDataSource radioBrowserRemoteDataSource;
    private final BaseRadioLocalDataSource radioLocalDataSource;
    private final MutableLiveData<Result> countryCodesMutableLiveData;
    private final MutableLiveData<Result> radiosMutableLiveData;
    private String country = null;

    public RadioRepository(BaseRadioBrowserRemoteDataSource radioBrowserRemoteDataSource,
                            BaseRadioLocalDataSource radioLocalDataSource) {
        this.radioBrowserRemoteDataSource = radioBrowserRemoteDataSource;
        this.radioLocalDataSource = radioLocalDataSource;
        this.radioBrowserRemoteDataSource.setCallback(this);
        this.radioLocalDataSource.setCallback(this);
        this.countryCodesMutableLiveData = new MutableLiveData<>();
        this.radiosMutableLiveData = new MutableLiveData<>();
    }

    public LiveData<Result> fetchCountryCodes() {
        radioBrowserRemoteDataSource.getCountryCodes();
        return countryCodesMutableLiveData;
    }

    public LiveData<Result> fetchRadioStations(String country) {
        this.country = country;
        radioLocalDataSource.getLastTimeFetched();
        return radiosMutableLiveData;
    }


    @Override
    public void onCountryCodesFetchSuccess(List<String> countryCodes) {
        Result.Success<List<String>> result = new Result.Success<>(countryCodes);
        countryCodesMutableLiveData.postValue(result);
    }

    @Override
    public void onLastTimeUpdatedGetSuccess(String lastTimeUpdated) {
        if (Long.parseLong(lastTimeUpdated) >= RADIO_BROWSER_EXPIRED_THRESHOLD) {
            radioBrowserRemoteDataSource.getRadioStationsByCountryWithPaging(country);
        } else {
            radioBrowserRemoteDataSource.updatePaging();
            radioLocalDataSource.getRadioStationsWithPaging();
        }
    }

    @Override
    public void onLastTimeUpdatedNotPresent() {
        radioBrowserRemoteDataSource.getRadioStationsByCountryWithPaging(country);
    }

    @Override
    public void onFetchingRadioStationsSuccess(List<RadioStation> radioStations) {
        radioLocalDataSource.saveRadioStations(radioStations);
    }

    @Override
    public void onSavingRadioStationSuccess(List<RadioStation> radioStations) {
        Result.Success<List<RadioStation>> success = new Result.Success<>(radioStations);
        this.radiosMutableLiveData.postValue(success);
    }

    @Override
    public void onGettingRadioStationsFromLocal(List<RadioStation> radioStations) {
        Result.Success<List<RadioStation>> success = new Result.Success<>(radioStations);
        this.radiosMutableLiveData.postValue(success);
    }

    @Override
    public void onFailureFetchingCountryCodes(Exception exception) {
        Result.Failure result = new Result.Failure(exception.getMessage());
        countryCodesMutableLiveData.postValue(result);
    }

    @Override
    public void onFailureFetchingStationsFromRemote(Exception exception) {
        Result.Failure result = new Result.Failure(exception.getMessage());
        this.radiosMutableLiveData.postValue(result);
    }

    @Override
    public void onFailureFromLocal(Exception exception) {
        Result.Failure result = new Result.Failure(exception.getMessage());
        this.radiosMutableLiveData.postValue(result);
    }
}
