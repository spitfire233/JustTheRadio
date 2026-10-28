package com.justtheradio.adapter.listener;


/**
 * Listener interface to handle user interaction with the items presented in the UI.
 * @param <T> The type of object contained in the UI element clicked by the user.
 */
public interface OnItemClickListener<T> {
    /**
     * Callback for when the user clicks/taps on the item.
     * @param item The selected item
     */
    void onItemClick(T item);
}
