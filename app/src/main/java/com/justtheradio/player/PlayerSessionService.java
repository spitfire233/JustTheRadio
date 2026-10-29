package com.justtheradio.player;

import static com.justtheradio.utils.constant.Constants.OKHTTP_CONNECTION_TIMEOUT;
import static com.justtheradio.utils.constant.Constants.OKHTTP_READ_TIMEOUT;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.ForwardingPlayer;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.datasource.okhttp.OkHttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;

import com.justtheradio.utils.network.SafeDns;
import com.justtheradio.utils.network.StreamInterceptor;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * Implementation of {@link MediaSessionService} that owns the audio player of the app.
 * Since the app plays live radio streams, resuming after a pause must not continue from
 * the old buffered audio. For this reason the {@link ExoPlayer} is wrapped in a
 * {@link ForwardingPlayer} that reloads the stream when playback is resumed, so that the
 * listener hears the live broadcast again.
 */
public class PlayerSessionService extends MediaSessionService {
    /**
     * Session exposing the player to the system and to media controllers
     */
    private MediaSession session = null;
    /**
     * {@code true} if playback has been paused since the last time it was started.
     */
    boolean pausedSinceLastPlay = false;
    /**
     * Creates the {@link OkHttpClient} network client, the {@link ExoPlayer} player
     * and the {@link MediaSession}.
     * <ul>
     *   <li>
     *       The {@link OkHttpClient} used to download the streams uses a custom DNS
     *       ({@link SafeDns}), a network interceptor ({@link StreamInterceptor}) and does not
     *       follow redirects from HTTPS to another protocol
     *   </li>
     *   <li>
     *       The {@link ExoPlayer} handles audio focus automatically
     *   </li>
     *   <li>
     *       A listener records when the playback is paused and a {@link ForwardingPlayer}
     *       reloads the stream when it is resumed (see {@link #pausedSinceLastPlay}).
     *   </li>
     * </ul>
     */
    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onCreate() {
        super.onCreate();
        // Create the network client
        OkHttpClient streamClient = new OkHttpClient.Builder()
                .dns(SafeDns.getDNS()) // Use custom DNS rules
                .addNetworkInterceptor(new StreamInterceptor()) // Use custom network interceptor
                .followSslRedirects(false) // Do not follo SSL redirects
                .connectTimeout(OKHTTP_CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(OKHTTP_READ_TIMEOUT, TimeUnit.SECONDS)
                .build();

        // Create data source for the ExoPlayer with the http client
        DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(
                this, new OkHttpDataSource.Factory(streamClient)
        );

        // Build the exo player object
        ExoPlayer exoPlayer = new ExoPlayer.Builder(this)
                .setMediaSourceFactory(new DefaultMediaSourceFactory(dataSourceFactory))
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(C.USAGE_MEDIA) // Sets the intended use as media
                        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC) // Sets the played content type to audio
                        .build(), true) // True allows to handle automatically audio focus
                .setHandleAudioBecomingNoisy(true) // Pause when the output becomes noisy (e.g. headphones unplugged)
                .setWakeMode(C.WAKE_MODE_NETWORK) // The player continues reproduction even if the screen is off
                .build();

        // Remember that the playback has been paused, so that the next play() reloads the stream
        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
                if (!playWhenReady)
                    pausedSinceLastPlay = true;
            }
        });

        // Wrap around the real player to make the resume jump to the live stream
        Player livePlayer = new ForwardingPlayer(exoPlayer) {
            /**
             * Handles play functionality of the player.
             * If the playback was paused, reloads the current stream from the start
             * (which, for a live stream, means the current live audio) before playing.
             */
            @Override
            public void play() {
                MediaItem item = getCurrentMediaItem();
                if (item != null && pausedSinceLastPlay) {
                    pausedSinceLastPlay = false;
                    setMediaItem(item, true);
                    prepare();
                }
                super.play();
            }
            /**
             * Redirects "play" requests to {@link #play()}, so that requests coming from
             * controllers using this method also reload the stream after a pause.
             * @param playWhenReady Whether playback should proceed when ready.
             */
            @Override
            public void setPlayWhenReady(boolean playWhenReady) {
                if (playWhenReady)
                    play();
                else
                    super.setPlayWhenReady(false);
            }
        };
        session = new MediaSession.Builder(this, livePlayer).build();
    }
    /**
     * Called when a controller connects to the service.
     * @param controllerInfo The information of the controller that is trying to connect.
     * @return The {@link MediaSession}, shared by all controllers
     */
    @Nullable
    @Override
    public MediaSession onGetSession(@NonNull MediaSession.ControllerInfo controllerInfo) {
        return session;
    }
    /**
     * Releases the player and the session when the service is destroyed, so that the audio
     * resources are freed.
     */
    @Override
    public void onDestroy() {
        session.getPlayer().release();
        session.release();
        session = null;
        super.onDestroy();
    }
    /**
     * Called when the user swipes the app away from the recent apps list.
     * Pauses the playback and stops the service, so that the radio does not keep playing
     * or leave a notification after the app has been closed.
     *
     * @param rootIntent The original root Intent that was used to launch
     * the task that is being removed.
     */
    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onTaskRemoved(@Nullable Intent rootIntent) {
        pauseAllPlayersAndStopSelf();
    }
}
