package com.justtheradio.repository;


import static com.justtheradio.utils.constant.RadioBrowserConstants.RADIO_BROWSER_INITIAL_LOADING_SIZE;
import static com.justtheradio.utils.constant.RadioBrowserConstants.RADIO_BROWSER_PAGING_LIMIT;
import static com.justtheradio.utils.constant.RadioBrowserConstants.RADIO_BROWSER_PREFETCH_DISTANCE;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.rxjava3.PagingRx;

import com.justtheradio.model.Result;
import com.justtheradio.model.radio.BaseRadioStation;
import com.justtheradio.model.radio.FavouriteStation;
import com.justtheradio.model.radio.RadioStation;
import com.justtheradio.paging.RadioSearchPagingSource;
import com.justtheradio.source.BaseRadioBrowserRemoteDataSource;
import com.justtheradio.source.BaseRadioLocalDataSource;
import com.justtheradio.source.callback.RadioCallback;
import com.justtheradio.paging.RadioRemoteMediator;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import io.reactivex.rxjava3.core.Flowable;

/**
 * Repository for the radio data retrieved and cached by the application. This class gives
 * a single access point to retrieve and save the stations' data.
 */
public class RadioRepository implements RadioCallback {
    /**
     * Remote source that queries the Radio Browser API
     */
    private final BaseRadioBrowserRemoteDataSource remote;
    /**
     * Local source backed that interacts with the Room database
     */
    private final BaseRadioLocalDataSource local;
    /**
     * Live data that holds the outcome of fetching the countries list
     */
    private final MutableLiveData<Result> countryCodesMutableLiveData;
    /**
     * Live data that holds the outcome of retrieving the country preference
     */
    private final MutableLiveData<Result> countryRetrieveMutableLiveData;
    /**
     * Live data that holds the outcome of saving the country preference
     */
    private final MutableLiveData<Result> countrySavingMutableLiveData;
    /**
     * Live data that holds the outcome of changing the favourite state of a station
     */
    private final MutableLiveData<Result> savingMutableLiveData;
    /**
     * {@link androidx.paging.rxjava3.RxRemoteMediator Mediator}
     * that downloads stations from the API into the database
     */
    private final RadioRemoteMediator radioRemoteMediator;
    /**
     * Paging config that holds the settings for paging content.
     */
    private final PagingConfig pagingConfig;

    /**
     * Constructor for the {@link RadioRepository} class
     * @param remote The remote data source that contacts the Radio Browser API
     * @param local The local data source that interacts with the cache and Room database
     * @param radioRemoteMediator The mediator to use for pagination of the retrieved results
     */
    public RadioRepository(BaseRadioBrowserRemoteDataSource remote,
                           BaseRadioLocalDataSource local,
                           RadioRemoteMediator radioRemoteMediator) {
        // Initialize data sources
        this.remote = remote;
        this.local = local;

        // Register callback to this object, so that the repository is notified of the result
        // of the asynchronous operations
        this.remote.setCallback(this);
        this.local.setCallback(this);

        // Initialize live data
        countryCodesMutableLiveData = new MutableLiveData<>();
        countryRetrieveMutableLiveData = new MutableLiveData<>();
        countrySavingMutableLiveData = new MutableLiveData<>();
        savingMutableLiveData = new MutableLiveData<>();

        // Initialize remote mediator
        this.radioRemoteMediator = radioRemoteMediator;

        // Create paging config to use when paging content
        pagingConfig = new PagingConfig(
                RADIO_BROWSER_PAGING_LIMIT,
                RADIO_BROWSER_PREFETCH_DISTANCE,
                true, // Enable placeholders
                RADIO_BROWSER_INITIAL_LOADING_SIZE
        );
    }
    /**
     * Returns the paged list of the stations of the preferred country.
     * The list is read from the database, which is the single source of truth, while the
     * {@link RadioRemoteMediator} downloads more stations from the API when the cached ones
     * run out or have expired.
     *
     * @return A {@link Flowable} emitting the {@link PagingData} to show in the list
     */
    public Flowable<PagingData<RadioStation>> getRadioStations() {
        // Create a pager object that coordinates the remote mediator and the local database
        Pager<Integer, RadioStation> pager = new Pager<>(
                pagingConfig,
                null,
                radioRemoteMediator,
                local::getRadioDAOPagingSource
        );
        return PagingRx.getFlowable(pager);
    }
    /**
     * Returns the paged search results for the given query.
     * Unlike {@link #getRadioStations()}, the results are not cached.
     *
     * @param query The search query
     * @return A {@link Flowable} emitting the {@link PagingData} with the matching stations
     */
    public Flowable<PagingData<RadioStation>> searchStations(String query) {
        // Create a pager object that retrieves the data from the paging source
        Pager<Integer, RadioStation> pager = new Pager<>(
                pagingConfig,
                () -> new RadioSearchPagingSource(remote, local, query)
        );
        return PagingRx.getFlowable(pager);
    }
    /**
     * Observes the identifiers of the stations marked as favourite
     *
     * @return A {@link Flowable} emitting the set of favourite station UUIDs every time it changes
     */
    public Flowable<Set<UUID>> observeFavouriteUUIDs() {
        return local.observeFavouriteUUIDs();
    }
    /**
     * Observes the stations marked as favourite
     * @return A {@link Flowable} emitting the list of favourite stations every time it changes
     */
    public Flowable<List<FavouriteStation>> observeFavourites() {
        return local.observeFavourites();
    }
    /**
     * Adds a station to the favourites.
     * The outcome is published on the {@link LiveData} returned by
     * {@link #getSavingResultLiveData()}.
     *
     * @param station The station to mark as favourite
     */
    public void addFavourite(BaseRadioStation station) {
        local.addFavourite(station);
    }
    /**
     * Removes a station from the favourites.
     * The outcome is published on the {@link LiveData} returned by
     * {@link #getSavingResultLiveData()}.
     *
     * @param station The station to remove from the favourites
     */
    public void removeFavourite(BaseRadioStation station) {
        local.removeFavourite(station);
    }
    /**
     * Gives access to the outcome of adding or removing a favourite.
     *
     * @return A {@link LiveData} containing the {@link Result} of the operation
     */
    public LiveData<Result> getSavingResultLiveData() {
        return savingMutableLiveData;
    }
    /**
     * Starts the request for the list of the available country codes.
     *
     * @return A {@link LiveData} that will receive the outcome of the operation
     */
    public LiveData<Result> fetchCountryCodes() {
        remote.getCountryCodes();
        return countryCodesMutableLiveData;
    }
    /**
     * Saves the country whose stations must be shown.
     *
     * @param country The country code to save
     * @return A {@link LiveData} that will receive the outcome of the operation
     */
    public LiveData<Result> saveCountryPreference(String country) {
        local.saveCountryPreference(country);
        return countrySavingMutableLiveData;
    }
    /**
     * Reads the saved country preference.
     *
     * @return A {@link LiveData} that will receive the outcome of the operation
     */
    public LiveData<Result> getCountryPreference() {
        local.getCountryPreferenceAsync();
        return countryRetrieveMutableLiveData;
    }
    /**
     * Implementation of {@link RadioCallback#onCountryCodesFetchSuccess(List)}.
     * Publishes the list of country codes that has been fetched successfully
     * @param countryCodes The list of country codes to publish
     */
    @Override
    public void onCountryCodesFetchSuccess(List<String> countryCodes) {
        Result.Success<List<String>> result = new Result.Success<>(countryCodes);
        countryCodesMutableLiveData.postValue(result);
    }
    /**
     * Implementation of {@link RadioCallback#onFailureFetchingCountryCodes(Exception)}
     * Publishes the error obtained while fetching the countries list from the API
     * @param exception The exception that occurred
     */
    @Override
    public void onFailureFetchingCountryCodes(Exception exception) {
        Result.Failure result = new Result.Failure(exception.getMessage());
        countryCodesMutableLiveData.postValue(result);
    }
    /**
     * Implementation of {@link RadioCallback#onSavingCountryPreferenceSuccess()}.
     * Publishes a successful result for the country code saving operation
     */
    @Override
    public void onSavingCountryPreferenceSuccess() {
        Result.Success<Void> result = new Result.Success<>();
        countrySavingMutableLiveData.postValue(result);
    }
    /**
     * Implementation of {@link RadioCallback#onGettingCountryPreferenceSuccess(String)}.
     * Publishes the country code saved by the user in cache
     * @param country The country code retreived from cache
     */
    @Override
    public void onGettingCountryPreferenceSuccess(String country) {
        Result.Success<String> result = new Result.Success<>(country);
        countryRetrieveMutableLiveData.postValue(result);
    }
    /**
     * Implementation of {@link RadioCallback#onRadioStationModifiedFavouriteStateSuccess(BaseRadioStation)}
     * Publishes the result of modifying the favourite status of a station
     * @param station The station whose favourite status has been modified
     */
    @Override
    public void onRadioStationModifiedFavouriteStateSuccess(BaseRadioStation station) {
        Result.Success<BaseRadioStation> result = new Result.Success<>(station);
        savingMutableLiveData.postValue(result);
    }
    /**
     * Implementation of {@link RadioCallback#onFailureSavingCountryPreference(Exception)}.
     * Publishes the error that occurred when saving a country in cache
     * @param exception The exception occurred while saving the country in cache
     */
    @Override
    public void onFailureSavingCountryPreference(Exception exception) {
        Result.Failure result = new Result.Failure(exception.getMessage());
        countrySavingMutableLiveData.postValue(result);
    }
    /**
     * Implementation of {@link RadioCallback#onFailureGettingCountryPreference(Exception)}.
     * Publishes the error occurred while reading the country preference from cache
     * @param exception The exception occurred while reading the country from cache
     */
    @Override
    public void onFailureGettingCountryPreference(Exception exception) {
        Result.Failure result = new Result.Failure(exception.getMessage());
        countryRetrieveMutableLiveData.postValue(result);
    }
    /**
     * Implementation of {@link RadioCallback#onFailureModifyingStationSavedState(Exception)}.
     * Publishes the error occurred while modifying the favourite status of a radio station
     * @param exception The exception occurred while modifying the favourite status
     */
    @Override
    public void onFailureModifyingStationSavedState(Exception exception) {
        Result.Failure result = new Result.Failure(exception.getMessage());
        savingMutableLiveData.postValue(result);
    }
    /**
     * Called when the holder of this class gets destroyed. It triggers the cleanup of the disposables
     * of the local data source to prevent memory leaks
     */
    public void clear() {
        local.onDestroy();
    }
}