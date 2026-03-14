package com.justtheradio.ui.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.carousel.CarouselLayoutManager;
import com.google.android.material.carousel.CarouselSnapHelper;
import com.google.android.material.carousel.HeroCarouselStrategy;
import com.justtheradio.R;
import com.justtheradio.adapter.RadioStationsAdapter;
import com.justtheradio.adapter.listeners.OnClickRadioCardListener;
import com.justtheradio.model.RadioStation;
import com.justtheradio.model.Result;
import com.justtheradio.repository.RadioStationsRepository;
import com.justtheradio.util.source.ServiceLocator;
import com.justtheradio.ui.viewmodel.RadioViewModel;
import com.justtheradio.ui.viewmodel.RadioViewModelFactory;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final String TAG = HomeFragment.class.getSimpleName();

    private RadioViewModel radioViewModel;

    private List<RadioStation> stationList;

    private RadioStationsAdapter radioStationsAdapter;

    private RecyclerView nationalRadiosRecyclerView;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViewModel();
        stationList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nationalRadiosRecyclerView = view.findViewById(R.id.national_radios_recycler_view);

        CarouselLayoutManager carouselLayoutManager
                = new CarouselLayoutManager(new HeroCarouselStrategy());
        carouselLayoutManager.setCarouselAlignment(CarouselLayoutManager.ALIGNMENT_CENTER);
        nationalRadiosRecyclerView.setLayoutManager(carouselLayoutManager);
        CarouselSnapHelper carouselSnapHelper = new CarouselSnapHelper();
        carouselSnapHelper.attachToRecyclerView(nationalRadiosRecyclerView);

        radioStationsAdapter =
                new RadioStationsAdapter(R.layout.carousel_layout, stationList,
                new OnClickRadioCardListener() {
                    @Override
                    public void onFavouriteButtonPressed(int position) {

                    }
                    @Override
                    public void onRadioItemPressed(RadioStation station) {

                    }
                }, false);
        nationalRadiosRecyclerView.setAdapter(radioStationsAdapter);

        radioViewModel.getNationalRadioStations("IT", 0, 64).observe(
                getViewLifecycleOwner(),
                result -> {
                    if(result.isSuccess()) {
                        int initialSize = this.stationList.size();
                        this.stationList.clear();
                        this.stationList.addAll(((Result.Success) result).getRadioStations());
                        radioStationsAdapter.notifyItemRangeInserted(initialSize, this.stationList.size());
                    }
                }
        );
    }

    private void initViewModel() {
        RadioStationsRepository radioStationsRepository
                = ServiceLocator.getInstance().getRadioStationsRepository(
                        requireActivity().getApplication()
                );
        radioViewModel = new ViewModelProvider(
                requireActivity(),
                new RadioViewModelFactory(radioStationsRepository)).get(RadioViewModel.class);
    }


}