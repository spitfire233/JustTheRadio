package com.justtheradio.utils.network;

import static com.justtheradio.utils.constant.Constants.BLOCKED_URL_ERROR;

import androidx.annotation.NonNull;

import com.justtheradio.utils.security.SecurityUtils;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Response;

public class StreamInterceptor implements Interceptor {

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
