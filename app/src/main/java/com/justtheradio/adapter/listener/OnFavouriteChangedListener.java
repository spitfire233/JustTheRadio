package com.justtheradio.adapter.listener;

public interface OnFavouriteChangedListener<T> {
    void onFavouriteStatusChanged(T item, boolean isFavourite);
}
