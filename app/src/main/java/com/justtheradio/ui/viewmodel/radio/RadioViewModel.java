package com.justtheradio.ui.viewmodel.radio;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.LiveDataReactiveStreams;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.PagingData;
import androidx.paging.rxjava3.PagingRx;

import com.justtheradio.model.radio.BaseRadioStation;
import com.justtheradio.model.radio.FavouriteStation;
import com.justtheradio.model.radio.RadioStation;
import com.justtheradio.model.Result;
import com.justtheradio.repository.RadioRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.BehaviorProcessor;

/**
 * {@link ViewModel} that gives the screens access to the radio stations, the favourites and the
 * country preference, using the {@link RadioRepository}.
 * The radio stations are obtained from a Flowable switch map depending on the contents
 * of the query string: if the string is empty, then the user is simply browsing the
 * available radio stations and the obtained results are cached in Room. Otherwise, the
 * user is searching by name and the stations are presented directly from the API.
 */
public class RadioViewModel extends ViewModel {
    /** Repository from which the data is obtained and to which the changes are sent. */
    private final RadioRepository repository;
    /** Current search query; starts empty, which means "no search". */
    private final BehaviorProcessor<String> query = BehaviorProcessor.createDefault("");
    /** Paged list of stations, either the stations of the country or the search results. */
    private final LiveData<PagingData<RadioStation>> radioStations;
    /** Identifiers of the favourite stations. */
    private final LiveData<Set<UUID>> favouriteUUIDs;
    /** Full list of the favourite stations. */
    private final LiveData<List<FavouriteStation>> favouriteStations;

    /**
     * Constructor for the {@link RadioViewModel} class.
     * Creates the ViewModel and sets up the streams of data exposed as {@link LiveData}.
     *
     * @param repository The repository from which the data is obtained
     */
    public RadioViewModel(RadioRepository repository) {
        this.repository = repository;
        // Switch map to change data source based on currently set query string
        Flowable<PagingData<RadioStation>> flow = query
                .distinctUntilChanged() // emits all distinct data, based on the equals method
                .switchMap(q -> q.isEmpty()
                          ? repository.getRadioStations() // User is browsing, cache the data
                          : repository.searchStations(q)); // User is searching, present from network
        // Cache the paged data in the scope of the ViewModel,
        // to keep it across configuration changes
        radioStations = LiveDataReactiveStreams.fromPublisher(
                PagingRx.cachedIn(flow, ViewModelKt.getViewModelScope(this)));
        // Subscribe the favourite live data to the data emitted by the repository
        favouriteUUIDs = LiveDataReactiveStreams.fromPublisher(repository.observeFavouriteUUIDs());
        favouriteStations = LiveDataReactiveStreams.fromPublisher(repository.observeFavourites());
    }
    /**
     * Sets the search query, changing the list returned by {@link #getRadioStations()}.
     *
     * @param query The text to search for; an empty or {@code null} value removes the
     *              search and shows the plain list of stations
     */
    public void setQuery(String query) {
        this.query.onNext(query == null ? "" : query);
    }
    /**
     * Gives access to the paged list of stations.
     *
     * @return A {@link LiveData} of the {@link PagingData} to show in the list
     */
    public LiveData<PagingData<RadioStation>> getRadioStations() {
        return radioStations;
    }
    /**
     * Saves the country whose stations must be shown.
     *
     * @param country the country code to save
     * @return A {@link LiveData} that will receive a {@link Result.Success} when the
     * preference has been saved, or a {@link Result.Failure} if saving failed
     */
    public LiveData<Result> saveCountryPreference(String country) {
        return this.repository.saveCountryPreference(country);
    }
    /**
     * Reads the saved country preference.
     *
     * @return A {@link LiveData} that will receive a {@link Result.Success} with the
     * country code, or a {@link Result.Failure} if it could not be read
     */
    public LiveData<Result> getCountryPreference() {
        return this.repository.getCountryPreference();
    }
    /**
     * Gives access to the identifiers of the favourite stations.
     *
     * @return A {@link LiveData} of the set of favourite station UUIDs, updated every time
     * it changes
     */
    public LiveData<Set<UUID>> getFavouriteUUIDs() {
        return favouriteUUIDs;
    }
    /**
     * Gives access to the full list of favourite stations.
     *
     * @return A {@link LiveData} of the list of favourites, updated every time it changes
     */
    public LiveData<List<FavouriteStation>> getFavouriteStations() {
        return favouriteStations;
    }
    /**
     * Changes a station favourite status. The outcome is published on the
     * {@link LiveData} returned by {@link #getSavingResultLiveData()}.
     *
     * @param station   The station to add or remove
     * @param favourite {@code true} to add the station to the favourites, {@code false} to
     *                  remove it
     */
    public void setFavourite(BaseRadioStation station, boolean favourite) {
        if (favourite) repository.addFavourite(station);
        else repository.removeFavourite(station);
    }
    /**
     * Gives access to the outcome of adding or removing a favourite.
     *
     * @return A {@link LiveData} with a {@link Result.Success} containing the modified
     * station, or a {@link Result.Failure} if the operation failed
     */
    public LiveData<Result> getSavingResultLiveData() {
        return repository.getSavingResultLiveData();
    }
    /**
     * Starts the request for the list of the available country codes.
     *
     * @return A {@link LiveData} that will receive a {@link Result.Success} with the list of
     * country codes, or a {@link Result.Failure} if the request failed
     */
    public LiveData<Result> fetchCountryCodes() {
        return this.repository.fetchCountryCodes();
    }
    /**
     * Called when the ViewModel is no longer used. Notifies the repository so that it can
     * release its resources.
     */
    @Override
    public void onCleared() {
        repository.onDestroy();
    }
}
