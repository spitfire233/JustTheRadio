package com.justtheradio.ui.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.justtheradio.R;
import com.justtheradio.repository.RadioStationsRepository;
import com.justtheradio.util.source.ServiceLocator;
import com.justtheradio.util.viewmodel.RadioViewModel;
import com.justtheradio.util.viewmodel.RadioViewModelFactory;

import java.util.ArrayList;
import java.util.List;

import de.sfuhrm.radiobrowser4j.Station;

public class HomeFragment extends Fragment {

    private static final String TAG = HomeFragment.class.getSimpleName();

    RadioViewModel radioViewModel;

    List<Station> stationList;

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
        // Inflate the layout for this fragment
        radioViewModel.getNationalRadioStations("IT", 0, 64).observe(getViewLifecycleOwner(), l ->{
            Log.d(TAG, "YAY!");
        });

        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    private void initViewModel() {
        RadioStationsRepository radioStationsRepository
                = ServiceLocator.getInstance().getRadioStationsRepository();
        radioViewModel = new ViewModelProvider(
                requireActivity(),
                new RadioViewModelFactory(radioStationsRepository)).get(RadioViewModel.class);
    }


}