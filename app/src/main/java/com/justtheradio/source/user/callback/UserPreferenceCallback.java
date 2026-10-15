package com.justtheradio.source.user.callback;

public interface UserPreferenceCallback {
    void onSavingUserPreferenceSuccess();
    void onRetrievingUserPreferenceSuccess(String resource);
    void onFailureSavingOnLocal(Exception exception);
    void onFailureRetrievingOnLocal(Exception exception);
}
