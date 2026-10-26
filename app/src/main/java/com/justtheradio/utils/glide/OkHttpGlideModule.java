package com.justtheradio.utils.glide;

import static com.justtheradio.utils.constant.Constants.BLOCKED_PRIVATE_ADDRESS_ERROR;
import static com.justtheradio.utils.constant.Constants.GLIDE_CONNECTION_TIMEOUT;
import static com.justtheradio.utils.constant.Constants.GLIDE_READ_CONNECTION_TIMEOUT;

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
import com.justtheradio.utils.security.SecurityUtils;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Dns;
import okhttp3.OkHttpClient;

@GlideModule
public class OkHttpGlideModule extends AppGlideModule {



    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .dns(SafeDns.getDNS())
                .addNetworkInterceptor(new ImageInterceptor())
                .followRedirects(true)
                .followSslRedirects(false)
                .connectTimeout(GLIDE_CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(GLIDE_READ_CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .build();
        registry.replace(GlideUrl.class, InputStream.class,
                new OkHttpUrlLoader.Factory(client));
    }

    @Override
    public boolean isManifestParsingEnabled() {
        return super.isManifestParsingEnabled();
    }
}
