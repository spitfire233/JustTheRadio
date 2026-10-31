package com.justtheradio.repository;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.paging.PagingData;

import com.justtheradio.model.Result;
import com.justtheradio.model.radio.FavouriteStation;
import com.justtheradio.model.radio.RadioStation;
import com.justtheradio.paging.RadioRemoteMediator;
import com.justtheradio.source.BaseRadioBrowserRemoteDataSource;
import com.justtheradio.source.BaseRadioLocalDataSource;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.subscribers.TestSubscriber;

/**
 * Unit tests for {@link RadioRepository}.
 * The test mock the remote, local and mediator sources to prevent testing them
 * in this class.
 */
public class RadioRepositoryUnitTest {
    /**
     * Makes LiveData work synchronously on the test thread
     */
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();
    /**
     * The mocked remote data source
     */
    private BaseRadioBrowserRemoteDataSource remote;
    /**
     * The mocked local data source
     */
    private BaseRadioLocalDataSource local;
    /**
     * The repository under test
     */
    private RadioRepository repository;
    /**
     * Mocks the data sources and creates the repository before every test
     */
    @Before
    public void setUp() {
        remote = mock(BaseRadioBrowserRemoteDataSource.class);
        local = mock(BaseRadioLocalDataSource.class);
        repository = new RadioRepository(remote, local, mock(RadioRemoteMediator.class));
    }
    /**
     * Creates a {@link FavouriteStation} with a random UUID and the given name
     *
     * @param name The station name
     * @return The station
     */
    private FavouriteStation createFavourite(String name) {
        FavouriteStation station = new FavouriteStation();
        station.setUUID(UUID.randomUUID());
        station.setName(name);
        return station;
    }
    /**
     * The repository must register itself as the callback of both data sources, otherwise it
     * would never be notified of the outcome of the asynchronous operations
     */
    @Test
    public void constructor_registersItselfAsCallbackOfBothSources() {
        verify(remote).setCallback(repository);
        verify(local).setCallback(repository);
    }
    /**
     * The browse list must be exposed as a Flowable
     */
    @Test
    public void getRadioStations_returnsFlowable() {
        Flowable<?> flowable = repository.getRadioStations();

        assertNotNull(flowable);
    }
    /**
     * The search results must be exposed as a Flowable
     */
    @Test
    public void searchStations_returnsFlowable() {
        Flowable<?> flowable = repository.searchStations("jazz");

        assertNotNull(flowable);
    }
    /**
     * The favourite UUIDs stream must be the one of the local source, unchanged
     */
    @Test
    public void observeFavouriteUUIDs_returnsLocalStream() {
        Flowable<Set<UUID>> expected = Flowable.just(Collections.singleton(UUID.randomUUID()));
        when(local.observeFavouriteUUIDs()).thenReturn(expected);

        assertSame(expected, repository.observeFavouriteUUIDs());
    }
    /**
     * The favourites stream must be the one of the local source, unchanged
     */
    @Test
    public void observeFavourites_returnsLocalStream() {
        Flowable<List<FavouriteStation>> expected =
                Flowable.just(Collections.singletonList(createFavourite("Radio One")));
        when(local.observeFavourites()).thenReturn(expected);

        assertSame(expected, repository.observeFavourites());
    }
    /**
     * Adding a favourite must be forwarded to the local source with the same station
     */
    @Test
    public void addFavourite_delegatesToLocalSource() {
        FavouriteStation station = createFavourite("Radio One");

        repository.addFavourite(station);

        verify(local).addFavourite(station);
    }

    /**
     * Removing a favourite must be forwarded to the local source with the same station
     */
    @Test
    public void removeFavourite_delegatesToLocalSource() {
        FavouriteStation station = createFavourite("Radio One");

        repository.removeFavourite(station);

        verify(local).removeFavourite(station);
    }

    /**
     * Fetching the country codes must start the request on the remote source and return the
     * LiveData that will receive the outcome
     */
    @Test
    public void fetchCountryCodes_startsRemoteRequestAndReturnsLiveData() {
        LiveData<Result> liveData = repository.fetchCountryCodes();

        verify(remote).getCountryCodes();
        assertNotNull(liveData);
    }
    /**
     * Calling the method twice must start two requests but always return the same LiveData, so
     * observers keep watching the same object
     */
    @Test
    public void fetchCountryCodes_calledTwice_reusesLiveData() {
        LiveData<Result> first = repository.fetchCountryCodes();
        LiveData<Result> second = repository.fetchCountryCodes();

        verify(remote, times(2)).getCountryCodes();
        assertSame(first, second);
    }
    /**
     * Saving the country must forward the code to the local source and return the LiveData that
     * will receive the outcome
     */
    @Test
    public void saveCountryPreference_delegatesToLocalSource() {
        LiveData<Result> liveData = repository.saveCountryPreference("IT");

        verify(local).saveCountryPreference("IT");
        assertNotNull(liveData);
    }
    /**
     * Reading the country preference must start an asynchronous read on the local source and
     * return the LiveData that will receive the outcome
     */
    @Test
    public void getCountryPreference_startsAsyncReadOnLocalSource() {
        LiveData<Result> liveData = repository.getCountryPreference();

        verify(local).getCountryPreferenceAsync();
        assertNotNull(liveData);
    }
    /**
     * Clearing the repository must tell the local source to release its resources
     */
    @Test
    public void clear_destroysLocalSource() {
        repository.clear();

        verify(local).onDestroy();
    }
    /**
     * A successful fetch of the country codes must publish a {@link Result.Success}
     */
    @Test
    public void onCountryCodesFetchSuccess_publishesSuccess() {
        LiveData<Result> liveData = repository.fetchCountryCodes();

        repository.onCountryCodesFetchSuccess(Arrays.asList("IT", "FR", "DE"));

        assertTrue(liveData.getValue() instanceof Result.Success);
    }
    /**
     * A failed fetch of the country codes must publish a {@link Result.Failure}
     */
    @Test
    public void onFailureFetchingCountryCodes_publishesFailure() {
        LiveData<Result> liveData = repository.fetchCountryCodes();

        repository.onFailureFetchingCountryCodes(new Exception("network error"));

        assertTrue(liveData.getValue() instanceof Result.Failure);
    }
    /**
     * A successful save of the country preference must publish a {@link Result.Success}
     */
    @Test
    public void onSavingCountryPreferenceSuccess_publishesSuccess() {
        LiveData<Result> liveData = repository.saveCountryPreference("IT");

        repository.onSavingCountryPreferenceSuccess();

        assertTrue(liveData.getValue() instanceof Result.Success);
    }
    /**
     * A failed save of the country preference must publish a {@link Result.Failure}
     */
    @Test
    public void onFailureSavingCountryPreference_publishesFailure() {
        LiveData<Result> liveData = repository.saveCountryPreference("IT");

        repository.onFailureSavingCountryPreference(new Exception("disk error"));

        assertTrue(liveData.getValue() instanceof Result.Failure);
    }
    /**
     * A successful read of the country preference must publish a {@link Result.Success}
     */
    @Test
    public void onGettingCountryPreferenceSuccess_publishesSuccess() {
        LiveData<Result> liveData = repository.getCountryPreference();

        repository.onGettingCountryPreferenceSuccess("IT");

        assertTrue(liveData.getValue() instanceof Result.Success);
    }
    /**
     * A failed read of the country preference must publish a {@link Result.Failure}
     */
    @Test
    public void onFailureGettingCountryPreference_publishesFailure() {
        LiveData<Result> liveData = repository.getCountryPreference();

        repository.onFailureGettingCountryPreference(new Exception("disk error"));

        assertTrue(liveData.getValue() instanceof Result.Failure);
    }
    /**
     * A successful change of the favourite state must publish a {@link Result.Success}
     */
    @Test
    public void onRadioStationModifiedFavouriteStateSuccess_publishesSuccess() {
        LiveData<Result> liveData = repository.getSavingResultLiveData();

        repository.onRadioStationModifiedFavouriteStateSuccess(createFavourite("Radio One"));

        assertTrue(liveData.getValue() instanceof Result.Success);
    }
    /**
     * A failed change of the favourite state must publish a {@link Result.Failure}
     */
    @Test
    public void onFailureModifyingStationSavedState_publishesFailure() {
        LiveData<Result> liveData = repository.getSavingResultLiveData();

        repository.onFailureModifyingStationSavedState(new Exception("database error"));

        assertTrue(liveData.getValue() instanceof Result.Failure);
    }
    /**
     * The saving result LiveData must always be the same instance
     */
    @Test
    public void getSavingResultLiveData_returnsSameInstance() {
        assertSame(repository.getSavingResultLiveData(), repository.getSavingResultLiveData());
    }
    /**
     * Each operation has its own LiveData: a result published for one operation must not show up
     * in the LiveData of the others
     */
    @Test
    public void callbacks_publishOnlyOnTheirOwnLiveData() {
        LiveData<Result> countryCodes = repository.fetchCountryCodes();
        LiveData<Result> countrySaving = repository.saveCountryPreference("IT");
        LiveData<Result> countryRetrieve = repository.getCountryPreference();
        LiveData<Result> favouriteSaving = repository.getSavingResultLiveData();

        repository.onCountryCodesFetchSuccess(Collections.singletonList("IT"));

        assertTrue(countryCodes.getValue() instanceof Result.Success);
        assertNull(countrySaving.getValue());
        assertNull(countryRetrieve.getValue());
        assertNull(favouriteSaving.getValue());
    }
    /**
     * A later result must replace the earlier one on the same LiveData: here a failure after a
     * success, as when a retry goes wrong
     */
    @Test
    public void callbacks_laterResultReplacesEarlierOne() {
        LiveData<Result> liveData = repository.getSavingResultLiveData();
        repository.onRadioStationModifiedFavouriteStateSuccess(createFavourite("Radio One"));
        assertTrue(liveData.getValue() instanceof Result.Success);

        repository.onFailureModifyingStationSavedState(new Exception("database error"));

        assertTrue(liveData.getValue() instanceof Result.Failure);
    }
    /**
     * Collecting the search flow must make the pager create its paging source.
     * The first PagingData is emitted only after the paging
     * source has been created, so receiving it proves the factory was invoked.
     */
    @Test(timeout = 10000)
    public void searchStations_whenCollected_createsPagingSource() {
        TestSubscriber<PagingData<RadioStation>> subscriber =
                repository.searchStations("jazz").test();
        try {
            // Blocks (up to 5 seconds) until the first PagingData is emitted
            subscriber.awaitCount(1);

            assertNotNull(subscriber.values().get(0));
        } finally {
            // Stop the pager, so nothing keeps running after the test
            subscriber.cancel();
        }
    }
}
