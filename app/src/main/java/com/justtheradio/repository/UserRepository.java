package com.justtheradio.repository;

import androidx.lifecycle.MutableLiveData;

import com.justtheradio.model.Result;
import com.justtheradio.source.user.BaseUserPreferencesLocalDataSource;
import com.justtheradio.source.user.callback.UserPreferenceCallback;

public class UserRepository implements UserPreferenceCallback {

    private final BaseUserPreferencesLocalDataSource userPreferencesLocalDataSource;
    private final MutableLiveData<Result> saveMutableLiveData;
    private final MutableLiveData<Result> getMutableLiveData;

    public UserRepository(BaseUserPreferencesLocalDataSource userPreferencesLocalDataSource) {
        this.userPreferencesLocalDataSource = userPreferencesLocalDataSource;
        saveMutableLiveData = new MutableLiveData<>();
        getMutableLiveData = new MutableLiveData<>();
        userPreferencesLocalDataSource.setUserCallback(this);
    }

    public MutableLiveData<Result> saveUserPreference(String key, String resource) {
        userPreferencesLocalDataSource.saveCountryPreference(key, resource);
        return saveMutableLiveData;
    }

    public MutableLiveData<Result> getUserPreference(String key) {
        userPreferencesLocalDataSource.getStringResource(key);
        return getMutableLiveData;
    }

    @Override
    public void onSavingUserPreferenceSuccess() {
        Result.Success<Void> success = new Result.Success<>();
        saveMutableLiveData.postValue(success);
    }

    @Override
    public void onRetrievingUserPreferenceSuccess(String resource) {
        Result.Success<String> success = new Result.Success<>(resource);
        getMutableLiveData.postValue(success);
    }

    @Override
    public void onFailureSavingOnLocal(Exception exception) {
        Result.Failure failure = new Result.Failure(exception.getMessage());
        saveMutableLiveData.postValue(failure);
    }

    @Override
    public void onFailureRetrievingOnLocal(Exception exception) {
        Result.Failure failure = new Result.Failure(exception.getMessage());
        getMutableLiveData.postValue(failure);
    }
}
