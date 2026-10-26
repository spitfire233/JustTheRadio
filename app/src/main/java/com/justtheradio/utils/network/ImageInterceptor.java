package com.justtheradio.utils.network;

import static com.justtheradio.utils.constant.Constants.BLOCKED_URL_ERROR;
import static com.justtheradio.utils.constant.Constants.IMAGE_TOO_LARGE_ERROR;
import static com.justtheradio.utils.constant.Constants.MAX_IMAGE_BYTES;
import static com.justtheradio.utils.constant.Constants.NOT_AN_IMAGE_ERROR;

import androidx.annotation.NonNull;

import com.justtheradio.utils.constant.Constants;
import com.justtheradio.utils.security.SecurityUtils;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

public class ImageInterceptor implements Interceptor {
    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        HttpUrl url = chain.request().url();
        if (!url.isHttps() || SecurityUtils.isLocalHost(url.host()))
            throw new IOException(BLOCKED_URL_ERROR + url.host());

        Response response = chain.proceed(chain.request());
        ResponseBody responseBody = response.body();

        if (response.isRedirect() || !response.isSuccessful())
            return response;

        MediaType type = responseBody.contentType();
        if (type == null || !type.type().equals("image")) {
            response.close();
            throw new IOException(NOT_AN_IMAGE_ERROR + type);
        }

        long declared = responseBody.contentLength();
        if (declared > MAX_IMAGE_BYTES) {
            response.close();
            throw new IOException(IMAGE_TOO_LARGE_ERROR + declared);
        }

        // Also cap the body when Content-Length is missing or lies
        ResponseBody limited = ResponseBody.create(
                Okio.buffer(new LimitedSource(responseBody.source())), type, declared
        );

        return response.newBuilder().body(limited).build();

    }

    public static final class LimitedSource extends ForwardingSource {
        private long total;

        public LimitedSource(@NonNull Source delegate) {
            super(delegate);
        }

        @Override
        public long read(@NonNull Buffer sink, long byteCount) throws IOException {
            long n = super.read(sink, byteCount);
            if (n > 0) {
                total += n;
                if (total > MAX_IMAGE_BYTES)
                    throw new IOException(Constants.IMAGE_TOO_LARGE_ERROR + n);
            }
            return n;
        }
    }

}
