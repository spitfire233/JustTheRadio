package com.justtheradio.utils.glide;

import static com.justtheradio.utils.constant.NetworkConstants.GLIDE_CONNECTION_TIMEOUT;
import static com.justtheradio.utils.constant.NetworkConstants.GLIDE_READ_CONNECTION_TIMEOUT;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;
import com.justtheradio.utils.network.ImageInterceptor;
import com.justtheradio.utils.network.SafeDns;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * Glide module that makes Glide download the images, such as the logos of the radio
 * stations, with a custom OkHttp client instead of its default connection.
 */
@GlideModule
public class OkHttpGlideModule extends AppGlideModule {
    /**
     * Replaces the component Glide uses to load images from URLs with one based on the
     * custom OkHttp client.
     * The client resolves host names with {@link SafeDns}, runs the {@link ImageInterceptor}
     * on the network requests, follows redirects but not those from HTTPS to another
     * protocol, and has connected and read timeouts (in seconds).
     *
     * @param context An Application {@link android.content.Context}.
     * @param glide The Glide singleton that is in the process of being initialized.
     * @param registry An {@link com.bumptech.glide.Registry} to use to register components.
     */
    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide,
                                   @NonNull Registry registry) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .dns(SafeDns.getDNS())
                .addNetworkInterceptor(new ImageInterceptor())
                .followRedirects(true)
                .followSslRedirects(false)
                .connectTimeout(GLIDE_CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(GLIDE_READ_CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .build();
        // Use the client above to load the images from URLs, in place of the default loader
        registry.replace(GlideUrl.class, InputStream.class,
                new OkHttpUrlLoader.Factory(client));
    }
    /**
     * Tells Glide whether it must also look for modules declared in the manifest.
     *
     * @return The default value of {@link AppGlideModule#isManifestParsingEnabled()}
     */
    @Override
    public boolean isManifestParsingEnabled() {
        return super.isManifestParsingEnabled();
    }
}
