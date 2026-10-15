package com.justtheradio.repository;


import androidx.lifecycle.MutableLiveData;

import com.justtheradio.model.Result;
import com.justtheradio.source.radiobrowser.BaseRadioBrowserRemoteDataSource;
import com.justtheradio.source.radiobrowser.callback.RadioCallback;

import java.util.List;

public class RadioRepository implements RadioCallback {

    private final BaseRadioBrowserRemoteDataSource radioBrowserRemoteDataSource;
    private final MutableLiveData<Result> countryCodesMutableLiveData;

    public RadioRepository(BaseRadioBrowserRemoteDataSource radioBrowserRemoteDataSource) {
        this.radioBrowserRemoteDataSource = radioBrowserRemoteDataSource;
        this.radioBrowserRemoteDataSource.setRadioBrowserCallback(this);
        this.countryCodesMutableLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<Result> fetchCountryCodes() {
        radioBrowserRemoteDataSource.getCountryCodes();
        return countryCodesMutableLiveData;
    }


    @Override
    public void onCountryCodesGetSuccess(List<String> countryCodes) {
        Result.Success<List<String>> result = new Result.Success<>(countryCodes);
        countryCodesMutableLiveData.postValue(result);
    }

    @Override
    public void onFailureFromRemote(Exception exception) {
        Result.Failure result = new Result.Failure(exception.getMessage());
        countryCodesMutableLiveData.postValue(result);
    }
}
