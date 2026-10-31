package com.justtheradio.ui.main;

import android.content.ComponentName;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.session.MediaController;
import androidx.media3.session.SessionToken;
import androidx.media3.ui.PlayerView;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.justtheradio.R;
import com.justtheradio.model.Result;
import com.justtheradio.model.radio.BaseRadioStation;
import com.justtheradio.model.radio.FavouriteStation;
import com.justtheradio.player.PlayerSessionService;
import com.justtheradio.repository.RadioRepository;
import com.justtheradio.ui.viewmodel.player.PlayerViewModel;
import com.justtheradio.ui.viewmodel.player.PlayerViewModelFactory;
import com.justtheradio.ui.viewmodel.radio.RadioViewModel;
import com.justtheradio.ui.viewmodel.radio.RadioViewModelFactory;
import com.justtheradio.utils.source.ServiceLocator;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Main activity of the app, shown after the intro.
 * It hosts the screens reachable from the bottom navigation bar and the media player.
 */
public class MainActivity extends AppCompatActivity {
    /** ViewModel that holds the station selected for playback. */
    private PlayerViewModel playerViewModel;
    /** ViewModel used to read and modify the favourite stations. */
    private RadioViewModel radioViewModel;
    /** Pending or completed connection to the player service; {@code null} when not connected. */
    private ListenableFuture<MediaController> future;
    /** View with the player controls, shown once a station has been selected. */
    private PlayerView playerView;

    /**
     * Executed when the activity is created.
     * Sets up the layout, the navigation and the favourite button of the player.
     *
     * @param savedInstanceState The previously saved state of the activity, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        // Call helper function to set up the navbar
        setupNavBar();
        // Apply the size of the system bars as padding of the root view
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize player view model
        playerViewModel = new ViewModelProvider(
                this,
                new PlayerViewModelFactory()
        ).get(PlayerViewModel.class);
        // Get the radio repository and initialize the radio view model
        RadioRepository radioRepository = ServiceLocator.getInstance().getRadioRepository(getApplication());
        radioViewModel = new ViewModelProvider(
                this,
                new RadioViewModelFactory(radioRepository)
        ).get(RadioViewModel.class);
        // Call helper function to set up the radio functionalities
        setupRadioUI();
        // Observe the results of the favourite adding/removing operations
        radioViewModel.getSavingResultLiveData().observe(this, result -> {
            if (result.isSuccess()) { // Status change operation was successfull
                Result.Success<BaseRadioStation> success = ((Result.Success) result);
                BaseRadioStation station = success.getData();

                String name = station.getName();
                // If the station returned is a favourite, then it was a saving operation
                String message = station instanceof FavouriteStation
                        ? String.format(getString(R.string.successfully_saved_radio_station), name)
                        : String.format(getString(R.string.successfully_removed_radio_station), name);
                // Show toast to the user
                Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
                toast.show();
            } else {
                // Get error message and prepare the message to show to the user
                String errorMessage = ((Result.Failure) result).getMessage();
                String message = getString(R.string.error_while_modifying_station_saved_state)
                        + ":" + errorMessage;
                // Show toast to the user
                Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }
    /**
     * Executed on the start of the activity.
     * Connects to the {@link PlayerSessionService} when the activity becomes visible.
     */
    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onStart() {
        super.onStart();
        // Identify the service to connect to
        SessionToken sessionToken = new SessionToken(
                getApplicationContext(),
                new ComponentName(getApplicationContext(), PlayerSessionService.class)
        );

        // Connect asynchronously: the controller is not available immediately
        future = new MediaController.Builder(
                getApplicationContext(), sessionToken
        ).buildAsync();

        // Listen to the status of the media controller
        future.addListener(() -> {
            try {
                // The controller is available, now we can obtain it
                MediaController controller = future.get();
                // Set up the player for the player view with the media controller
                playerView.setPlayer(controller);

                // Observe the currently selected radio station
                playerViewModel.getSelectedRadioStation().observe(this, station -> {
                    // Show the name of the station currently playing
                    TextView textView = findViewById(R.id.player_station_name_text_view);
                    textView.setText(station.getName());
                    // Needed for the marquis effect
                    textView.setSelected(true);

                    // Load the logo of the station, showing the app logo until it is ready or when
                    // an error occurs
                    ImageView imageView = findViewById(R.id.player_radio_image_view);
                    Glide.with(this)
                            .load(station.getFaviconURL())
                            .placeholder(R.drawable.justtheradio_logo)
                            .error(R.drawable.justtheradio_logo)
                            .into(imageView);

                    // Show the media player and the playback controls
                    playerView.setVisibility(View.VISIBLE);
                    playerView.showController();
                    // Load the stream of the station and start playing it
                    MediaItem item = MediaItem.fromUri(station.getURL());
                    controller.setMediaItem(item);
                    controller.prepare();
                    controller.play();
                });
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            // Run the listener directly on the thread that completes the future
        }, MoreExecutors.directExecutor());
    }
    /**
     * Helper functions that sets up the bottom navigation view with the navigation graph
     */
    private void setupNavBar() {
        // Get a reference to the nav host fragment through the fragment manager
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.main_fragment_container_view);
        assert navHostFragment != null;
        // Get the nav controller and set up the bottom navigation view with the nav controller
        NavController navController = navHostFragment.getNavController();
        BottomNavigationView bottomNavigationView = findViewById(R.id.menu_bottom_navigation_view);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
        // Selecting the current item again goes back to the first screen of that item
        bottomNavigationView.setOnItemReselectedListener(item ->
                navController.popBackStack(item.getItemId(), false));
    }
    /**
     * Helper function that sets up the functionalities of the radio player UI
     */
    private void setupRadioUI() {
        // Get a reference to the player view
        playerView = findViewById(R.id.player_view);

        // Set up the checkbox so that when
        // clicking the heart adds or removes the selected station from the favourites
        MaterialCheckBox favouriteCheckBox = playerView.findViewById(R.id.favourite_button);
        favouriteCheckBox.setOnClickListener(v -> {
            BaseRadioStation station = playerViewModel.getSelectedRadioStation().getValue();
            if (station != null)
                radioViewModel.setFavourite(station, favouriteCheckBox.isChecked());
        });

        // Checks the heart if the selected station is one of the favourites
        Runnable refreshHeart = () -> {
            BaseRadioStation station = playerViewModel.getSelectedRadioStation().getValue();
            Set<UUID> favourites = radioViewModel.getFavouriteUUIDs().getValue();
            if (station != null && favourites != null)
                favouriteCheckBox.setChecked(favourites.contains(station.getUUID()));
        };

        // The heart must be updated both when another station is selected and
        // when the favourites change
        playerViewModel.getSelectedRadioStation().observe(this, s -> refreshHeart.run());
        radioViewModel.getFavouriteUUIDs().observe(this, f -> refreshHeart.run());
    }
    /**
     * Executed when the activity is stopped.
     * Disconnects from the {@link PlayerSessionService} and relases all utilized resources
     * to avoid memory leaks.
     */
    @Override
    protected void onStop() {
        if (future != null) {
            MediaController.releaseFuture(future);
            future = null;
        }
        playerView.setPlayer(null);
        radioViewModel.clear();
        super.onStop();
    }
}