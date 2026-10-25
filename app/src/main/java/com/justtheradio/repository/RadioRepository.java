package com.justtheradio.repository;


import static com.justtheradio.utils.constant.Constants.RADIO_BROWSER_PAGING_LIMIT;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingLiveData;
import androidx.paging.rxjava3.PagingRx;

import com.justtheradio.model.Result;
import com.justtheradio.model.RadioStation;
import com.justtheradio.paging.RadioSearchPagingSource;
import com.justtheradio.source.BaseRadioBrowserRemoteDataSource;
import com.justtheradio.source.BaseRadioLocalDataSource;
import com.justtheradio.source.callback.RadioCallback;
import com.justtheradio.paging.RadioRemoteMediator;

import java.util.List;

import io.reactivex.rxjava3.core.Flowable;

public class RadioRepository implements RadioCallback {

    private final BaseRadioBrowserRemoteDataSource radioBrowserRemoteDataSource;
    private final BaseRadioLocalDataSource radioLocalDataSource;
    private final MutableLiveData<Result> countryCodesMutableLiveData;
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
        this.cacheRetrieveMutableLiveData = new MutableLiveData<>();
        this.cacheSavingMutableLiveData = new MutableLiveData<>();
        this.radioRemoteMediator = radioRemoteMediator;
    }

    public Flowable<PagingData<RadioStation>> getRadioStations() {
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
        return PagingRx.getFlowable(pager);
    }

    public Flowable<PagingData<RadioStation>> searchStations(String query) {
        PagingConfig pagingConfig = new PagingConfig(
                RADIO_BROWSER_PAGING_LIMIT,
                RADIO_BROWSER_PAGING_LIMIT / 2,
                true,
                RADIO_BROWSER_PAGING_LIMIT * 2
        );
        Pager<Integer, RadioStation> pager = new Pager<>(
                pagingConfig,
                () -> new RadioSearchPagingSource(radioBrowserRemoteDataSource, radioLocalDataSource, query)
        );
        return PagingRx.getFlowable(pager);
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
