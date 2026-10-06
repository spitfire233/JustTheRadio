package com.justtheradio.ui.start.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.justtheradio.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChooseRegionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChooseRegionFragment extends Fragment {

    public ChooseRegionFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static ChooseRegionFragment newInstance(String param1, String param2) {
        ChooseRegionFragment fragment = new ChooseRegionFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_choose_region, container, false);
    }
}