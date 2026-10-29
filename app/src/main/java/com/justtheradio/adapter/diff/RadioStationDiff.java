package com.justtheradio.adapter.diff;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.justtheradio.model.radio.BaseRadioStation;

/**
 * Utility class to handle the instantiation of a {@link DiffUtil.ItemCallback} for the radio station
 * adapters
 */
public class RadioStationDiff {
    /**
     * Getter method that returns a {@link DiffUtil.ItemCallback} object for a specific
     * {@link BaseRadioStation} implementation
     * @return A {@link DiffUtil.ItemCallback} object specific fo the provided
     * {@link BaseRadioStation}  implementation
     * @param <T> Parameter class. Must extend {@link BaseRadioStation}
     */
    public static <T extends BaseRadioStation> DiffUtil.ItemCallback<T> getDiffCallback() {
        return new DiffUtil.ItemCallback<>() {
            /**
             * Compares the station contained in the old list with the station
             * contained in the new list.
             * @param oldItem The item in the old list.
             * @param newItem The item in the new list.
             * @return {@code true} if the two objects represent the same station, that is, if
             * they have the same {@link BaseRadioStation#getUUID() UUID} , {@code false} otherwise.
             */
            @Override
            public boolean areItemsTheSame(@NonNull T oldItem, @NonNull T newItem) {
                return oldItem.getUUID().equals(newItem.getUUID());
            }
            /**
             * Compares the data of the station in the old list with the data of the
             * station in new list.
             * @param oldItem The item in the old list.
             * @param newItem The item in the new list.
             * @return {@code true} if the two stations have the same identifying data
             * (i.e. {@link BaseRadioStation#getName() name} and
             * {@link BaseRadioStation#getURL() URL}), {@code false} otherwise
             */
            @Override
            public boolean areContentsTheSame(@NonNull T oldItem, @NonNull T newItem) {
                return oldItem.getName().equals(newItem.getName())
                        && oldItem.getURL().equals(newItem.getURL());
            }
        };
    }
}
