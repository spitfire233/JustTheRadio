package com.justtheradio.ui.start.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.justtheradio.R;
import com.justtheradio.repository.radio.RadioRepository;
import com.justtheradio.ui.viewmodel.radio.RadioViewModel;
import com.justtheradio.ui.viewmodel.radio.RadioViewModelFactory;
import com.justtheradio.utils.source.ServiceLocator;

public class ChooseCountryFragment extends Fragment {

    private RadioViewModel radioViewModel;

    public ChooseCountryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RadioRepository radioRepository = ServiceLocator.getInstance().getRadioRepository();
        radioViewModel = new ViewModelProvider(
                requireActivity(),
                new RadioViewModelFactory(radioRepository))
                .get(RadioViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        radioViewModel.fetchCountryCodes().observe(getViewLifecycleOwner(), result-> {

        });


        return inflater.inflate(R.layout.fragment_choose_country, container, false);
    }
}