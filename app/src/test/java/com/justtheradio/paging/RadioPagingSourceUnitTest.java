package com.justtheradio.paging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.justtheradio.model.radio.RadioStation;
import com.justtheradio.source.BaseRadioBrowserRemoteDataSource;
import com.justtheradio.source.BaseRadioLocalDataSource;
import com.justtheradio.utils.radio.RadioStationMapper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import androidx.paging.PagingConfig;
import androidx.paging.PagingSource;
import androidx.paging.PagingSource.LoadResult;
import androidx.paging.PagingState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.sfuhrm.radiobrowser4j.Station;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Unit tests for {@link RadioSearchPagingSource}.
 * The data sources are mocked, the station mapper is mocked with
 * {@code mockStatic}, and the IO scheduler is replaced by a synchronous one.
 */
public class RadioPagingSourceUnitTest {
    /** Country code returned by the mocked local data source. */
    private static final String COUNTRY = "IT";
    /** Search query passed to the source. */
    private static final String QUERY = "jazz";
    /** Number of stations requested by the tests that load a full page. */
    private static final int LOAD_SIZE = 20;

    /** Mocked remote data source, used to provide the pages of stations. */
    private BaseRadioBrowserRemoteDataSource remote;
    /** Mocked local data source, used to provide the country preference. */
    private BaseRadioLocalDataSource local;
    /** Static mock replacing the station mapper, closed after each test. */
    private MockedStatic<RadioStationMapper> mapperStatic;
    /** The paging source under test, built on the mocked dependencies. */
    private RadioSearchPagingSource source;

    /**
     * Builds the source on mocked dependencies. The IO scheduler is made synchronous, the
     * mapper turns every station into a valid one, and the local data source knows the country
     */
    @Before
    public void setUp() {
        RxJavaPlugins.setIoSchedulerHandler(scheduler -> Schedulers.trampoline());

        remote = mock(BaseRadioBrowserRemoteDataSource.class);
        local = mock(BaseRadioLocalDataSource.class);

        mapperStatic = Mockito.mockStatic(RadioStationMapper.class);
        mapperStatic.when(() -> RadioStationMapper.fromStationToRadioStationModel(
                any(Station.class))).thenAnswer(invocation -> new RadioStation());

        when(local.getCountryPreference()).thenReturn(Single.just(COUNTRY));

        source = new RadioSearchPagingSource(remote, local, QUERY);
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
     * Runs a load on the source and waits for its result.
     *
     * @param key      The key of the page to load, or {@code null} for the first page
     * @param loadSize The number of stations requested
     * @return The result of the load
     */
    private LoadResult<Integer, RadioStation> load(Integer key, int loadSize) {
        PagingSource.LoadParams<Integer> params;
        if (key == null)
            params = new PagingSource.LoadParams.Refresh<Integer>(null, loadSize, false);
        else
            params = new PagingSource.LoadParams.Append<Integer>(key, loadSize, false);
        return source.loadSingle(params).blockingGet();
    }

    /**
     * Casts a result that is expected to be a page.
     *
     * @param result The result of a load
     * @return The result as a page
     */
    private static LoadResult.Page<Integer, RadioStation> asPage(
            LoadResult<Integer, RadioStation> result) {
        assertTrue(result instanceof LoadResult.Page);
        return (LoadResult.Page<Integer, RadioStation>) result;
    }

    /**
     * The first page has no key, so it must start from offset 0, use the requested load size
     * and the query, and give back one station for each one fetched
     */
    @Test
    public void load_firstPage_startsFromOffsetZeroWithQuery() throws Exception {
        when(remote.getRadioStationsByCountry(COUNTRY, QUERY, 0, LOAD_SIZE))
                .thenReturn(stations(LOAD_SIZE));

        LoadResult.Page<Integer, RadioStation> page = asPage(load(null, LOAD_SIZE));

        assertEquals(LOAD_SIZE, page.getData().size());
        verify(remote).getRadioStationsByCountry(COUNTRY, QUERY, 0, LOAD_SIZE);
    }
    /**
     * A later page must start from the offset given as key, and the next key must move forward
     * by the number of stations fetched
     */
    @Test
    public void load_laterPage_startsFromKeyAndAdvancesNextKey() throws Exception {
        when(remote.getRadioStationsByCountry(COUNTRY, QUERY, 40, LOAD_SIZE))
                .thenReturn(stations(LOAD_SIZE));

        LoadResult.Page<Integer, RadioStation> page = asPage(load(40, LOAD_SIZE));

        assertEquals(Integer.valueOf(60), page.getNextKey());
    }
    /**
     * A page must never have a previous key, since the list is only loaded from the beginning
     * towards the end
     */
    @Test
    public void load_page_hasNoPreviousKey() throws Exception {
        when(remote.getRadioStationsByCountry(COUNTRY, QUERY, 40, LOAD_SIZE))
                .thenReturn(stations(LOAD_SIZE));

        LoadResult.Page<Integer, RadioStation> page = asPage(load(40, LOAD_SIZE));

        assertNull(page.getPrevKey());
    }
    /**
     * A page with fewer stations than requested means there are no more results, so the next
     * key must be null
     */
    @Test
    public void load_partialPage_hasNoNextKey() throws Exception {
        when(remote.getRadioStationsByCountry(COUNTRY, QUERY, 0, LOAD_SIZE))
                .thenReturn(stations(7));

        LoadResult.Page<Integer, RadioStation> page = asPage(load(null, LOAD_SIZE));

        assertEquals(7, page.getData().size());
        assertNull(page.getNextKey());
    }
    /**
     * A search with no results must give an empty page with no next key, and not an error
     */
    @Test
    public void load_noResults_returnsEmptyPageWithNoNextKey() throws Exception {
        when(remote.getRadioStationsByCountry(COUNTRY, QUERY, 0, LOAD_SIZE))
                .thenReturn(Collections.emptyList());

        LoadResult.Page<Integer, RadioStation> page = asPage(load(null, LOAD_SIZE));

        assertTrue(page.getData().isEmpty());
        assertNull(page.getNextKey());
    }
    /**
     * Stations that the mapper rejects must not appear in the page, while the next key must
     * still advance by the number of stations fetched, so that the rejected ones are not
     * requested again
     */
    @Test
    public void load_invalidStations_areSkippedButNextKeyCountsThem() throws Exception {
        Station valid1 = mock(Station.class);
        Station invalid = mock(Station.class);
        Station valid2 = mock(Station.class);
        mapperStatic.when(() -> RadioStationMapper.fromStationToRadioStationModel(invalid))
                .thenReturn(null);
        when(remote.getRadioStationsByCountry(COUNTRY, QUERY, 0, 3))
                .thenReturn(Arrays.asList(valid1, invalid, valid2));

        LoadResult.Page<Integer, RadioStation> page = asPage(load(null, 3));

        assertEquals(2, page.getData().size());
        assertEquals(Integer.valueOf(3), page.getNextKey());
    }
    /**
     * The results of a search must not be saved anywhere: the source may only read the country
     * from the local data source
     */
    @Test
    public void load_success_onlyReadsCountryFromLocal() throws Exception {
        when(remote.getRadioStationsByCountry(COUNTRY, QUERY, 0, LOAD_SIZE))
                .thenReturn(stations(LOAD_SIZE));

        load(null, LOAD_SIZE);

        verify(local).getCountryPreference();
        verifyNoMoreInteractions(local);
    }
    /**
     * A network error must be returned as an error result with the original exception, and not
     * thrown to the caller
     */
    @Test
    public void load_remoteThrows_returnsError() throws Exception {
        IOException error = new IOException("connection reset");
        when(remote.getRadioStationsByCountry(COUNTRY, QUERY, 0, LOAD_SIZE)).thenThrow(error);

        LoadResult<Integer, RadioStation> result = load(null, LOAD_SIZE);

        assertTrue(result instanceof LoadResult.Error);
        assertSame(error, ((LoadResult.Error<Integer, RadioStation>) result).getThrowable());
    }
    /**
     * A failure reading the country preference must be returned as an error result with the
     * original exception, without calling the network
     */
    @Test
    public void load_countryUnavailable_returnsErrorWithoutNetworkCall() throws Exception {
        RuntimeException error = new RuntimeException("no country");
        when(local.getCountryPreference()).thenReturn(Single.<String>error(error));

        LoadResult<Integer, RadioStation> result = load(null, LOAD_SIZE);

        assertTrue(result instanceof LoadResult.Error);
        assertSame(error, ((LoadResult.Error<Integer, RadioStation>) result).getThrowable());
        verify(remote, never()).getRadioStationsByCountry(any(), any(), anyInt(), anyInt());
    }
    /**
     * The refresh key must always be null, so that the search restarts from the first page
     * instead of restoring the scroll position, even when Paging knows where the user was
     */
    @Test
    public void getRefreshKey_withAnchorPosition_returnsNull() {
        PagingConfig config = new PagingConfig(10, 2, false, 30);
        PagingState<Integer, RadioStation> state =
                new PagingState<>(Collections.emptyList(), 5, config, 0);

        assertNull(source.getRefreshKey(state));
    }
}
