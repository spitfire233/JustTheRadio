package com.justtheradio.repository;

import com.justtheradio.model.RadioStation;

import java.util.List;

public interface IRadioBrowserResponseCallback {
    public void onSuccessFromRemote(List<RadioStation> stations);
    public void onFailureFromRemote(Exception exception);
}
