package com.justtheradio.source.user;

import com.justtheradio.source.user.callback.UserPreferenceCallback;

public abstract class BaseUserPreferencesLocalDataSource {
    protected UserPreferenceCallback userCallback;

    public void setUserCallback(UserPreferenceCallback userCallback) {
        this.userCallback = userCallback;
    }

    public abstract void saveCountryPreference(String key, String country);
    public abstract void getStringResource(String key);
}
