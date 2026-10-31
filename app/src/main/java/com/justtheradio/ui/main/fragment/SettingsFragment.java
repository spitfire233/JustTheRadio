package com.justtheradio.ui.main.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.button.MaterialButton;
import com.justtheradio.R;
import com.justtheradio.ui.intro.IntroActivity;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Simple settings screen that allows the user to reconfigure the application.
 */
public class SettingsFragment extends Fragment {

    /** Reconfiguring button. */
    private MaterialButton reconfigureButton;

    /**
     * Required public constructor for the {@link SettingsFragment} class.
     */
    public SettingsFragment() { }

    /**
     * Executed when the fragment is created. Initializes the radio view model.
     *
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Executed on the creation of the view for this fragment. Initializes the UI components.
     * Inflates the layout of the fragment and finds the views that compose the UI.
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return The root view of the inflated layout
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        reconfigureButton = view.findViewById(R.id.reconfiguring_button);
        // Inflate the layout for this fragment
        return view;
    }

    /**
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        reconfigureButton.setOnClickListener(v -> {
            startActivity(new Intent(requireActivity(), IntroActivity.class));
            requireActivity().finish();
        });
    }
}