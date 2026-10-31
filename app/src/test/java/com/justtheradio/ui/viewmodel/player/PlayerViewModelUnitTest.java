package com.justtheradio.ui.viewmodel.player;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.justtheradio.model.radio.BaseRadioStation;
import com.justtheradio.model.radio.FavouriteStation;
import com.justtheradio.model.radio.RadioStation;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Unit tests for {@link PlayerViewModel}.
 */
public class PlayerViewModelUnitTest {
    /**
     * Makes LiveData work synchronously on the test thread
     */
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();
    /**
     * The view model under test
     */
    private PlayerViewModel viewModel;
    /**
     * Stations received by the observer attached in {@link #observe()}, in arrival order
     */
    private List<BaseRadioStation> received;
    /**
     * The observer attached to the selected station, or null if none is attached
     */
    private Observer<BaseRadioStation> observer;
    /**
     * Creates a fresh view model before every test, so tests never share state
     */
    @Before
    public void setUp() {
        viewModel = new PlayerViewModel();
        received = new ArrayList<>();
    }
    /**
     * Detaches the observer, if a test attached one
     */
    @After
    public void tearDown() {
        if (observer != null) {
            viewModel.getSelectedRadioStation().removeObserver(observer);
        }
    }
    /**
     * Attaches an observer that records every value it receives into {@link #received}
     */
    private void observe() {
        observer = received::add;
        viewModel.getSelectedRadioStation().observeForever(observer);
    }
    /**
     * Creates a {@link RadioStation} with a random UUID and the given name
     *
     * @param name The station name
     * @return The station
     */
    private RadioStation createRadioStation(String name) {
        RadioStation station = new RadioStation();
        station.setUUID(UUID.randomUUID());
        station.setName(name);
        return station;
    }
    /**
     * Creates a {@link FavouriteStation} with a random UUID and the given name
     *
     * @return The station
     */
    private FavouriteStation createFavouriteStation() {
        FavouriteStation station = new FavouriteStation();
        station.setUUID(UUID.randomUUID());
        station.setName("Favourite");
        return station;
    }
    /**
     * The LiveData must exist from the start
     */
    @Test
    public void getSelectedRadioStation_isNeverNull() {
        assertNotNull(viewModel.getSelectedRadioStation());
    }
    /**
     * Until the user selects a station, the LiveData must have no value
     */
    @Test
    public void initialState_hasNoSelectedStation() {
        assertNull(viewModel.getSelectedRadioStation().getValue());
    }
    /**
     * A new observer must not be notified before a station is selected: a LiveData with no value
     * emits nothing
     */
    @Test
    public void initialState_observerReceivesNothing() {
        observe();

        assertEquals(0, received.size());
    }
    /**
     * The getter must always return the same LiveData instance, otherwise observers attached at
     * different times would watch different objects
     */
    @Test
    public void getSelectedRadioStation_returnsSameInstance() {
        assertSame(viewModel.getSelectedRadioStation(), viewModel.getSelectedRadioStation());
    }
    /**
     * After selecting a station, the LiveData must hold exactly that station
     */
    @Test
    public void setRadioStation_updatesValue() {
        RadioStation station = createRadioStation("Radio One");

        viewModel.setRadioStation(station);

        assertSame(station, viewModel.getSelectedRadioStation().getValue());
    }
    /**
     * An observer attached before the selection must be notified with the selected station
     */
    @Test
    public void setRadioStation_notifiesObserver() {
        RadioStation station = createRadioStation("Radio One");
        observe();

        viewModel.setRadioStation(station);

        assertEquals(1, received.size());
        assertSame(station, received.get(0));
    }
    /**
     * An observer attached after the selection must immediately receive the current station
     * (this is what happens when the activity is recreated and observes again)
     */
    @Test
    public void setRadioStation_lateObserverReceivesCurrentStation() {
        RadioStation station = createRadioStation("Radio One");
        viewModel.setRadioStation(station);

        observe();

        assertEquals(1, received.size());
        assertSame(station, received.get(0));
    }
    /**
     * Selecting a second station must replace the first one and notify the observer again
     */
    @Test
    public void setRadioStation_secondSelection_replacesFirst() {
        RadioStation first = createRadioStation("Radio One");
        RadioStation second = createRadioStation("Radio Two");
        observe();

        viewModel.setRadioStation(first);
        viewModel.setRadioStation(second);

        assertSame(second, viewModel.getSelectedRadioStation().getValue());
        assertEquals(2, received.size());
        assertSame(first, received.get(0));
        assertSame(second, received.get(1));
    }
    /**
     * Selecting the same station twice must notify the observer twice: LiveData does not
     * deduplicate values, and the player relies on a new selection to restart the playback
     */
    @Test
    public void setRadioStation_sameStationTwice_notifiesTwice() {
        RadioStation station = createRadioStation("Radio One");
        observe();

        viewModel.setRadioStation(station);
        viewModel.setRadioStation(station);

        assertEquals(2, received.size());
    }
    /**
     * The view model accepts any {@link BaseRadioStation}: both stations coming from the browse
     * list ({@link RadioStation}) and from the favourites ({@link FavouriteStation})
     */
    @Test
    public void setRadioStation_acceptsBothStationTypes() {
        RadioStation browsed = createRadioStation("Browsed");
        FavouriteStation favourite = createFavouriteStation();
        observe();

        viewModel.setRadioStation(browsed);
        viewModel.setRadioStation(favourite);

        assertEquals(2, received.size());
        assertSame(browsed, received.get(0));
        assertSame(favourite, received.get(1));
    }
    /**
     * Passing null is accepted and clears the selection. This documents the current behavior:
     * the method has no null check.
     */
    @Test
    public void setRadioStation_null_clearsSelection() {
        viewModel.setRadioStation(createRadioStation("Radio One"));

        viewModel.setRadioStation(null);

        assertNull(viewModel.getSelectedRadioStation().getValue());
    }
    /**
     * Two view model instances must not share state
     */
    @Test
    public void separateInstances_doNotShareState() {
        PlayerViewModel other = new PlayerViewModel();

        viewModel.setRadioStation(createRadioStation("Radio One"));

        assertNull(other.getSelectedRadioStation().getValue());
    }
}
