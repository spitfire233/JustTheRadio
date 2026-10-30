package com.justtheradio.utils.network;

import static com.justtheradio.utils.constant.NetworkConstants.BLOCKED_URL_ERROR;

import androidx.annotation.NonNull;

import com.justtheradio.utils.security.SecurityUtils;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Response;
/**
 * Implementation of {@link Interceptor}.
 * OkHttp interceptor that protects the app when it streams radio stations, whose URLs come
 * from an external source and cannot be trusted.
 * It is meant to be added as a network interceptor, so it runs for every request actually
 * sent, including those that follow a redirect.
 */
public class StreamInterceptor implements Interceptor {
    /**
     * Checks the host of the request and, if it is accepted, lets the request proceed
     * unchanged.
     *
     * @param chain The chain of interceptors, used to proceed with the request
     * @return The response to the request, unchanged
     * @throws IOException If the host is blocked, or if the request fails
     */
    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        HttpUrl url = chain.request().url();
        if (SecurityUtils.isValidUrl(url.host(), false)) {
            throw new IOException(BLOCKED_URL_ERROR + url.host());
        }
        return chain.proceed(chain.request());
    }
}
