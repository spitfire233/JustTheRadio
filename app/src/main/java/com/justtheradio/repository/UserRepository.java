package com.justtheradio.repository;

import static com.justtheradio.utils.constant.Constants.CHOSEN_COUNTRY_KEY;

import androidx.lifecycle.MutableLiveData;

import com.justtheradio.model.Result;
import com.justtheradio.source.user.BaseUserPreferencesLocalDataSource;
import com.justtheradio.source.user.callback.UserPreferenceCallback;

public class UserRepository implements UserPreferenceCallback {

    private final BaseUserPreferencesLocalDataSource userPreferencesLocalDataSource;
    private final MutableLiveData<Result> userPreferencesMutableLiveData;

    public UserRepository(BaseUserPreferencesLocalDataSource userPreferencesLocalDataSource) {
        this.userPreferencesLocalDataSource = userPreferencesLocalDataSource;
        userPreferencesMutableLiveData = new MutableLiveData<>();
        userPreferencesLocalDataSource.setUserCallback(this);
    }

    public MutableLiveData<Result> saveUserPreference(String key, String resource) {
        userPreferencesLocalDataSource.saveCountryPreference(key, resource);
        return userPreferencesMutableLiveData;
    }

    public MutableLiveData<Result> getUserPreference(String key) {
        userPreferencesLocalDataSource.getStringResource(key);
        return userPreferencesMutableLiveData;
    }

    @Override
    public void onSavingUserPreferenceSuccess() {
        Result.Success<Void> success = new Result.Success<>();
        userPreferencesMutableLiveData.postValue(success);
    }

    @Override
    public void onRetrievingUserPreferenceSuccess(String resource) {
        Result.Success<String> success = new Result.Success<>(resource);
        userPreferencesMutableLiveData.postValue(success);
    }

    @Override
    public void onFailureFromLocal(Exception exception) {
        Result.Failure failure = new Result.Failure(exception.getMessage());
        userPreferencesMutableLiveData.postValue(failure);
    }
}
