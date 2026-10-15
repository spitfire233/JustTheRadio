package com.justtheradio.source.radiobrowser.callback;

import java.util.List;

public interface RadioCallback {
    void onCountryCodesGetSuccess(List<String> countryCodes);
    void onFailureFromRemote(Exception exception);
}
