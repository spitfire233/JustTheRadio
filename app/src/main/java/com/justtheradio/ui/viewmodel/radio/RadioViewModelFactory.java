package com.justtheradio.ui.viewmodel.radio;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.justtheradio.repository.RadioRepository;

/**
 * Implementation of {@link ViewModelProvider.Factory}
 * Factory used by the {@link ViewModelProvider} to create the {@link RadioViewModel}.
 */
public class RadioViewModelFactory implements ViewModelProvider.Factory {
    /** Repository passed to the ViewModel when it is created. */
    private final RadioRepository repository;
    /**
     * Constructor for the {@link RadioViewModelFactory} class.
     * @param repository The {@link RadioRepository} to give to the {@link RadioViewModel}
     */
    public RadioViewModelFactory(RadioRepository repository) {
        this.repository = repository;
    }
    /**
     * Creates a new {@link RadioViewModel} using the repository of the factory.
     *
     * @param modelClass The class of the ViewModel to create
     * @param <T>        The type of the ViewModel
     * @return A new {@link RadioViewModel}, cast to the requested type
     */
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new RadioViewModel(repository);
    }
}
