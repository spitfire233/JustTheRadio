package com.justtheradio.source.user.callback;

public interface UserPreferenceCallback {
    public void onSavingUserPreferenceSuccess();
    public void onRetrievingUserPreferenceSuccess(String resource);
    public void onFailureFromLocal(Exception exception);
}
