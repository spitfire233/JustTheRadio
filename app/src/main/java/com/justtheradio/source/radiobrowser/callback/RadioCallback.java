package com.justtheradio.source.radiobrowser.callback;

import java.util.List;

public interface RadioCallback {
    public void onCountryCodesGetSuccess(List<String> countryCodes);
    public void onFailureFromRemote(Exception exception);
}
