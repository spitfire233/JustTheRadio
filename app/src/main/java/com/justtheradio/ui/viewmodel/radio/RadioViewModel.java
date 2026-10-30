package com.justtheradio.ui.viewmodel.radio;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.LiveDataReactiveStreams;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.PagingData;
import androidx.paging.rxjava3.PagingRx;

import com.justtheradio.model.radio.BaseRadioStation;
import com.justtheradio.model.radio.FavouriteStation;
import com.justtheradio.model.radio.RadioStation;
import com.justtheradio.model.Result;
import com.justtheradio.repository.RadioRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.BehaviorProcessor;

public class RadioViewModel extends ViewModel {

    private final RadioRepository radioRepository;
    private final BehaviorProcessor<String> query = BehaviorProcessor.createDefault("");
    private final LiveData<PagingData<RadioStation>> radioStations;
    private final LiveData<Set<UUID>> favouriteUUIDs;
    private final LiveData<List<FavouriteStation>> favouriteStations;

    public RadioViewModel(RadioRepository radioRepository) {
        this.radioRepository = radioRepository;
        Flowable<PagingData<RadioStation>> flow = query
                .distinctUntilChanged()
                .switchMap(q -> q.isEmpty()
                          ? radioRepository.getRadioStations()
                          : radioRepository.searchStations(q));
        radioStations = LiveDataReactiveStreams.fromPublisher(
                PagingRx.cachedIn(flow, ViewModelKt.getViewModelScope(this)));
        favouriteUUIDs = LiveDataReactiveStreams.fromPublisher(radioRepository.observeFavouriteUUIDs());
        favouriteStations = LiveDataReactiveStreams.fromPublisher(radioRepository.observeFavourites());
    }

    public void setQuery(String query) {
        this.query.onNext(query == null ? "" : query);
    }

    public LiveData<PagingData<RadioStation>> getRadioStations() {
        return radioStations;
    }

    public LiveData<Result> saveCountryPreference(String country) {
        return this.radioRepository.saveCountryPreference(country);
    }

    public LiveData<Result> getCountryPreference() {
        return this.radioRepository.getCountryPreference();
    }

    public LiveData<Set<UUID>> getFavouriteUUIDs() {
        return favouriteUUIDs;
    }

    public LiveData<List<FavouriteStation>> getFavouriteStations() {
        return favouriteStations;
    }



    public void setFavourite(BaseRadioStation station, boolean favourite) {
        if (favourite) radioRepository.addFavourite(station);
        else radioRepository.removeFavourite(station);
    }

    public LiveData<Result> getSavingResultLiveData() {
        return radioRepository.getSavingResultLiveData();
    }
    public LiveData<Result> fetchCountryCodes() {
        return this.radioRepository.fetchCountryCodes();
    }

    @Override
    public void onCleared() {
        radioRepository.onDestroy();
    }
}
