package com.justtheradio.utils.network;

import static com.justtheradio.utils.constant.NetworkConstants.BLOCKED_URL_ERROR;
import static com.justtheradio.utils.constant.NetworkConstants.IMAGE_TOO_LARGE_ERROR;
import static com.justtheradio.utils.constant.NetworkConstants.MAX_IMAGE_BYTES;
import static com.justtheradio.utils.constant.NetworkConstants.NOT_AN_IMAGE_ERROR;

import androidx.annotation.NonNull;

import com.justtheradio.utils.constant.NetworkConstants;
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

/**
 * Implementation of {@link Interceptor}.
 * OkHttp interceptor that protects the app when it downloads images, such as the logos of
 * the radio stations, whose URLs come from an external source and cannot be trusted.
 * It is meant to be added as a network interceptor, so it runs for every request actually
 * sent, including those that follow a redirect.
 */
public class ImageInterceptor implements Interceptor {

    /**
     * Checks the request and the response of an image download.
     * <ol>
     *   <li>The URL must use HTTPS and its host must not be the local host, otherwise the
     *       request is blocked before it is sent.</li>
     *   <li>Redirects and unsuccessful responses are returned unchanged, as they do not
     *       contain an image (a redirect is checked when the next request is sent).</li>
     *   <li>The content type must be an image.</li>
     *   <li>The declared length of the body must not exceed the maximum size.</li>
     *   <li>The body is wrapped so that its size is also limited while it is read.</li>
     * </ol>
     *
     * @param chain The chain of interceptors, used to proceed with the request
     * @return The response, with a body that cannot exceed the maximum size
     * @throws IOException If the URL is blocked, the content is not an image, or it is too
     *                     large, or if the request fails
     */
    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        HttpUrl url = chain.request().url();
        // Only HTTPS is allowed and the local host is blocked
        if (!url.isHttps() || SecurityUtils.isLocalHost(url.host()))
            throw new IOException(BLOCKED_URL_ERROR + url.host());

        Response response = chain.proceed(chain.request());
        ResponseBody responseBody = response.body();

        // Nothing to check in a redirect (the next request will be checked) or in an error
        if (response.isRedirect() || !response.isSuccessful())
            return response;

        // The content must be an image
        MediaType type = responseBody.contentType();
        if (type == null || !type.type().equals("image")) {
            response.close();
            throw new IOException(NOT_AN_IMAGE_ERROR + type);
        }

        // Reject early the images that declare a size over the limit (-1 if not declared)
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
    /**
     * Source that counts the bytes read from the body of a response and fails as soon as
     * they exceed the maximum size of an image, so that an oversized body is never read.
     */
    public static final class LimitedSource extends ForwardingSource {
        /** Number of bytes read so far. */
        private long total;
        /**
         * Constructor for the {@link LimitedSource} class.
         *
         * @param delegate Delegate the source of the body to limit
         */
        public LimitedSource(@NonNull Source delegate) {
            super(delegate);
        }
        /**
         * Reads from the underlying source and adds the bytes read to the total.
         *
         *
         * @param sink          The buffer where the bytes are read into
         * @param byteCount     The maximum number of bytes to read
         * @return              The number of bytes read, or -1 if the source is exhausted
         * @throws IOException  If reading fails or the total exceeds the maximum size of an image
         */
        @Override
        public long read(@NonNull Buffer sink, long byteCount) throws IOException {
            long n = super.read(sink, byteCount);
            if (n > 0) {
                total += n;
                if (total > MAX_IMAGE_BYTES)
                    throw new IOException(NetworkConstants.IMAGE_TOO_LARGE_ERROR + n);
            }
            return n;
        }
    }
}
