package com.justtheradio.paging;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagingState;
import androidx.paging.rxjava3.RxPagingSource;

import com.justtheradio.model.radio.RadioStation;
import com.justtheradio.source.BaseRadioBrowserRemoteDataSource;
import com.justtheradio.source.BaseRadioLocalDataSource;
import com.justtheradio.utils.radio.RadioStationMapper;

import java.util.ArrayList;
import java.util.List;

import de.sfuhrm.radiobrowser4j.Station;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Paging 3 {@link RxPagingSource} implementation used by the search screen to look up radio stations
 * from the remote API. The results returned by this class are not cached in the room database.
 */
public class RadioSearchPagingSource extends RxPagingSource<Integer, RadioStation> {
    /**
     * Remote data source. Used to communicate with the Radio Browser API.
     */
    private final BaseRadioBrowserRemoteDataSource remote;
    /**
     * Local data source. Used to access the local Room database
     */
    private final BaseRadioLocalDataSource local;
    /**
     * The search query string
     */
    private final String query;
    /**
     * Constructor for the {@link RadioSearchPagingSource} class
     * @param remote The remote data source for the Radio Browser API
     * @param local  The local data source to access the Room database
     * @param query  The search query
     */
    public RadioSearchPagingSource(BaseRadioBrowserRemoteDataSource remote,
                                   BaseRadioLocalDataSource local,
                                   String query) {
        this.remote = remote;
        this.local = local;
        this.query = query;
    }
    /**
     * Entry point used by Paging 3 to load a page of search results.
     *
     * @param loadParams parameters of the load: the key of the page (the offset, or
     *                   {@code null} for the first page) and the requested load size
     * @return A {@link Single} emitting a {@link LoadResult.Page} with the loaded stations,
     * or a {@link LoadResult.Error} if the load failed
     */
    @NonNull
    @Override
    public Single<LoadResult<Integer, RadioStation>> loadSingle(@NonNull LoadParams<Integer> loadParams) {
        int offset = loadParams.getKey() != null ? loadParams.getKey() : 0;
        int limit = loadParams.getLoadSize();

        return local.getCountryPreference()
                .map(country -> load(country, offset, limit))
                .subscribeOn(Schedulers.io())
                .onErrorReturn(LoadResult.Error::new);
    }
    /**
     * Helper function that retrieves the handles the actual loading of the search results
     * @param country The user's country preference
     * @param offset  The offset to use when calling the Radio Browser API
     * @param limit   The limit to use when calling the Radio Browser API
     * @return A {@link LoadResult.Page LoadResult.Page} with the mapped stations, no previous key and the
     * offset of the next page as next key ({@code null} if there are no more results)
     * @throws Exception If the remote call fails
     */
    private LoadResult<Integer, RadioStation> load(String country, int offset, int limit) throws Exception{
        // Get the stations from the remote API
        List<Station> raw = remote.getRadioStationsByCountry(country, query, offset, limit);

        // Map the raw API data to the local data model
        List<RadioStation> page = new ArrayList<>();
        for (Station station : raw) {
            RadioStation mapped = RadioStationMapper.fromStationToRadioStationModel(station);
            if (mapped != null)
                page.add(mapped);
        }
        // Calculate the next offset and return the load result
        Integer nextKey = raw.size() < limit ? null : offset + raw.size();
        return new LoadResult.Page<>(page, null, nextKey);
    }
    /**
     * Tells Paging which key to use to reload the list when it is invalidated.
     * Always returns {@code null}, so the search restarts from the first page instead of
     * trying to restore the scroll position.
     *
     * @param pagingState current paging state
     * @return {@code null}, to always restart from the top
     */
    @Nullable
    @Override
    public Integer getRefreshKey(@NonNull PagingState<Integer, RadioStation> pagingState) {
        return null; // Always restart from the top
    }
}
