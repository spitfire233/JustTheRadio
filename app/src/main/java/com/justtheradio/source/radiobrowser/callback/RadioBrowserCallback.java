package com.justtheradio.source.radiobrowser.callback;

import java.util.List;

public interface RadioBrowserCallback {
    public void onCountryCodesGetSuccess(List<String> countryCodes);
    public void onFailureFromRemote(Exception exception);
}
