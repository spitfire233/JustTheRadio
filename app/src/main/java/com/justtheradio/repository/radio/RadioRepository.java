package com.justtheradio.repository.radio;

import androidx.lifecycle.MutableLiveData;

import com.justtheradio.model.Result;
import com.justtheradio.source.radiobrowser.BaseRadioBrowserRemoteDataSource;
import com.justtheradio.source.radiobrowser.BaseRadioLocalDataSource;
import com.justtheradio.source.radiobrowser.callback.RadioBrowserCallback;

import java.util.List;

public class RadioRepository implements RadioBrowserCallback {

    private final BaseRadioBrowserRemoteDataSource radioBrowserRemoteDataSource;
    private final BaseRadioLocalDataSource radioLocalDataSource;
    private final MutableLiveData<Result> countryCodesMutableLiveData;

    public RadioRepository(BaseRadioBrowserRemoteDataSource radioBrowserRemoteDataSource,
                           BaseRadioLocalDataSource radioLocalDataSource) {
        this.radioBrowserRemoteDataSource = radioBrowserRemoteDataSource;
        this.radioLocalDataSource = radioLocalDataSource;
        this.radioBrowserRemoteDataSource.setRadioBrowserCallback(this);

        this.countryCodesMutableLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<Result> fetchCountryCodes() {
        radioBrowserRemoteDataSource.getCountryCodes();
        return countryCodesMutableLiveData;
    }


    @Override
    public void onCountryCodesGetSuccess(List<String> countryCodes) {
        Result.Success<String> result = new Result.Success<String>(countryCodes);
        countryCodesMutableLiveData.postValue(result);
    }

    @Override
    public void onFailureFromRemote(Exception exception) {
        Result.Failure result = new Result.Failure(exception.getMessage());
        countryCodesMutableLiveData.postValue(result);
    }
}
