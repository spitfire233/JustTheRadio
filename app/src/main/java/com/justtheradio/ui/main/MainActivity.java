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
import androidx.media3.common.util.NonNullApi;
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

public class MainActivity extends AppCompatActivity {

    private PlayerViewModel playerViewModel;
    private RadioViewModel radioViewModel;
    private ListenableFuture<MediaController> future;
    private PlayerView playerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.main_fragment_container_view);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();
        BottomNavigationView bottomNavigationView = findViewById(R.id.menu_bottom_navigation_view);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        bottomNavigationView.setOnItemReselectedListener(item ->
                navController.popBackStack(item.getItemId(), false));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        playerViewModel = new ViewModelProvider(
                this,
                new PlayerViewModelFactory()
        ).get(PlayerViewModel.class);

        RadioRepository radioRepository = ServiceLocator.getInstance().getRadioRepository(getApplication());

        radioViewModel = new ViewModelProvider(
                this,
                new RadioViewModelFactory(radioRepository)
        ).get(RadioViewModel.class);

        playerView = findViewById(R.id.player_view);
        MaterialCheckBox favouriteCheckBox = playerView.findViewById(R.id.favourite_button);

        favouriteCheckBox.setOnClickListener(v -> {
            BaseRadioStation station = playerViewModel.getSelectedRadioStation().getValue();
            if (station != null)
                radioViewModel.setFavourite(station, favouriteCheckBox.isChecked());
        });

        Runnable refreshHeart = () -> {
            BaseRadioStation station = playerViewModel.getSelectedRadioStation().getValue();
            Set<UUID> favourites = radioViewModel.getFavouriteUUIDs().getValue();
            if (station != null && favourites != null)
                favouriteCheckBox.setChecked(favourites.contains(station.getUUID()));
        };

        playerViewModel.getSelectedRadioStation().observe(this, s -> refreshHeart.run());
        radioViewModel.getFavouriteUUIDs().observe(this, f -> refreshHeart.run());

        radioViewModel.getSavingResultLiveData().observe(this, result -> {
            if (result.isSuccess()) {
                Result.Success<BaseRadioStation> success = ((Result.Success) result);
                BaseRadioStation station = success.getData();
                String message = station instanceof FavouriteStation
                        ? String.format(getResources().getString(R.string.successfully_saved_radio_station), station.getName())
                        : String.format(getResources().getString(R.string.successfully_removed_radio_station), station.getName());
                Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
                toast.show();
            } else {
                String message =
                        getResources().getString(R.string.error_while_modifying_station_saved_state)
                        + ":" +((Result.Failure) result).getMessage();
                Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }
    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onStart() {
        super.onStart();
        SessionToken sessionToken = new SessionToken(
                getApplicationContext(),
                new ComponentName(getApplicationContext(), PlayerSessionService.class)
        );

        future = new MediaController.Builder(
                getApplicationContext(), sessionToken
        ).buildAsync();

        future.addListener(() -> {
            try {
                MediaController controller = future.get();
                playerView.setPlayer(controller);
                playerViewModel.getSelectedRadioStation().observe(this, station -> {
                    TextView textView = findViewById(R.id.player_station_name_text_view);
                    textView.setText(station.getName());
                    textView.setSelected(true);
                    ImageView imageView = findViewById(R.id.player_radio_image_view);
                    Glide.with(this)
                            .load(station.getFaviconURL())
                            .placeholder(R.drawable.justtheradio_logo)
                            .into(imageView);
                    playerView.setVisibility(View.VISIBLE);
                    playerView.showController();
                    MediaItem item = MediaItem.fromUri(station.getURL());
                    controller.setMediaItem(item);
                    controller.prepare();
                    controller.play();
                });
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, MoreExecutors.directExecutor());
    }

    @Override
    protected void onStop() {
        if (future != null) {
            MediaController.releaseFuture(future);
            future = null;
        }
        playerView.setPlayer(null);
        super.onStop();
    }
}