package com.justtheradio.paging;

import static com.justtheradio.utils.constant.Constants.LAST_TIME_UPDATED_KEY;
import static com.justtheradio.utils.constant.Constants.RADIO_BROWSER_EXPIRED_THRESHOLD;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.paging.ExperimentalPagingApi;
import androidx.paging.LoadType;
import androidx.paging.PagingState;
import androidx.paging.rxjava3.RxRemoteMediator;

import com.justtheradio.model.RadioRemoteKey;
import com.justtheradio.model.RadioStation;
import com.justtheradio.source.BaseRadioBrowserRemoteDataSource;
import com.justtheradio.source.BaseRadioLocalDataSource;
import com.justtheradio.utils.datastore.DataStoreManagerSingleton;
import com.justtheradio.utils.radio.RadioStationMapper;
import com.justtheradio.utils.security.SecurityUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import de.sfuhrm.radiobrowser4j.Station;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

@OptIn(markerClass = ExperimentalPagingApi.class)
public class RadioRemoteMediator extends RxRemoteMediator<Integer, RadioStation> {

    private final DataStoreManagerSingleton datastore;
    private final BaseRadioBrowserRemoteDataSource remote;
    private final BaseRadioLocalDataSource local;

    public RadioRemoteMediator(DataStoreManagerSingleton datastore,
                               BaseRadioBrowserRemoteDataSource remote,
                               BaseRadioLocalDataSource local) {
        this.datastore = datastore;
        this.local = local;
        this.remote = remote;
    }


    @NonNull
    @Override
    public Single<InitializeAction> initializeSingle() {
        return datastore.getStringResource(LAST_TIME_UPDATED_KEY)
                .map(lastTimeUpdated -> System.currentTimeMillis() - Long.parseLong(lastTimeUpdated) >= RADIO_BROWSER_EXPIRED_THRESHOLD
                        ? InitializeAction.LAUNCH_INITIAL_REFRESH
                        : InitializeAction.SKIP_INITIAL_REFRESH)
                .onErrorReturnItem(InitializeAction.LAUNCH_INITIAL_REFRESH);
    }

    @NonNull
    @Override
    public Single<MediatorResult> loadSingle(@NonNull LoadType loadType,
                                             @NonNull PagingState<Integer, RadioStation> pagingState) {
        return Single.fromCallable(() -> load(loadType, pagingState))
                .subscribeOn(Schedulers.io())
                .onErrorReturn(MediatorResult.Error::new);
    }


    private MediatorResult load(LoadType loadType, PagingState<Integer, RadioStation> pagingState) {
        int limit;
        int offset;

        switch (loadType) {
            case REFRESH:
                offset = 0;
                limit = pagingState.getConfig().initialLoadSize;
                break;
            case PREPEND: // We never prepend
                return new MediatorResult.Success(true);
            default: // append
                RadioRemoteKey radioRemoteKey = local.getRadioRemoteKey();
                if (radioRemoteKey == null || radioRemoteKey.isEndReached())
                    return new MediatorResult.Success(true);
                offset = radioRemoteKey.getNextOffset();
                limit = pagingState.getConfig().pageSize;
        }

        try {
            String country = local.getCountryPreference().blockingGet();
            // Get the raw radio stations page, determine
            // if we have reached the end of the results and the next offset:
            List<Station> raw = remote.getRadioStationsByCountry(country, offset, limit);
            boolean isEndReached = raw.size() < limit;
            int nextOffset = offset + raw.size();

            // Construct radio list to insert in local cache
            Set<UUID> favouriteUUIDs = local.getFavouriteStations()
                    .stream()
                    .map(RadioStation::getUUID)
                    .collect(Collectors.toSet());

            List<RadioStation> toInsert = new ArrayList<>();
            for (int i = 0; i < raw.size(); i++) {
                RadioStation radioStation = RadioStationMapper.toRadioStation(raw.get(i));
                if (radioStation != null) {
                    radioStation.setFavourite(favouriteUUIDs.contains(radioStation.getUUID()));
                    toInsert.add(radioStation);
                }
            }

            RadioRemoteKey newKey = new RadioRemoteKey(nextOffset, isEndReached);
            boolean isRefresh = loadType == LoadType.REFRESH;
            local.savePage(isRefresh, toInsert, newKey);
            if (isRefresh)
                local.saveLastTimeFetched(System.currentTimeMillis())
                        .blockingSubscribe();
            return new MediatorResult.Success(isEndReached);
        } catch (Exception e) {
            return new MediatorResult.Error(e);
        }

    }
}
