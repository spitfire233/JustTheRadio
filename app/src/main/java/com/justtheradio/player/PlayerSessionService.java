package com.justtheradio.player;

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

public class PlayerSessionService extends MediaSessionService {
    private MediaSession session = null;
    boolean pausedSinceLastPlay = false;

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onCreate() {
        super.onCreate();
        OkHttpClient streamClient = new OkHttpClient.Builder()
                .dns(SafeDns.getDNS())
                .addNetworkInterceptor(new StreamInterceptor())
                .followSslRedirects(false)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(
                this, new OkHttpDataSource.Factory(streamClient)
        );
        ExoPlayer exoPlayer = new ExoPlayer.Builder(this)
                .setMediaSourceFactory(new DefaultMediaSourceFactory(dataSourceFactory))
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(C.USAGE_MEDIA)
                        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                        .build(), true)
                .setHandleAudioBecomingNoisy(true)
                .setWakeMode(C.WAKE_MODE_NETWORK)
                .build();

        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
                if (!playWhenReady)
                    pausedSinceLastPlay = true;
            }
        });

        Player livePlayer = new ForwardingPlayer(exoPlayer) {
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

    @Nullable
    @Override
    public MediaSession onGetSession(@NonNull MediaSession.ControllerInfo controllerInfo) {
        return session;
    }

    @Override
    public void onDestroy() {
        session.getPlayer().release();
        session.release();
        session = null;
        super.onDestroy();
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onTaskRemoved(@Nullable Intent rootIntent) {
        pauseAllPlayersAndStopSelf();
    }
}
