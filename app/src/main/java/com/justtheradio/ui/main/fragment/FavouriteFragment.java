package com.justtheradio.ui.main.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.justtheradio.R;
import com.justtheradio.adapter.RadioCardAdapter;
import com.justtheradio.model.radio.BaseRadioStation;
import com.justtheradio.repository.RadioRepository;
import com.justtheradio.ui.viewmodel.player.PlayerViewModel;
import com.justtheradio.ui.viewmodel.player.PlayerViewModelFactory;
import com.justtheradio.ui.viewmodel.radio.RadioViewModel;
import com.justtheradio.ui.viewmodel.radio.RadioViewModelFactory;
import com.justtheradio.utils.source.ServiceLocator;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FavouriteFragment extends Fragment {

    private RadioViewModel radioViewModel;
    private PlayerViewModel playerViewModel;
    private final List<BaseRadioStation> favourites = new ArrayList<>();
    private TextView noFavouritesTextView;
    private RecyclerView recyclerView;
    private CircularProgressIndicator progressIndicator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RadioRepository radioRepository = ServiceLocator.getInstance().getRadioRepository(
                requireActivity().getApplication()
        );
        radioViewModel = new ViewModelProvider(
                requireActivity(),
                new RadioViewModelFactory(radioRepository)
        ).get(RadioViewModel.class);

        playerViewModel = new ViewModelProvider(
                requireActivity(),
                new PlayerViewModelFactory()
        ).get(PlayerViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favourite, container, false);
        noFavouritesTextView = view.findViewById(R.id.no_favourites_text_view);
        recyclerView = view.findViewById(R.id.favourites_recycler_view);
        progressIndicator = view.findViewById(R.id.favourites_progress_indicator);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RadioCardAdapter radioCardAdapter = new RadioCardAdapter(
                station -> playerViewModel.setRadioStation(station)
        );

        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        recyclerView.setAdapter(radioCardAdapter);

        radioViewModel.getFavouriteStations().observe(getViewLifecycleOwner(), stations -> {
            radioCardAdapter.submitList(stations);
            boolean isEmpty = stations.isEmpty();
            noFavouritesTextView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            progressIndicator.setVisibility(View.GONE);
        });

    }
}