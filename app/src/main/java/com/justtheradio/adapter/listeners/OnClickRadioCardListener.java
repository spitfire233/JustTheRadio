package com.justtheradio.adapter.listeners;

import com.justtheradio.model.RadioStation;

public interface OnClickRadioCardListener {
    public void onFavouriteButtonPressed(int position);
    public void onRadioItemPressed(RadioStation station);
}
