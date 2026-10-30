package com.justtheradio.ui.viewmodel.player;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

/**
 * Implementation of {@link ViewModelProvider.Factory}.
 * Factory used by the {@link ViewModelProvider} to create the {@link PlayerViewModel}.
 */
public class PlayerViewModelFactory implements ViewModelProvider.Factory {
    /**
     * Creates a new {@link PlayerViewModel}.
     *
     * @param modelClass The class of the ViewModel to create
     * @param <T>        The type of the ViewModel
     * @return A new {@link PlayerViewModel}
     */
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new PlayerViewModel();
    }
}
