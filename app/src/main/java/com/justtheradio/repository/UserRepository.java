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

    public MutableLiveData<Result> saveCountryPreference(String country) {
        userPreferencesLocalDataSource.saveCountryPreference(CHOSEN_COUNTRY_KEY, country);
        return userPreferencesMutableLiveData;
    }


    @Override
    public void onSavingUserPreferenceSuccess() {
        Result.Success<Void> success = new Result.Success<>();
        userPreferencesMutableLiveData.postValue(success);
    }

    @Override
    public void onRetrievingUserPreferenceSuccess(String resource) {

    }

    @Override
    public void onFailureFromLocal(Exception exception) {
        Result.Failure failure = new Result.Failure(exception.getMessage());
        userPreferencesMutableLiveData.postValue(failure);
    }
}
