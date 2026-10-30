package com.justtheradio.ui.main.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.justtheradio.R;
import com.justtheradio.adapter.RadioCardAdapter;
import com.justtheradio.repository.RadioRepository;
import com.justtheradio.ui.viewmodel.player.PlayerViewModel;
import com.justtheradio.ui.viewmodel.player.PlayerViewModelFactory;
import com.justtheradio.ui.viewmodel.radio.RadioViewModel;
import com.justtheradio.ui.viewmodel.radio.RadioViewModelFactory;
import com.justtheradio.utils.source.ServiceLocator;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Screen of the main activity that shows the stations the user has marked as favourite.
 * The stations are displayed as cards in a two-column grid and kept updated by observing
 * the favourites from the database.
 */
public class FavouriteFragment extends Fragment {
    /** ViewModel shared with the host activity, used to observe the favourite stations. */
    private RadioViewModel radioViewModel;
    /** ViewModel shared with the host activity, used to start playing a station. */
    private PlayerViewModel playerViewModel;
    /** TextView that displays the message shown when there are no favourites. */
    private TextView noFavouritesTextView;
    /** List of the favourite stations. */
    private RecyclerView recyclerView;
    /** Progress indicator shown until the favourites have been loaded. */
    private CircularProgressIndicator progressIndicator;
    /**
     * Executed on the creation of the fragment.
     * Gets the {@link RadioRepository} and creates the {@link RadioViewModel} and the
     * {@link PlayerViewModel}. Both are scoped to the host activity, not to the fragment,
     * so they are shared with the other screens.
     *
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get the radio repository from the service locator and initialize the radio view model
        RadioRepository radioRepository = ServiceLocator.getInstance().getRadioRepository(
                requireActivity().getApplication()
        );
        radioViewModel = new ViewModelProvider(
                requireActivity(),
                new RadioViewModelFactory(radioRepository)
        ).get(RadioViewModel.class);
        // Initialize the player view model
        playerViewModel = new ViewModelProvider(
                requireActivity(),
                new PlayerViewModelFactory()
        ).get(PlayerViewModel.class);
    }
    /**
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favourite, container, false);
        noFavouritesTextView = view.findViewById(R.id.no_favourites_text_view);
        recyclerView = view.findViewById(R.id.favourites_recycler_view);
        progressIndicator = view.findViewById(R.id.favourites_progress_indicator);
        return view;
    }
    /**
     * Executed after the fragment view has been created.
     * Setups the adapter and the observes the user favourites.
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Initialize the adapter
        RadioCardAdapter radioCardAdapter = new RadioCardAdapter(
                station -> playerViewModel.setRadioStation(station)
        );
        // Set the recycler view layout manager and adpater
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        recyclerView.setAdapter(radioCardAdapter);
        // Observe the favourite stations from the database and notify the adapter of every
        // insertion/deletion
        radioViewModel.getFavouriteStations().observe(getViewLifecycleOwner(), stations -> {
            radioCardAdapter.submitList(stations); // Notify the adapter
            boolean isEmpty = stations.isEmpty();
            // Set up the UI based on the received the list
            noFavouritesTextView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            progressIndicator.setVisibility(View.GONE);
        });
    }
}