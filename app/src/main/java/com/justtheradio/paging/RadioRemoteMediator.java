package com.justtheradio.paging;

import static com.justtheradio.utils.constant.Constants.LAST_TIME_UPDATED_KEY;
import static com.justtheradio.utils.constant.Constants.RADIO_BROWSER_EXPIRED_THRESHOLD;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.paging.ExperimentalPagingApi;
import androidx.paging.LoadType;
import androidx.paging.PagingState;
import androidx.paging.rxjava3.RxRemoteMediator;

import com.justtheradio.model.radio.RadioRemoteKey;
import com.justtheradio.model.radio.RadioStation;
import com.justtheradio.source.BaseRadioBrowserRemoteDataSource;
import com.justtheradio.source.BaseRadioLocalDataSource;
import com.justtheradio.utils.datastore.DataStoreManagerSingleton;
import com.justtheradio.utils.radio.RadioStationMapper;

import java.util.ArrayList;
import java.util.List;

import de.sfuhrm.radiobrowser4j.Station;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Paging 3 {@link RxRemoteMediator} implementation
 * that keeps the local Room database in sync with the Radio Browser API.
 * Only {@link LoadType#REFRESH REFRESH} and
 * {@link LoadType#APPEND APPEND} are supported; {@link LoadType#PREPEND PREPEND}
 * is never needed because the list is always loaded from the beginning towards the end.
 */
@OptIn(markerClass = ExperimentalPagingApi.class)
public class RadioRemoteMediator extends RxRemoteMediator<Integer, RadioStation> {
    /**
     * Instance of the datastore manager. Used to access local device cache
     */
    private final DataStoreManagerSingleton datastore;
    /**
     * Remote data source. Used to communicate with the Radio Browser API
     */
    private final BaseRadioBrowserRemoteDataSource remote;
    /**
     * Local data source. Used to communicate with the local Room database
     */
    private final BaseRadioLocalDataSource local;
    /**
     * Constructor for the {@link RadioRemoteMediator} class
     * @param datastore The single instance of the datastore manager
     * @param remote The remote data source used to communicate with the Radio Browser API
     * @param local The local data source used to communicate with the local Room database
     */
    public RadioRemoteMediator(DataStoreManagerSingleton datastore,
                               BaseRadioBrowserRemoteDataSource remote,
                               BaseRadioLocalDataSource local) {
        this.datastore = datastore;
        this.local = local;
        this.remote = remote;
    }
    /**
     * This method is called once by Paging 3 before any load,
     * to decide whether the cached data must be refreshed first:
     * <ul>
     *   <li>If no remote key is stored, the cache is empty: launch an initial refresh.</li>
     *   <li>Otherwise compare the time of the last fetch with
     *   {@link com.justtheradio.utils.constant.Constants#RADIO_BROWSER_EXPIRED_THRESHOLD RADIO_BROWSER_EXPIRED_THRESHOLD}:
     *   if the cache is stale, refresh; if it is still fresh, skip the refresh and show cached data immediately.
     *   </li>
     * </ul>
     * @return A {@link Single} emitting {@link InitializeAction#LAUNCH_INITIAL_REFRESH} if the cache
     *  is empty or expired, or {@link InitializeAction#SKIP_INITIAL_REFRESH} if the cached data is
     *  still fresh
     */
    @NonNull
    @Override
    public Single<InitializeAction> initializeSingle() {
        // Database access is blocking, so we execute it in a different thread
        return Single.fromCallable(() -> local.getRadioRemoteKey() == null) // Check if a key exists
                .subscribeOn(Schedulers.io())
                .flatMap(noKey -> {
                    if (noKey) return Single.just(InitializeAction.LAUNCH_INITIAL_REFRESH);
                    return datastore.getStringResource(LAST_TIME_UPDATED_KEY) // Check if cached data is stale
                            .map(last -> System.currentTimeMillis() - Long.parseLong(last)
                                    >= RADIO_BROWSER_EXPIRED_THRESHOLD
                                    ? InitializeAction.LAUNCH_INITIAL_REFRESH // Is stale: refresh
                                    : InitializeAction.SKIP_INITIAL_REFRESH); // Skip refresh
                });
    }
    /**
     * Entry point used by Paging 3 to load more data from the network into the database.
     * @param loadType    {@link LoadType#REFRESH}, {@link LoadType#PREPEND} or
     *                    {@link LoadType#APPEND}
     * @param pagingState current paging state (configuration and loaded pages)
     * @return A {@link Single} emitting a {@link MediatorResult.Success} (with
     * {@code endOfPaginationReached} set to {@code true} when no more data is available)
     * or a {@link MediatorResult.Error} if the load failed
     */
    @NonNull
    @Override
    public Single<MediatorResult> loadSingle(@NonNull LoadType loadType,
                                             @NonNull PagingState<Integer, RadioStation> pagingState) {
        return Single.fromCallable(() -> load(loadType, pagingState)) // Call actual loading function
                .subscribeOn(Schedulers.io())
                .onErrorReturn(MediatorResult.Error::new);
    }
    /**
     * Helper function that handles the actual page loading logic for
     * {@link RadioRemoteMediator#loadSingle(LoadType, PagingState) loadSingle}.
     * This function computes the offset and limit to use for the API call from the {@code loadType}
     * parameter, fetches them from the Radio Browser API if necessary and maps them to the local data
     * model. Finally, the function saves both the page and the new {@link RadioRemoteKey}
     * in the room database
     * @param loadType      {@link LoadType#REFRESH} restarts from offset 0 using the initial load size,
     *                      {@link LoadType#APPEND} continues from the stored {@link RadioRemoteKey}
     *                      using the page size, {@link LoadType#PREPEND} is never needed and
     *                      returns immediately
     * @param pagingState   Current paging state, used to read the paging configuration
     *                      (initial load size and page size)
     * @return {@link MediatorResult.Success} with {@code endOfPaginationReached} set to {@code true}
     * if there is no more data to load, or {@link MediatorResult.Error} if fetching, mapping
     * or saving the data failed
     */
    private MediatorResult load(LoadType loadType, PagingState<Integer, RadioStation> pagingState) {
        int limit;
        int offset;

        // Compute limit and offset based on the loadType received
        switch (loadType) {
            case REFRESH:
                // Restart from the beginning: offset is 0 and the limit is the one set in the
                // paging state config
                offset = 0;
                limit = pagingState.getConfig().initialLoadSize;
                break;
            case PREPEND: // We never prepend, return immediately
                return new MediatorResult.Success(true);
            default: // We're appending to the list
                // Get, if it exists, the radio remote key from the local database
                RadioRemoteKey radioRemoteKey = local.getRadioRemoteKey();
                if (radioRemoteKey == null || radioRemoteKey.isEndReached())
                    // The key does not exist or end of pagination is reached
                    return new MediatorResult.Success(true);
                // Continue from where the previous page has ended
                offset = radioRemoteKey.getNextOffset();
                limit = pagingState.getConfig().pageSize;
        }

        try {
            // Get the user's country preference from the local device cache
            String country = local.getCountryPreference().blockingGet();
            // Get the raw radio stations page, determine if we have reached the
            // end of the results and calculate the next offset:
            List<Station> raw = remote.getRadioStationsByCountry(country, offset, limit);
            boolean isEndReached = raw.size() < limit;
            int nextOffset = offset + raw.size();

            // Map the raw API stations objects to the local model
            List<RadioStation> toInsert = new ArrayList<>();
            for (Station station : raw) {
                RadioStation mapped = RadioStationMapper.fromStationToRadioStationModel(station);
                if (mapped != null) // mapper can return null when the station is invalid
                    toInsert.add(mapped);
            }
            // Create a new remote key to insert into the room database
            RadioRemoteKey newKey = new RadioRemoteKey(nextOffset, isEndReached);

            boolean isRefresh = loadType == LoadType.REFRESH;

            // Save the retrieved stations and the new remote key in the local database. If this
            // is a refresh, tell the Room database to wipe the cache before inserting
            local.savePage(isRefresh, toInsert, newKey);
            // If this is a refresh, save the last time we retrieved from the API in the local
            // device cache
            if (isRefresh)
                local.saveLastTimeFetched(System.currentTimeMillis()).blockingSubscribe();
            return new MediatorResult.Success(isEndReached);
        } catch (Exception e) {
            // If an error occurred, return an error state
            return new MediatorResult.Error(e);
        }
    }
}
