package com.justtheradio.repository;


import static com.justtheradio.utils.constant.Constants.RADIO_BROWSER_EXPIRED_THRESHOLD;
import static com.justtheradio.utils.constant.Constants.RADIO_BROWSER_PAGING_LIMIT;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingLiveData;
import androidx.paging.PagingSource;

import com.justtheradio.model.Result;
import com.justtheradio.model.RadioStation;
import com.justtheradio.source.radio.BaseRadioBrowserRemoteDataSource;
import com.justtheradio.source.radio.BaseRadioLocalDataSource;
import com.justtheradio.source.radio.callback.RadioCallback;
import com.justtheradio.source.radio.paging.RadioRemoteMediator;

import java.util.List;

public class RadioRepository implements RadioCallback {

    private final BaseRadioBrowserRemoteDataSource radioBrowserRemoteDataSource;
    private final BaseRadioLocalDataSource radioLocalDataSource;
    private final MutableLiveData<Result> countryCodesMutableLiveData;
    private final MutableLiveData<Result> radiosMutableLiveData;
    private final RadioRemoteMediator radioRemoteMediator;

    public RadioRepository(BaseRadioBrowserRemoteDataSource radioBrowserRemoteDataSource,
                            BaseRadioLocalDataSource radioLocalDataSource,
                           RadioRemoteMediator radioRemoteMediator) {
        this.radioBrowserRemoteDataSource = radioBrowserRemoteDataSource;
        this.radioLocalDataSource = radioLocalDataSource;
        this.radioBrowserRemoteDataSource.setCallback(this);
        this.radioLocalDataSource.setCallback(this);
        this.countryCodesMutableLiveData = new MutableLiveData<>();
        this.radiosMutableLiveData = new MutableLiveData<>();
        this.radioRemoteMediator = radioRemoteMediator;
    }

    public LiveData<PagingData<RadioStation>> getRadioStations(String country) {
        radioRemoteMediator.setCountry(country);
        PagingConfig pagingConfig = new PagingConfig(
                RADIO_BROWSER_PAGING_LIMIT,
                RADIO_BROWSER_PAGING_LIMIT / 2, // Prefetch distance
                true,                                        // Enable placeholders
                RADIO_BROWSER_PAGING_LIMIT * 2                 // Initial loading size
        );
        Pager<Integer, RadioStation> pager = new Pager<>(
                pagingConfig,
                null,
                radioRemoteMediator,
                radioLocalDataSource::getRadioDAOPagingSource
        );
        return PagingLiveData.getLiveData(pager);
    }


    public LiveData<Result> fetchCountryCodes() {
        radioBrowserRemoteDataSource.getCountryCodes();
        return countryCodesMutableLiveData;
    }


    @Override
    public void onCountryCodesFetchSuccess(List<String> countryCodes) {
        Result.Success<List<String>> result = new Result.Success<>(countryCodes);
        countryCodesMutableLiveData.postValue(result);
    }

    @Override
    public void onSavingRadioStationSuccess(List<RadioStation> radioStations) {
        Result.Success<List<RadioStation>> success = new Result.Success<>(radioStations);
        this.radiosMutableLiveData.postValue(success);
    }

    @Override
    public void onFailureFetchingCountryCodes(Exception exception) {
        Result.Failure result = new Result.Failure(exception.getMessage());
        countryCodesMutableLiveData.postValue(result);
    }

    @Override
    public void onFailureFromLocal(Exception exception) {
        Result.Failure result = new Result.Failure(exception.getMessage());
        this.radiosMutableLiveData.postValue(result);
    }
}
