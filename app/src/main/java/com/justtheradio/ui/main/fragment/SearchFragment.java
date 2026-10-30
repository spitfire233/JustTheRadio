package com.justtheradio.ui.main.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LoadState;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.justtheradio.R;
import com.justtheradio.adapter.RadioListPagingDataAdapter;
import com.justtheradio.repository.RadioRepository;
import com.justtheradio.ui.viewmodel.player.PlayerViewModel;
import com.justtheradio.ui.viewmodel.player.PlayerViewModelFactory;
import com.justtheradio.ui.viewmodel.radio.RadioViewModel;
import com.justtheradio.ui.viewmodel.radio.RadioViewModelFactory;
import com.justtheradio.utils.source.ServiceLocator;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import kotlin.Unit;

/**
 * Screen of the main activity where the user searches for radio stations by name or browses
 * the available radio stations for the selected country.
 */
public class SearchFragment extends Fragment {
    /** ViewModel shared with the host activity, used to set the query and get the results. */
    private RadioViewModel radioViewModel;
    /** ViewModel shared with the host activity, used to start playing a station. */
    private PlayerViewModel playerViewModel;
    /** Progress indicator shown while the results are loading. */
    private CircularProgressIndicator circularProgressIndicator;
    /** List of the search results. */
    private RecyclerView radiosRecyclerView;
    /** Search field where the user types the query. */
    private TextInputEditText editText;
    /** Adapter for the radio list items */
    private RadioListPagingDataAdapter adapter;
    /**
     * {@code true} if the list must be scrolled back to the top when the results of the
     * next search start loading, so that a new search does not keep the old scroll position.
     */
    private boolean shouldScrollOnTop = false;
    /**
     * Required empty public constructor
     */
    public SearchFragment() { }
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
        // Get the repository from the service locator and initialize radio the radio view model
        RadioRepository radioRepository = ServiceLocator.getInstance()
                .getRadioRepository(requireActivity().getApplication());
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        circularProgressIndicator = view.findViewById(R.id.search_fragment_circular_progress_indicator);
        radiosRecyclerView = view.findViewById(R.id.search_fragment_recycler_view);
        editText = view.findViewById(R.id.search_edit_text);
        return view;
    }
    /**
     * Executed after the fragment view has been created.
     * Sets up the list of results, the search field and the observers.
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Tapping a result starts playing the station
         adapter = new RadioListPagingDataAdapter(
                station -> playerViewModel.setRadioStation(station)
        );
        // Sets the recycler view layout manager and adapter
        radiosRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        radiosRecyclerView.setAdapter(adapter);

        setupReactiveElements();

        // Fetch the radio stations, results are paged
        radioViewModel.getRadioStations().observe(getViewLifecycleOwner(),
                data ->
                        adapter.submitData(getViewLifecycleOwner().getLifecycle(), data));

    }

    /**
     * Helper function that sets up the reactive elements for the UI of this fragment
     */
    private void setupReactiveElements() {
        // Set up the search bar
        editText.setOnEditorActionListener((textView, actionID, keyEvent) -> {
            // Check if the action from the keyboard is a search action
            boolean isSearchAction = actionID == EditorInfo.IME_ACTION_SEARCH;
            // Check if the key pressed is the enter key
            boolean isEnterPressed =
                    keyEvent != null
                            && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER
                            && keyEvent.getAction() == KeyEvent.ACTION_DOWN;
            // The user has finished inputting the query and presses enter
            if (isSearchAction || isEnterPressed) {
                // Set the query to trigger the search and reset the screen position to the top
                String query = textView.getText() == null ? "" : textView.getText().toString();
                radioViewModel.setQuery(query);
                shouldScrollOnTop = true;
                return true; // The event has been handled
            }
            return false; // The event wasn't handled because it isn't a search action
        });

        // Set up the list item adapter to show the results retrieved from the API and/or from
        // the local Room database
        adapter.addLoadStateListener(states -> {
            LoadState refresh = states.getRefresh(); // Check if the request is a refresh
            // Check if the request hasn't returned yet
            boolean isLoading = refresh instanceof LoadState.Loading;
            // Show the progress indicator instead of the list while loading
            circularProgressIndicator.setVisibility(isLoading ? View.VISIBLE : View.INVISIBLE);
            radiosRecyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);

            // A new search is starting: bring the list back to the top
            if (isLoading && shouldScrollOnTop) {
                radiosRecyclerView.scrollToPosition(0);
                shouldScrollOnTop = false;
            }
            // Check if there was an error of some kind while loading
            boolean isError = refresh instanceof LoadState.Error;
            if (isError) {
                // Show error in a toast
                Throwable error = ((LoadState.Error) refresh).getError();
                Toast.makeText(
                        requireActivity(),
                        String.valueOf(error.getMessage()),
                        Toast.LENGTH_SHORT
                ).show();
            }
            return Unit.INSTANCE;
        });
    }
}