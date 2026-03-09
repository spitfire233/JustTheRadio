package com.justtheradio.adapter.listeners;

import de.sfuhrm.radiobrowser4j.Station;

public interface OnClickRadioCardListener {
    public void onFavouriteButtonPressed(int position);
    public void onRadioItemPressed(Station station);
}
