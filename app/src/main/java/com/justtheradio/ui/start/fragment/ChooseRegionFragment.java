package com.justtheradio.ui.start.fragment;

import static com.justtheradio.utils.constant.Constants.CHOSEN_COUNTRY_KEY;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.justtheradio.R;

import org.jspecify.annotations.NonNull;

public class ChooseRegionFragment extends Fragment {

    public ChooseRegionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getParentFragmentManager().setFragmentResultListener(CHOSEN_COUNTRY_KEY, this,
                (requestKey, result) -> {

        });



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_choose_region, container, false);
    }
}