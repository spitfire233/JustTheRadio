package com.justtheradio.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.justtheradio.model.RadioStation;
import com.justtheradio.model.Result;
import com.justtheradio.source.BaseRadioStationsLocalDataSource;
import com.justtheradio.source.BaseRadioStationsRemoteDataSource;

import java.util.List;

public class RadioStationsRepository implements IRadioStationsResponseCallback {
    private final MutableLiveData<Result> nationalRadiosMutableLiveData;
    private final MutableLiveData<Result> internationalRadioStationsMutableLiveData;
    private final BaseRadioStationsRemoteDataSource radioStationsRemoteDataSource;
    private final BaseRadioStationsLocalDataSource radioStationsLocalDataSource;


    public RadioStationsRepository(BaseRadioStationsRemoteDataSource radioStationsRemoteDataSource,
                                   BaseRadioStationsLocalDataSource radioStationsLocalDataSource) {
        nationalRadiosMutableLiveData = new MutableLiveData<>();
        internationalRadioStationsMutableLiveData = new MutableLiveData<>();
        this.radioStationsRemoteDataSource = radioStationsRemoteDataSource;
        this.radioStationsLocalDataSource = radioStationsLocalDataSource;
        radioStationsRemoteDataSource.setCallback(this);
        radioStationsLocalDataSource.setCallback(this);
    }

    public LiveData<Result> fetchNationalRadioStations(String countryCode,
                                                       int offset, int limit) {
        radioStationsRemoteDataSource.getNationalRadioStations(countryCode, offset, limit);
        return nationalRadiosMutableLiveData;
    }

    public LiveData<Result> fetchInternationalRadioStations(List<String> countryCodes,
                                                            int offset, int limit) {
        radioStationsRemoteDataSource.getInternationalRadioStations(countryCodes, offset, limit);
        return internationalRadioStationsMutableLiveData;
    }


    @Override
    public void onSuccessFetchingNationalRadioStations(List<RadioStation> stations) {
        radioStationsLocalDataSource.saveRadioStations(stations, true);
    }

    @Override
    public void onSuccessFetchingInternationalRadioStations(List<RadioStation> stations) {
        radioStationsLocalDataSource.saveRadioStations(stations, false);
    }

    @Override
    public void onSuccessSavingFromLocal(List<RadioStation> radioStations,
                                         boolean isSavingNational) {
        Result.Success result = new Result.Success(radioStations);
        if(isSavingNational)
            nationalRadiosMutableLiveData.postValue(result);
        else
            internationalRadioStationsMutableLiveData.postValue(result);
    }

    @Override
    public void onFailureFetchingNationalRadioStations(Exception exception) {
        Result.Error result = new Result.Error(exception.getMessage());
        nationalRadiosMutableLiveData.postValue(result);
    }

    @Override
    public void onFailureFetchingInternationalRadioStations(Exception exception) {
        Result.Error result = new Result.Error(exception.getMessage());
        internationalRadioStationsMutableLiveData.postValue(result);
    }

    @Override
    public void onFailureFromLocal(Exception exception, boolean isSavingNational) {
        Result.Error result = new Result.Error(exception.getMessage());
        if(isSavingNational)
            nationalRadiosMutableLiveData.postValue(result);
        else
            internationalRadioStationsMutableLiveData.postValue(result);
    }
}
