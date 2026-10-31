package com.justtheradio.ui.viewmodel.radio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.paging.PagingData;

import com.justtheradio.model.Result;
import com.justtheradio.model.radio.FavouriteStation;
import com.justtheradio.repository.RadioRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.BehaviorProcessor;

/**
 * Unit tests for {@link RadioViewModel}.
 * The {@link RadioRepository} is mocked to test only the view model and
 * not the repository
 */
@RunWith(RobolectricTestRunner.class)
public class RadioViewModelUnitTest {
    /**
     * Maximum time, in milliseconds, to wait for an asynchronous assertion to pass
     */
    private static final long TIMEOUT_MS = 5000;
    /**
     * The mocked repository the view model reads from
     */
    private RadioRepository repository;
    /**
     * Source of the favourite UUIDs: tests push values into it to simulate database changes
     */
    private BehaviorProcessor<Set<UUID>> favouriteUuidProcessor;
    /**
     * Source of the favourite stations: tests push values into it to simulate database changes
     */
    private BehaviorProcessor<List<FavouriteStation>> favouriteStationProcessor;
    /**
     * The view model under test
     */
    private RadioViewModel viewModel;
    /**
     * Actions that detach the observers attached during a test
     */
    private final List<Runnable> cleanups = new ArrayList<>();
    /**
     * Mocks the repository and creates the view model before every test.
     */
    @Before
    public void setUp() {
        repository = mock(RadioRepository.class);
        favouriteUuidProcessor = BehaviorProcessor.create();
        favouriteStationProcessor = BehaviorProcessor.create();

        when(repository.observeFavouriteUUIDs()).thenReturn(favouriteUuidProcessor);
        when(repository.observeFavourites()).thenReturn(favouriteStationProcessor);
        when(repository.getRadioStations())
                .thenReturn(Flowable.just(PagingData.empty()));
        when(repository.searchStations("jazz"))
                .thenReturn(Flowable.just(PagingData.empty()));

        viewModel = new RadioViewModel(repository);
    }
    /**
     * Detaches every observer, which unsubscribes the LiveData from the repository streams
     */
    @After
    public void tearDown() {
        for (Runnable cleanup : cleanups) {
            cleanup.run();
        }
        cleanups.clear();
    }
    /**
     * Attaches a no-op observer to the given LiveData, so that it becomes active and subscribes
     * to its source. The observer is removed automatically in {@link #tearDown()}.
     *
     * @param liveData The LiveData to activate
     * @param <T>      The type held by the LiveData
     */
    private <T> void activate(LiveData<T> liveData) {
        Observer<T> observer = value -> { };
        liveData.observeForever(observer);
        cleanups.add(() -> liveData.removeObserver(observer));
    }
    /**
     * Runs the given assertion repeatedly, processing the main looper between attempts, until it
     * passes or the timeout expires. In the second case the last failure is rethrown.
     *
     * @param assertion The assertion (or Mockito verification) to retry
     */
    private void eventually(Runnable assertion) {
        long deadline = System.currentTimeMillis() + TIMEOUT_MS;
        AssertionError lastFailure = null;
        while (System.currentTimeMillis() < deadline) {
            shadowOf(Looper.getMainLooper()).idle();
            try {
                assertion.run();
                return;
            } catch (AssertionError e) {
                lastFailure = e;
            }
            sleep();
        }
        if (lastFailure != null) {
            throw lastFailure;
        }
        fail("Timed out");
    }
    /**
     * Lets pending asynchronous work run for a short while. Used before asserting that something
     * did NOT happen, since {@link #eventually(Runnable)} would return as soon as it holds.
     */
    private void settle() {
        for (int i = 0; i < 10; i++) {
            shadowOf(Looper.getMainLooper()).idle();
            sleep();
        }
    }
    /**
     * Sleeps for the given time, restoring the interrupt flag if interrupted
     */
    private void sleep() {
        try {
            Thread.sleep((long) 10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Interrupted");
        }
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
     * Every stream exposed by the view model must be non-null
     */
    @Test
    public void exposedLiveData_areNeverNull() {
        assertNotNull(viewModel.getRadioStations());
        assertNotNull(viewModel.getFavouriteUUIDs());
        assertNotNull(viewModel.getFavouriteStations());
    }
    /**
     * With no query set, the user is browsing: stations come from the repository's cached list
     * and no search is made
     */
    @Test
    public void radioStations_noQuery_usesBrowseSource() {
        activate(viewModel.getRadioStations());

        eventually(() -> verify(repository).getRadioStations());
        verify(repository, never()).searchStations("jazz");
    }
    /**
     * Setting a non-empty query must switch to the repository's search source with that query
     */
    @Test
    public void setQuery_nonEmpty_switchesToSearch() {
        activate(viewModel.getRadioStations());
        eventually(() -> verify(repository).getRadioStations());

        viewModel.setQuery("jazz");

        eventually(() -> verify(repository).searchStations("jazz"));
    }
    /**
     * Setting the same query twice must search only once: the query stream drops repeated values
     */
    @Test
    public void setQuery_sameQueryTwice_searchesOnce() {
        activate(viewModel.getRadioStations());
        eventually(() -> verify(repository).getRadioStations());

        viewModel.setQuery("jazz");
        viewModel.setQuery("jazz");

        eventually(() -> verify(repository).searchStations("jazz"));
        settle();
        verify(repository, times(1)).searchStations("jazz");
    }
    /**
     * Clearing the query with an empty string must go back to the browse source
     */
    @Test
    public void setQuery_empty_returnsToBrowse() {
        activate(viewModel.getRadioStations());
        eventually(() -> verify(repository).getRadioStations());
        viewModel.setQuery("jazz");
        eventually(() -> verify(repository).searchStations("jazz"));

        viewModel.setQuery("");

        eventually(() -> verify(repository, times(2)).getRadioStations());
    }
    /**
     * A null query must be treated like an empty one: back to browsing, with no search for null
     */
    @Test
    public void setQuery_null_returnsToBrowse() {
        activate(viewModel.getRadioStations());
        eventually(() -> verify(repository).getRadioStations());
        viewModel.setQuery("jazz");
        eventually(() -> verify(repository).searchStations("jazz"));

        viewModel.setQuery(null);

        eventually(() -> verify(repository, times(2)).getRadioStations());
        verify(repository, never()).searchStations(null);
    }
    /**
     * Setting an empty query when it is already empty must not reload the browse list
     */
    @Test
    public void setQuery_emptyWhileAlreadyEmpty_doesNotReload() {
        activate(viewModel.getRadioStations());
        eventually(() -> verify(repository).getRadioStations());

        viewModel.setQuery("");
        settle();

        verify(repository, times(1)).getRadioStations();
    }
    /**
     * The favourite UUIDs must follow the repository stream, including later updates
     */
    @Test
    public void favouriteUUIDs_followsRepositoryStream() {
        activate(viewModel.getFavouriteUUIDs());
        Set<UUID> first = new HashSet<>(Collections.singletonList(UUID.randomUUID()));
        Set<UUID> second = new HashSet<>(Arrays.asList(UUID.randomUUID(), UUID.randomUUID()));

        favouriteUuidProcessor.onNext(first);
        eventually(() -> assertEquals(first, viewModel.getFavouriteUUIDs().getValue()));

        favouriteUuidProcessor.onNext(second);
        eventually(() -> assertEquals(second, viewModel.getFavouriteUUIDs().getValue()));
    }
    /**
     * The favourite stations must follow the repository stream, including later updates
     */
    @Test
    public void favouriteStations_followsRepositoryStream() {
        activate(viewModel.getFavouriteStations());
        List<FavouriteStation> first = Collections.singletonList(createFavourite("Radio One"));
        List<FavouriteStation> second =
                Arrays.asList(createFavourite("Radio One"), createFavourite("Radio Two"));

        favouriteStationProcessor.onNext(first);
        eventually(() -> assertSame(first, viewModel.getFavouriteStations().getValue()));

        favouriteStationProcessor.onNext(second);
        eventually(() -> assertSame(second, viewModel.getFavouriteStations().getValue()));
    }
    /**
     * {@code setFavourite(station, true)} must add the station and must not remove anything
     */
    @Test
    public void setFavourite_true_addsFavourite() {
        FavouriteStation station = createFavourite("Radio One");

        viewModel.setFavourite(station, true);

        verify(repository).addFavourite(station);
        verify(repository, never()).removeFavourite(station);
    }
    /**
     * {@code setFavourite(station, false)} must remove the station and must not add anything
     */
    @Test
    public void setFavourite_false_removesFavourite() {
        FavouriteStation station = createFavourite("Radio One");

        viewModel.setFavourite(station, false);

        verify(repository).removeFavourite(station);
        verify(repository, never()).addFavourite(station);
    }
    /**
     * Saving the country must forward the code and return the repository's LiveData as is
     */
    @Test
    public void saveCountryPreference_delegatesToRepository() {
        MutableLiveData<Result> expected = new MutableLiveData<>();
        when(repository.saveCountryPreference("IT")).thenReturn(expected);

        LiveData<Result> actual = viewModel.saveCountryPreference("IT");

        assertSame(expected, actual);
        verify(repository).saveCountryPreference("IT");
    }
    /**
     * Reading the country preference must return the repository's LiveData as is
     */
    @Test
    public void getCountryPreference_delegatesToRepository() {
        MutableLiveData<Result> expected = new MutableLiveData<>();
        when(repository.getCountryPreference()).thenReturn(expected);

        assertSame(expected, viewModel.getCountryPreference());
    }
    /**
     * Fetching the country codes must return the repository's LiveData as is
     */
    @Test
    public void fetchCountryCodes_delegatesToRepository() {
        MutableLiveData<Result> expected = new MutableLiveData<>();
        when(repository.fetchCountryCodes()).thenReturn(expected);

        assertSame(expected, viewModel.fetchCountryCodes());
    }
    /**
     * The outcome of the favourite operations must be the repository's LiveData as is
     */
    @Test
    public void getSavingResultLiveData_delegatesToRepository() {
        MutableLiveData<Result> expected = new MutableLiveData<>();
        when(repository.getSavingResultLiveData()).thenReturn(expected);

        assertSame(expected, viewModel.getSavingResultLiveData());
    }
    /**
     * Clearing the view model must tell the repository to release its resources
     */
    @Test
    public void clear_notifiesRepository() {
        viewModel.clear();

        verify(repository).clear();
    }
}
