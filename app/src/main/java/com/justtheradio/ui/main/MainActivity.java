package com.justtheradio.ui.main;

import android.content.ComponentName;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.justtheradio.R;
import com.justtheradio.player.PlayerSessionService;
import com.justtheradio.ui.viewmodel.player.PlayerViewModel;
import com.justtheradio.ui.viewmodel.player.PlayerViewModelFactory;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    PlayerViewModel playerViewModel;
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
                playerView = findViewById(R.id.player_view);
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