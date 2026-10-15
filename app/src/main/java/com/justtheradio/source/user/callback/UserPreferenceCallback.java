package com.justtheradio.source.user.callback;

public interface UserPreferenceCallback {
    public void onSavingUserPreferenceSuccess();
    public void onRetrievingUserPreferenceSuccess(String resource);
    public void onFailureSavingOnLocal(Exception exception);
    public void onFailureRetrievingOnLocal(Exception exception);
}
