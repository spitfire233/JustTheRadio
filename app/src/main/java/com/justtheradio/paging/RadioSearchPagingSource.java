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
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import de.sfuhrm.radiobrowser4j.Station;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class RadioSearchPagingSource extends RxPagingSource<Integer, RadioStation> {
    private final BaseRadioBrowserRemoteDataSource remote;
    private final BaseRadioLocalDataSource local;
    private final String query;

    public RadioSearchPagingSource(BaseRadioBrowserRemoteDataSource remote,
                                   BaseRadioLocalDataSource local,
                                   String query) {
        this.remote = remote;
        this.local = local;
        this.query = query;
    }

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

    private LoadResult<Integer, RadioStation> load(String country, int offset, int limit) throws Exception{
        List<Station> raw = remote.getRadioStationsByCountry(country, query, offset, limit);

        List<RadioStation> page = new ArrayList<>();

        for (Station station : raw) {
            RadioStation mapped = RadioStationMapper.fromStationToRadioStationModel(station);
            if (mapped != null)
                page.add(mapped);
        }
        Integer nextKey = raw.size() < limit ? null : offset + raw.size();
        return new LoadResult.Page<>(page, null, nextKey);
    }

    @Nullable
    @Override
    public Integer getRefreshKey(@NonNull PagingState<Integer, RadioStation> pagingState) {
        return null; // Always restart from the top
    }
}
