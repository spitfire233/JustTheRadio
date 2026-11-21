package com.justtheradio.source;

import static com.justtheradio.utils.constant.LocalCacheConstants.CHOSEN_COUNTRY_KEY;
import static com.justtheradio.utils.constant.LocalCacheConstants.LAST_TIME_UPDATED_KEY;
import static com.justtheradio.utils.constant.LocalCacheConstants.NO_KEY_WITH_THAT_NAME_IN_DATASTORE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Application;

import com.justtheradio.database.RadioDAO;
import com.justtheradio.database.RadioRoomDatabase;
import com.justtheradio.model.radio.BaseRadioStation;
import com.justtheradio.model.radio.FavouriteStation;
import com.justtheradio.model.radio.RadioRemoteKey;
import com.justtheradio.model.radio.RadioStation;
import com.justtheradio.source.callback.RadioCallback;
import com.justtheradio.utils.datastore.DataStoreManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import androidx.datastore.preferences.core.Preferences;
import androidx.paging.PagingSource;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subjects.SingleSubject;

/**
 * Unit tests for {@link RadioLocalDataSource}.
 * The DAO, the DataStore manager and the callback are mocked, and the two
 * static dependencies (DataStoreManager.getInstance and
 * RadioRoomDatabase.getDatabaseWriteExecutor) are mocked with {@code mockStatic}.
 */
public class RadioLocalDataSourceUnitTest {
    /** Mocked DAO, used to stub the database reads and to verify the writes. */
    private RadioDAO dao;
    /** Mocked DataStore manager, whose Single results drive the country preference paths. */
    private DataStoreManager datastore;
    /** Mocked callback, used to verify which outcome the data source reports. */
    private RadioCallback callback;
    /** Static mock replacing {@code DataStoreManager.getInstance}, closed after each test. */
    private MockedStatic<DataStoreManager> dataStoreStatic;
    /**
     * Static mock replacing {@code RadioRoomDatabase.getDatabaseWriteExecutor}, closed after
     * each test.
     */
    private MockedStatic<RadioRoomDatabase> databaseStatic;
    /** The data source under test, built on the mocked dependencies. */
    private RadioLocalDataSource source;

    /**
     * Builds the data source on mocked dependencies. The two static calls it makes in the
     * constructor and when writing are mocked too, and the write executor runs tasks on the
     * calling thread, so every assertion can follow the call directly
     */
    @Before
    public void setUp() {
        dao = mock(RadioDAO.class);
        datastore = mock(DataStoreManager.class);
        callback = mock(RadioCallback.class);

        Application application = mock(Application.class);
        RadioRoomDatabase database = mock(RadioRoomDatabase.class);
        when(database.radioDAO()).thenReturn(dao);

        // Executor that runs every task immediately on the calling thread.
        ExecutorService directExecutor = mock(ExecutorService.class);
        doAnswer(invocation -> {
            ((Runnable) invocation.getArgument(0)).run();
            return null;
        }).when(directExecutor).execute(any(Runnable.class));

        dataStoreStatic = Mockito.mockStatic(DataStoreManager.class);
        dataStoreStatic.when(() -> DataStoreManager.getInstance(application))
                .thenReturn(datastore);

        databaseStatic = Mockito.mockStatic(RadioRoomDatabase.class);
        databaseStatic.when(RadioRoomDatabase::getDatabaseWriteExecutor)
                .thenReturn(directExecutor);

        source = new RadioLocalDataSource(application, database);
        source.setCallback(callback);
    }
    /**
     * Static mocks are bound to the thread that opened them: they must be closed after each
     * test, or they leak into the following ones
     */
    @After
    public void tearDown() {
        // Static mocks are thread-local and must be closed, or they leak into other tests.
        dataStoreStatic.close();
        databaseStatic.close();
    }

    /**
     * A successful insert must notify the callback with the very same FavouriteStation that was
     * written to the DAO, not a copy or the original station
     */
    @Test
    public void addFavourite_success_insertsAndNotifiesWithSameObject() {
        BaseRadioStation station = mock(BaseRadioStation.class);
        when(station.getUUID()).thenReturn(UUID.randomUUID());

        source.addFavourite(station);

        ArgumentCaptor<FavouriteStation> inserted = ArgumentCaptor.forClass(FavouriteStation.class);
        verify(dao).insertFavourite(inserted.capture());

        ArgumentCaptor<BaseRadioStation> notified = ArgumentCaptor.forClass(BaseRadioStation.class);
        verify(callback).onRadioStationModifiedFavouriteStateSuccess(notified.capture());

        assertSame(inserted.getValue(), notified.getValue());
        verify(callback, never()).onFailureModifyingStationSavedState(any());
    }
    /**
     * An exception thrown by the DAO must reach the failure callback unchanged, and the success
     * callback must stay silent: here an insert that fails
     */
    @Test
    public void addFavourite_daoThrows_notifiesFailure() {
        BaseRadioStation station = mock(BaseRadioStation.class);
        RuntimeException error = new RuntimeException("insert failed");
        doThrow(error).when(dao).insertFavourite(any(FavouriteStation.class));

        source.addFavourite(station);

        verify(callback).onFailureModifyingStationSavedState(error);
        verify(callback, never()).onRadioStationModifiedFavouriteStateSuccess(any());
    }
    /**
     * Removing a favourite must delete by the station's UUID and then report the original
     * station back through the callback
     */
    @Test
    public void removeFavourite_success_deletesByUuidAndNotifies() {
        UUID uuid = UUID.randomUUID();
        BaseRadioStation station = mock(BaseRadioStation.class);
        when(station.getUUID()).thenReturn(uuid);

        source.removeFavourite(station);

        verify(dao).deleteFavourite(uuid);
        verify(callback).onRadioStationModifiedFavouriteStateSuccess(station);
        verify(callback, never()).onFailureModifyingStationSavedState(any());
    }
    /**
     * An exception thrown by the DAO must reach the failure callback unchanged, and the success
     * callback must stay silent
     */
    @Test
    public void removeFavourite_daoThrows_notifiesFailure() {
        UUID uuid = UUID.randomUUID();
        BaseRadioStation station = mock(BaseRadioStation.class);
        when(station.getUUID()).thenReturn(uuid);
        RuntimeException error = new RuntimeException("delete failed");
        doThrow(error).when(dao).deleteFavourite(uuid);

        source.removeFavourite(station);

        verify(callback).onFailureModifyingStationSavedState(error);
        verify(callback, never()).onRadioStationModifiedFavouriteStateSuccess(any());
    }
    /**
     * The DAO emits a list but callers need a set: repeated UUIDs must collapse into one, as
     * when the same station appears twice in the emitted list
     */
    @Test
    public void observeFavouriteUUIDs_mapsListToSet_collapsingDuplicates() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        List<UUID> withDuplicates = Arrays.asList(a, b, a);
        when(dao.observeFavouritesUUIDs()).thenReturn(Flowable.just(withDuplicates));

        Set<UUID> expected = new HashSet<>(Arrays.asList(a, b));

        source.observeFavouriteUUIDs()
                .test()
                .assertValue(expected)
                .assertNoErrors();
    }
    /**
     * Once the DataStore write completes the success callback must fire, and the failure one
     * must not
     */
    @Test
    public void saveCountryPreference_success_callsSuccessCallback() {
        Preferences saved = mock(Preferences.class);
        doReturn(Single.just(saved))
                .when(datastore).saveStringResource(CHOSEN_COUNTRY_KEY, "IT");

        source.saveCountryPreference("IT");

        verify(callback).onSavingCountryPreferenceSuccess();
        verify(callback, never()).onFailureSavingCountryPreference(any());
    }
    /**
     * A failed DataStore write must be reported through the failure callback, with the original
     * throwable kept as the cause of the exception
     */
    @Test
    public void saveCountryPreference_error_wrapsThrowableInFailureCallback() {
        RuntimeException error = new RuntimeException("disk full");
        doReturn(Single.error(error))
                .when(datastore).saveStringResource(CHOSEN_COUNTRY_KEY, "IT");

        source.saveCountryPreference("IT");

        ArgumentCaptor<Exception> captor = ArgumentCaptor.forClass(Exception.class);
        verify(callback).onFailureSavingCountryPreference(captor.capture());
        assertSame(error, captor.getValue().getCause());
        verify(callback, never()).onSavingCountryPreferenceSuccess();
    }
    /**
     * A missing key must be reported through the failure callback, keeping the
     * NoSuchElementException from the DataStore as the cause: here a first launch, before any
     * country has been saved
     */
    @Test
    public void getCountryPreferenceAsync_missingKey_wrapsNoSuchElementInFailureCallback() {
        NoSuchElementException missingKey =
                new NoSuchElementException(NO_KEY_WITH_THAT_NAME_IN_DATASTORE);
        when(datastore.getStringResource(CHOSEN_COUNTRY_KEY))
                .thenReturn(Single.<String>error(missingKey));

        source.getCountryPreferenceAsync();

        ArgumentCaptor<Exception> captor = ArgumentCaptor.forClass(Exception.class);
        verify(callback).onFailureGettingCountryPreference(captor.capture());
        assertSame(missingKey, captor.getValue().getCause());
        assertEquals(NO_KEY_WITH_THAT_NAME_IN_DATASTORE, Objects.requireNonNull(captor.getValue()
                .getCause()).getMessage());
        verify(callback, never()).onGettingCountryPreferenceSuccess(any());
    }
    /**
     * Destroying the data source must wipe the Room database through the DAO
     */
    @Test
    public void onDestroy_clearsDatabase() {
        source.onDestroy();

        verify(dao).clearDatabase();
    }
    /**
     * Pending DataStore subscriptions must be disposed on destroy, so that a late emission never
     * reaches the callback: here a read that completes after the data source is gone
     */
    @Test
    public void onDestroy_disposesPendingSubscriptions() {
        SingleSubject<String> pending = SingleSubject.create();
        when(datastore.getStringResource(CHOSEN_COUNTRY_KEY)).thenReturn(pending);

        source.getCountryPreferenceAsync();
        assertTrue(pending.hasObservers());

        source.onDestroy();

        assertFalse(pending.hasObservers());
        // A late emission must not reach the callback after destroy.
        pending.onSuccess("IT");
        verify(callback, never()).onGettingCountryPreferenceSuccess(any());
    }
    /**
     * The page and its refresh flag must be handed to the DAO untouched, since the data source
     * adds no logic of its own here
     */
    @Test
    public void savePage_delegatesToDao() {
        source.savePage(true, null, null);

        verify(dao).savePage(true, null, null);
    }
    /**
     * The refresh time must be saved as a string under its own key: here a timestamp in
     * milliseconds that only fits in a long
     */
    @Test
    public void saveLastTimeFetched_savesTimestampAsStringUnderItsKey() {
        Preferences saved = mock(Preferences.class);
        when(datastore.saveStringResource(LAST_TIME_UPDATED_KEY, "1700000000000"))
                .thenReturn(Single.just(saved));

        source.saveLastTimeFetched(1700000000000L)
                .test()
                .assertValue(saved);
    }
    /**
     * The remote key must be returned as the DAO provides it: here a stored key
     */
    @Test
    public void getRadioRemoteKey_returnsKeyFromDao() {
        RadioRemoteKey key = mock(RadioRemoteKey.class);
        when(dao.getRadioRemoteKey()).thenReturn(key);

        assertSame(key, source.getRadioRemoteKey());
    }
    /**
     * A missing remote key must come back as null and not fail: here the very first launch,
     * before any page has been cached
     */
    @Test
    public void getRadioRemoteKey_nothingCached_returnsNull() {
        when(dao.getRadioRemoteKey()).thenReturn(null);

        assertNull(source.getRadioRemoteKey());
    }
    /**
     * The country must be read under the chosen-country key and returned as a stream: here a
     * stored country code
     */
    @Test
    public void getCountryPreference_returnsStoredCountry() {
        when(datastore.getStringResource(CHOSEN_COUNTRY_KEY)).thenReturn(Single.just("IT"));

        source.getCountryPreference()
                .test()
                .assertValue("IT");
    }
    /**
     * The paging source must be the one created by the DAO, not a new or wrapped one
     */
    @Test
    @SuppressWarnings("unchecked")
    public void getRadioDAOPagingSource_returnsDaoPagingSource() {
        PagingSource<Integer, RadioStation> pagingSource = mock(PagingSource.class);
        when(dao.pagingSource()).thenReturn(pagingSource);

        assertSame(pagingSource, source.getRadioDAOPagingSource());
    }
    /**
     * Every list the DAO emits must reach the caller as it is: here a list with one favourite
     */
    @Test
    public void observeFavourites_emitsListsFromDao() {
        List<FavouriteStation> favourites = Collections.singletonList(mock(FavouriteStation.class));
        when(dao.observeFavourites()).thenReturn(Flowable.just(favourites));

        source.observeFavourites()
                .test()
                .assertValue(favourites);
    }


}
