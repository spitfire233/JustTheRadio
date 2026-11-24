package com.justtheradio.paging;

import static com.justtheradio.utils.constant.LocalCacheConstants.LAST_TIME_UPDATED_KEY;
import static com.justtheradio.utils.constant.RadioBrowserConstants.RADIO_BROWSER_EXPIRED_THRESHOLD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import androidx.datastore.preferences.core.Preferences;
import androidx.paging.LoadType;
import androidx.paging.PagingConfig;
import androidx.paging.PagingState;
import androidx.paging.RemoteMediator.MediatorResult;
import androidx.paging.RemoteMediator.InitializeAction;

import com.justtheradio.model.radio.RadioRemoteKey;
import com.justtheradio.model.radio.RadioStation;
import com.justtheradio.source.BaseRadioBrowserRemoteDataSource;
import com.justtheradio.source.BaseRadioLocalDataSource;
import com.justtheradio.utils.datastore.DataStoreManager;
import com.justtheradio.utils.radio.RadioStationMapper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import de.sfuhrm.radiobrowser4j.Station;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Unit tests for {@link RadioRemoteMediator}.
 * The data sources and the DataStore manager are mocked, the station mapper
 * is mocked with {@code mockStatic}, and the IO scheduler is replaced by a synchronous one.
 */
public class RadioRemoteMediatorUnitTest {
    /** Page size of the paging configuration used by the tests. */
    private static final int PAGE_SIZE = 10;
    /** Initial load size of the paging configuration used by the tests. */
    private static final int INITIAL_LOAD_SIZE = 30;
    /** Country code returned by the mocked local data source. */
    private static final String COUNTRY = "IT";

    /** Mocked DataStore manager, used to provide the time of the last fetch. */
    private DataStoreManager datastore;
    /** Mocked remote data source, used to provide the pages of stations. */
    private BaseRadioBrowserRemoteDataSource remote;
    /** Mocked local data source, used to provide the remote key and to verify the saves. */
    private BaseRadioLocalDataSource local;
    /** Static mock replacing the station mapper, closed after each test. */
    private MockedStatic<RadioStationMapper> mapperStatic;
    /** Paging state passed to the mediator, built on a real paging configuration. */
    private PagingState<Integer, RadioStation> pagingState;
    /** The mediator under test, built on the mocked dependencies. */
    private RadioRemoteMediator mediator;

    /**
     * Builds the mediator on mocked dependencies. The IO scheduler is made synchronous, the
     * mapper turns every station into a valid one, and the local data source knows the
     * country and accepts the saving of the time of the last fetch
     */
    @Before
    public void setUp() {
        RxJavaPlugins.setIoSchedulerHandler(scheduler -> Schedulers.trampoline());

        datastore = mock(DataStoreManager.class);
        remote = mock(BaseRadioBrowserRemoteDataSource.class);
        local = mock(BaseRadioLocalDataSource.class);

        mapperStatic = Mockito.mockStatic(RadioStationMapper.class);
        mapperStatic.when(() -> RadioStationMapper.fromStationToRadioStationModel(
                any(Station.class))).thenAnswer(invocation -> new RadioStation());

        Preferences saved = mock(Preferences.class);
        when(local.getCountryPreference()).thenReturn(Single.just(COUNTRY));
        when(local.saveLastTimeFetched(anyLong())).thenReturn(Single.just(saved));

        PagingConfig config = new PagingConfig(PAGE_SIZE, 2, false, INITIAL_LOAD_SIZE);
        pagingState = new PagingState<>(Collections.emptyList(), null, config, 0);

        mediator = new RadioRemoteMediator(datastore, remote, local);
    }

    /**
     * The static mock and the scheduler override are global to the thread and the process: they
     * must be undone after each test, or they leak into the following ones
     */
    @After
    public void tearDown() {
        mapperStatic.close();
        RxJavaPlugins.reset();
    }

    /**
     * Builds a list of mocked stations.
     *
     * @param count The number of stations to build
     * @return A list with {@code count} stations
     */
    private static List<Station> stations(int count) {
        List<Station> stations = new ArrayList<>();
        for (int i = 0; i < count; i++)
            stations.add(mock(Station.class));
        return stations;
    }
    /**
     * Runs a load on the mediator and waits for its result.
     *
     * @param loadType The type of load to run
     * @return The result of the load
     */
    private MediatorResult load(LoadType loadType) {
        return mediator.loadSingle(loadType, pagingState).blockingGet();
    }

    /**
     * Reads the end-of-pagination flag of a result that is expected to be a success.
     *
     * @param result The result of a load
     * @return {@code true} if the result says that no more data is available
     */
    private static boolean endReached(MediatorResult result) {
        assertTrue(result instanceof MediatorResult.Success);
        return ((MediatorResult.Success) result).endOfPaginationReached();
    }

    /**
     * With no remote key stored the cache is empty, so an initial refresh must be launched
     * without even looking at the time of the last fetch
     */
    @Test
    public void initializeSingle_noRemoteKey_launchesInitialRefresh() {
        when(local.getRadioRemoteKey()).thenReturn(null);

        mediator.initializeSingle()
                .test()
                .assertValue(InitializeAction.LAUNCH_INITIAL_REFRESH);

        verifyNoInteractions(datastore);
    }
    /**
     * A cache older than the expiration threshold must be refreshed
     */
    @Test
    public void initializeSingle_staleCache_launchesInitialRefresh() {
        long lastFetch = System.currentTimeMillis() - RADIO_BROWSER_EXPIRED_THRESHOLD - 1000;
        when(local.getRadioRemoteKey()).thenReturn(new RadioRemoteKey(30, false));
        when(datastore.getStringResource(LAST_TIME_UPDATED_KEY))
                .thenReturn(Single.just(Long.toString(lastFetch)));

        mediator.initializeSingle()
                .test()
                .assertValue(InitializeAction.LAUNCH_INITIAL_REFRESH);
    }
    /**
     * A cache fetched more recently than the expiration threshold must be shown as it is,
     * with no refresh
     */
    @Test
    public void initializeSingle_freshCache_skipsInitialRefresh() {
        long lastFetch = System.currentTimeMillis();
        when(local.getRadioRemoteKey()).thenReturn(new RadioRemoteKey(30, false));
        when(datastore.getStringResource(LAST_TIME_UPDATED_KEY))
                .thenReturn(Single.just(Long.toString(lastFetch)));

        mediator.initializeSingle()
                .test()
                .assertValue(InitializeAction.SKIP_INITIAL_REFRESH);
    }
    /**
     * A missing time of the last fetch currently surfaces as an error, because the mediator
     * does not handle it. It can happen if the app stops after saving a page and before saving
     * the time
     */
    @Test
    public void initializeSingle_lastTimeMissing_propagatesError() {
        NoSuchElementException error = new NoSuchElementException("no key");
        when(local.getRadioRemoteKey()).thenReturn(new RadioRemoteKey(30, false));
        when(datastore.getStringResource(LAST_TIME_UPDATED_KEY))
                .thenReturn(Single.error(error));

        mediator.initializeSingle()
                .test()
                .assertError(error);
    }
    /**
     * A refresh must start from offset 0 with the initial load size, replace the cached data
     * and store a key that continues after the loaded stations when the page is full
     */
    @Test
    public void load_refreshFullPage_savesPageAndKeyForNextOffset() throws Exception {
        when(remote.getRadioStationsByCountry(COUNTRY, 0, INITIAL_LOAD_SIZE))
                .thenReturn(stations(INITIAL_LOAD_SIZE));

        MediatorResult result = load(LoadType.REFRESH);

        ArgumentCaptor<RadioRemoteKey> key = ArgumentCaptor.forClass(RadioRemoteKey.class);
        verify(local).savePage(eq(true), anyList(), key.capture());
        assertEquals(INITIAL_LOAD_SIZE, key.getValue().getNextOffset());
        assertFalse(key.getValue().isEndReached());
        assertFalse(endReached(result));
    }
    /**
     * A page with fewer stations than requested means there is nothing more to load, so the
     * stored key and the result must both say that the end has been reached
     */
    @Test
    public void load_refreshPartialPage_reportsEndReached() throws Exception {
        when(remote.getRadioStationsByCountry(COUNTRY, 0, INITIAL_LOAD_SIZE))
                .thenReturn(stations(12));

        MediatorResult result = load(LoadType.REFRESH);

        ArgumentCaptor<RadioRemoteKey> key = ArgumentCaptor.forClass(RadioRemoteKey.class);
        verify(local).savePage(eq(true), anyList(), key.capture());
        assertEquals(12, key.getValue().getNextOffset());
        assertTrue(key.getValue().isEndReached());
        assertTrue(endReached(result));
    }
    /**
     * A refresh must save the time of the fetch, and the saved time must lie between the start
     * and the end of the load
     */
    @Test
    public void load_refresh_savesLastTimeFetched() throws Exception {
        when(remote.getRadioStationsByCountry(COUNTRY, 0, INITIAL_LOAD_SIZE))
                .thenReturn(stations(INITIAL_LOAD_SIZE));

        long before = System.currentTimeMillis();
        load(LoadType.REFRESH);
        long after = System.currentTimeMillis();

        ArgumentCaptor<Long> time = ArgumentCaptor.forClass(Long.class);
        verify(local).saveLastTimeFetched(time.capture());
        assertTrue(time.getValue() >= before && time.getValue() <= after);
    }
    /**
     * Stations that the mapper rejects must not be saved, while the next offset must still
     * advance by the number of stations fetched, so that the rejected ones are not requested
     * again
     */
    @Test
    public void load_invalidStations_areSkippedButOffsetCountsThem() throws Exception {
        Station valid1 = mock(Station.class);
        Station invalid = mock(Station.class);
        Station valid2 = mock(Station.class);
        mapperStatic.when(() -> RadioStationMapper.fromStationToRadioStationModel(invalid))
                .thenReturn(null);
        when(remote.getRadioStationsByCountry(COUNTRY, 0, INITIAL_LOAD_SIZE))
                .thenReturn(Arrays.asList(valid1, invalid, valid2));

        load(LoadType.REFRESH);

        ArgumentCaptor<RadioRemoteKey> key = ArgumentCaptor.forClass(RadioRemoteKey.class);
        verify(local).savePage(eq(true), argThat(saved -> saved.size() == 2), key.capture());
        assertEquals(3, key.getValue().getNextOffset());
    }
    /**
     * An append must continue from the offset of the stored key with the page size, add to the
     * cached data without replacing it, and store a key that moves the offset forward
     */
    @Test
    public void load_append_continuesFromStoredKey() throws Exception {
        when(local.getRadioRemoteKey()).thenReturn(new RadioRemoteKey(30, false));
        when(remote.getRadioStationsByCountry(COUNTRY, 30, PAGE_SIZE))
                .thenReturn(stations(PAGE_SIZE));

        MediatorResult result = load(LoadType.APPEND);

        ArgumentCaptor<RadioRemoteKey> key = ArgumentCaptor.forClass(RadioRemoteKey.class);
        verify(local).savePage(eq(false), anyList(), key.capture());
        assertEquals(40, key.getValue().getNextOffset());
        assertFalse(key.getValue().isEndReached());
        assertFalse(endReached(result));
    }
    /**
     * An append must not touch the time of the last fetch, since only a refresh renews the
     * cache
     */
    @Test
    public void load_append_doesNotSaveLastTimeFetched() throws Exception {
        when(local.getRadioRemoteKey()).thenReturn(new RadioRemoteKey(30, false));
        when(remote.getRadioStationsByCountry(COUNTRY, 30, PAGE_SIZE))
                .thenReturn(stations(PAGE_SIZE));

        load(LoadType.APPEND);

        verify(local, never()).saveLastTimeFetched(anyLong());
    }
    /**
     * An append with no stored key has nowhere to continue from, so it must end the pagination
     * without calling the network
     */
    @Test
    public void load_appendWithoutKey_endsPaginationWithoutNetworkCall() throws Exception {
        when(local.getRadioRemoteKey()).thenReturn(null);

        MediatorResult result = load(LoadType.APPEND);

        assertTrue(endReached(result));
        verify(remote, never()).getRadioStationsByCountry(any(), anyInt(), anyInt());
    }
    /**
     * An append after the end has been reached must end the pagination without calling the
     * network
     */
    @Test
    public void load_appendAfterEndReached_endsPaginationWithoutNetworkCall() throws Exception {
        when(local.getRadioRemoteKey()).thenReturn(new RadioRemoteKey(60, true));

        MediatorResult result = load(LoadType.APPEND);

        assertTrue(endReached(result));
        verify(remote, never()).getRadioStationsByCountry(any(), anyInt(), anyInt());
    }
    /**
     * Prepend is never needed, so it must return immediately with the end reached and without
     * touching the network, the database or the DataStore
     */
    @Test
    public void load_prepend_endsPaginationWithoutTouchingAnything() {
        MediatorResult result = load(LoadType.PREPEND);

        assertTrue(endReached(result));
        verifyNoInteractions(remote, datastore);
        verify(local, never()).savePage(anyBoolean(), anyList(), any(RadioRemoteKey.class));
    }
    /**
     * A network error must be returned as an error result with the original exception, and
     * nothing must be saved, so the cache is not wiped when the refresh fails
     */
    @Test
    public void load_remoteThrows_returnsErrorAndSavesNothing() throws Exception {
        IOException error = new IOException("connection reset");
        when(remote.getRadioStationsByCountry(COUNTRY, 0, INITIAL_LOAD_SIZE)).thenThrow(error);

        MediatorResult result = load(LoadType.REFRESH);

        assertTrue(result instanceof MediatorResult.Error);
        assertSame(error, ((MediatorResult.Error) result).getThrowable());
        verify(local, never()).savePage(anyBoolean(), anyList(), any(RadioRemoteKey.class));
    }
    /**
     * A failure reading the country preference must be returned as an error result with the
     * original exception, without calling the network
     */
    @Test
    public void load_countryUnavailable_returnsErrorWithoutNetworkCall() throws Exception {
        RuntimeException error = new RuntimeException("no country");
        when(local.getCountryPreference()).thenReturn(Single.error(error));

        MediatorResult result = load(LoadType.REFRESH);

        assertTrue(result instanceof MediatorResult.Error);
        assertSame(error, ((MediatorResult.Error) result).getThrowable());
        verify(remote, never()).getRadioStationsByCountry(any(), anyInt(), anyInt());
    }
    /**
     * A failure saving the page must be returned as an error result with the original
     * exception, and the time of the last fetch must not be saved, since the cache was not
     * renewed
     */
    @Test
    public void load_savePageThrows_returnsErrorAndDoesNotSaveLastTime() throws Exception {
        RuntimeException error = new RuntimeException("database error");
        when(remote.getRadioStationsByCountry(COUNTRY, 0, INITIAL_LOAD_SIZE))
                .thenReturn(stations(INITIAL_LOAD_SIZE));
        doThrow(error).when(local).savePage(anyBoolean(), anyList(), any(RadioRemoteKey.class));

        MediatorResult result = load(LoadType.REFRESH);

        assertTrue(result instanceof MediatorResult.Error);
        assertSame(error, ((MediatorResult.Error) result).getThrowable());
        verify(local, never()).saveLastTimeFetched(anyLong());
    }
}
