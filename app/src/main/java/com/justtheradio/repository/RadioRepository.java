package com.justtheradio.repository;


import static com.justtheradio.utils.constant.Constants.RADIO_BROWSER_PAGING_LIMIT;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingLiveData;

import com.justtheradio.model.Result;
import com.justtheradio.model.RadioStation;
import com.justtheradio.source.BaseRadioBrowserRemoteDataSource;
import com.justtheradio.source.BaseRadioLocalDataSource;
import com.justtheradio.source.callback.RadioCallback;
import com.justtheradio.mediator.RadioRemoteMediator;

import java.util.List;

public class RadioRepository implements RadioCallback {

    private final BaseRadioBrowserRemoteDataSource radioBrowserRemoteDataSource;
    private final BaseRadioLocalDataSource radioLocalDataSource;
    private final MutableLiveData<Result> countryCodesMutableLiveData;
    private final MutableLiveData<Result> radiosMutableLiveData;
    private final MutableLiveData<Result> cacheRetrieveMutableLiveData;
    private final MutableLiveData<Result> cacheSavingMutableLiveData;
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
        this.cacheRetrieveMutableLiveData = new MutableLiveData<>();
        this.cacheSavingMutableLiveData = new MutableLiveData<>();

        this.radioRemoteMediator = radioRemoteMediator;
    }

    public LiveData<PagingData<RadioStation>> getRadioStations(String country) {
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

    public LiveData<Result> saveCountryPreference(String country) {
        radioLocalDataSource.saveCountryPreference(country);
        return cacheSavingMutableLiveData;
    }

    public LiveData<Result> getCountryPreference() {
        radioLocalDataSource.getCountryPreferenceAsync();
        return cacheRetrieveMutableLiveData;
    }


    @Override
    public void onCountryCodesFetchSuccess(List<String> countryCodes) {
        Result.Success<List<String>> result = new Result.Success<>(countryCodes);
        countryCodesMutableLiveData.postValue(result);
    }

    @Override
    public void onFailureFetchingCountryCodes(Exception exception) {
        Result.Failure result = new Result.Failure(exception.getMessage());
        countryCodesMutableLiveData.postValue(result);
    }

    @Override
    public void onSavingCountryPreferenceSuccess() {
        Result.Success<Void> result = new Result.Success<>();
        cacheSavingMutableLiveData.postValue(result);
    }

    @Override
    public void onGettingCountryPreferenceSuccess(String country) {
        Result.Success<String> result = new Result.Success<>(country);
        cacheRetrieveMutableLiveData.postValue(result);
    }

    @Override
    public void onFailureSavingCountryPreference(Exception exception) {
        Result.Failure result = new Result.Failure(exception.getMessage());
        cacheSavingMutableLiveData.postValue(result);
    }

    @Override
    public void onFailureGettingCountryPreference(Exception exception) {
        Result.Failure result = new Result.Failure(exception.getMessage());
        cacheRetrieveMutableLiveData.postValue(result);
    }
}
