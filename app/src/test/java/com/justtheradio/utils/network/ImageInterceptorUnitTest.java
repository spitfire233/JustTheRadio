package com.justtheradio.utils.network;


import static com.justtheradio.utils.constant.NetworkConstants.BLOCKED_URL_ERROR;
import static com.justtheradio.utils.constant.NetworkConstants.IMAGE_TOO_LARGE_ERROR;
import static com.justtheradio.utils.constant.NetworkConstants.MAX_IMAGE_BYTES;
import static com.justtheradio.utils.constant.NetworkConstants.NOT_AN_IMAGE_ERROR;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.annotation.NonNull;

import com.justtheradio.utils.security.SecurityUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.Okio;
import okio.Source;
import okio.Timeout;

/**
 * Unit tests for {@link ImageInterceptor} and its {@link ImageInterceptor.LimitedSource}.
 * The requests and the responses are real OkHttp objects and the chain is
 * mocked, so no network is used. The check on the local host is mocked with {@code mockStatic}.
 */
@SuppressWarnings("resource")
public class ImageInterceptorUnitTest {

    /** Maximum size of an image, copied from the constants. */
    private static final long MAX = MAX_IMAGE_BYTES;
    /** Size of the chunks used to read the bodies. */
    private static final long CHUNK = 8192;
    /** Mocked chain, which hands the request to the interceptor and returns the response. */
    private Interceptor.Chain chain;
    /** Static mock replacing the local host check, closed after each test. */
    private MockedStatic<SecurityUtils> securityStatic;
    /** The interceptor under test. */
    private ImageInterceptor interceptor;

    /**
     * Builds the interceptor and a chain, with the local host check answering that no host is
     * local
     */
    @Before
    public void setUp() {
        chain = mock(Interceptor.Chain.class);
        securityStatic = Mockito.mockStatic(SecurityUtils.class);
        interceptor = new ImageInterceptor();
    }

    /**
     * The static mock is bound to the thread that opened it: it must be closed after each
     * test, or it leaks into the following ones
     */
    @After
    public void tearDown() {
        securityStatic.close();
    }

    /**
     * Source that produces a given number of zero bytes and then ends. The bytes are created
     * a chunk at a time, so a body as large as the limit costs no memory.
     */
    private static final class ZerosSource implements Source {
        /** Number of bytes still to produce. */
        private long remaining;

        /**
         * Constructor for the {@link ZerosSource} class.
         *
         * @param total The number of bytes the source produces before ending
         */
        ZerosSource(long total) {
            this.remaining = total;
        }

        @Override
        public long read(@NonNull Buffer sink, long byteCount) {
            if (remaining == 0)
                return -1;
            long n = Math.min(byteCount, remaining);
            sink.write(new byte[(int) n]);
            remaining -= n;
            return n;
        }

        @NonNull
        @Override
        public Timeout timeout() {
            return Timeout.NONE;
        }

        @Override
        public void close() { }
    }

    /**
     * Builds a request for the given address.
     *
     * @param url The address of the request
     * @return The request
     */
    private static Request request(String url) {
        return new Request.Builder().url(url).build();
    }

    /**
     * Builds a response around a body.
     *
     * @param request The request the response answers
     * @param code    The status code of the response
     * @param body    The body of the response
     * @return The response
     */
    private static Response response(Request request, int code, ResponseBody body) {
        return new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message("message")
                .body(body)
                .build();
    }

    /**
     * Builds a response body that reads from a source.
     *
     * @param source      The source of the body
     * @param contentType The content type of the body, or {@code null} for none
     * @param declared    The declared length of the body, or -1 if not declared
     * @return The body
     */
    private static ResponseBody body(Source source, String contentType, long declared) {
        MediaType type = contentType == null ? null : MediaType.parse(contentType);
        return ResponseBody.create(Okio.buffer(source), type, declared);
    }

    /**
     * Reads a source until it ends and counts the bytes, discarding them.
     *
     * @param source The source to read
     * @return The number of bytes read
     * @throws IOException If reading fails
     */
    private static long drain(Source source) throws IOException {
        Buffer sink = new Buffer();
        long total = 0;
        long n;
        while ((n = source.read(sink, CHUNK)) != -1) {
            total += n;
            sink.clear();
        }
        return total;
    }

    /**
     * Makes the chain return the given response for the given request.
     *
     * @param request  The request the chain holds
     * @param response The response the chain returns when the request proceeds
     * @throws IOException Never, it only declares the exception to the chain
     */
    private void chainReturns(Request request, Response response) throws IOException {
        when(chain.request()).thenReturn(request);
        when(chain.proceed(request)).thenReturn(response);
    }

    /**
     * A request that does not use HTTPS must be blocked before it is sent, with an error that
     * names the host
     */
    @Test
    public void intercept_httpUrl_isBlockedBeforeProceeding() throws Exception {
        when(chain.request()).thenReturn(request("http://example.com/logo.png"));

        IOException error = assertThrows(IOException.class, () -> interceptor.intercept(chain));

        assertEquals(BLOCKED_URL_ERROR + "example.com", error.getMessage());
        verify(chain, never()).proceed(any());
    }
    /**
     * A request to the local host must be blocked before it is sent, even when it uses HTTPS,
     * with an error that names the host
     */
    @Test
    public void intercept_localHost_isBlockedBeforeProceeding() throws Exception {
        securityStatic.when(() -> SecurityUtils.isLocalHost("localhost")).thenReturn(true);
        when(chain.request()).thenReturn(request("https://localhost/logo.png"));

        IOException error = assertThrows(IOException.class, () -> interceptor.intercept(chain));

        assertEquals(BLOCKED_URL_ERROR + "localhost", error.getMessage());
        verify(chain, never()).proceed(any());
    }
    /**
     * A redirect must be returned unchanged even if it is not an image, since the request that
     * follows it will be checked on its own
     */
    @Test
    public void intercept_redirect_isReturnedUnchanged() throws Exception {
        Request request = request("https://example.com/logo.png");
        Response redirect = response(request, 302,
                body(new ZerosSource(0), "text/html", -1));
        chainReturns(request, redirect);

        assertSame(redirect, interceptor.intercept(chain));
    }
    /**
     * An unsuccessful response must be returned unchanged even if it is not an image, since it
     * does not carry one
     */
    @Test
    public void intercept_errorResponse_isReturnedUnchanged() throws Exception {
        Request request = request("https://example.com/logo.png");
        Response notFound = response(request, 404,
                body(new ZerosSource(0), "text/html", -1));
        chainReturns(request, notFound);

        assertSame(notFound, interceptor.intercept(chain));
    }
    /**
     * A valid image must pass through with its status, its content type, its declared length
     * and its bytes untouched, and the original request must be the one that proceeds
     */
    @Test
    public void intercept_validImage_passesThroughUnchanged() throws Exception {
        Request request = request("https://example.com/logo.png");
        byte[] content = {1, 2, 3, 4, 5};
        Response original = response(request, 200, ResponseBody.create(
                new Buffer().write(content), MediaType.parse("image/png"), content.length));
        chainReturns(request, original);

        Response result = interceptor.intercept(chain);

        assertEquals(200, result.code());
        assertEquals(MediaType.parse("image/png"), result.body().contentType());
        assertEquals(content.length, result.body().contentLength());
        assertArrayEquals(content, result.body().bytes());
        verify(chain).proceed(request);
    }
    /**
     * An image whose length is not declared must be accepted, since the limit is also enforced
     * while the body is read
     */
    @Test
    public void intercept_undeclaredLength_isAccepted() throws Exception {
        Request request = request("https://example.com/logo.png");
        chainReturns(request, response(request, 200,
                body(new ZerosSource(10), "image/png", -1)));

        Response result = interceptor.intercept(chain);

        assertEquals(10, drain(result.body().source()));
    }
    /**
     * An image that declares exactly the maximum size must be accepted, since only a size over
     * the limit is rejected
     */
    @Test
    public void intercept_declaredLengthAtLimit_isAccepted() throws Exception {
        Request request = request("https://example.com/logo.png");
        chainReturns(request, response(request, 200,
                body(new ZerosSource(0), "image/png", MAX)));

        Response result = interceptor.intercept(chain);

        assertEquals(MAX, result.body().contentLength());
    }
    /**
     * A response whose content type is not an image must be rejected with an error that names
     * the type, and the response must be closed so that the connection is released
     */
    @Test
    public void intercept_notAnImage_throwsAndClosesResponse() throws Exception {
        Request request = request("https://example.com/logo.png");
        Source source = mock(Source.class);
        chainReturns(request, response(request, 200, body(source, "text/html", 10)));

        IOException error = assertThrows(IOException.class, () -> interceptor.intercept(chain));

        assertEquals(NOT_AN_IMAGE_ERROR + "text/html", error.getMessage());
        verify(source).close();
    }
    /**
     * A response with no content type must be rejected as not being an image, and the response
     * must be closed
     */
    @Test
    public void intercept_noContentType_throwsAndClosesResponse() throws Exception {
        Request request = request("https://example.com/logo.png");
        Source source = mock(Source.class);
        chainReturns(request, response(request, 200, body(source, null, 10)));

        IOException error = assertThrows(IOException.class, () -> interceptor.intercept(chain));

        assertEquals(NOT_AN_IMAGE_ERROR + "null", error.getMessage());
        verify(source).close();
    }
    /**
     * An image that declares a size over the limit must be rejected before any byte is read,
     * with an error that reports the declared size, and the response must be closed
     */
    @Test
    public void intercept_declaredLengthOverLimit_throwsAndClosesResponse() throws Exception {
        Request request = request("https://example.com/logo.png");
        Source source = mock(Source.class);
        chainReturns(request, response(request, 200, body(source, "image/png", MAX + 1)));

        IOException error = assertThrows(IOException.class, () -> interceptor.intercept(chain));

        assertEquals(IMAGE_TOO_LARGE_ERROR + (MAX + 1), error.getMessage());
        verify(source).close();
    }
    /**
     * A body that does not declare its length must fail while it is read once it exceeds the
     * maximum size, so an oversized body is never read in full
     */
    @Test
    public void intercept_undeclaredLengthOverLimit_failsWhileReading() throws Exception {
        Request request = request("https://example.com/logo.png");
        chainReturns(request, response(request, 200,
                body(new ZerosSource(MAX + 1), "image/png", -1)));

        Response result = interceptor.intercept(chain);

        assertThrows(IOException.class, () -> drain(result.body().source()));
    }
    /**
     * A body that declares a small length but is larger must fail while it is read once it
     * exceeds the maximum size, so a lying server cannot bypass the limit
     */
    @Test
    public void intercept_lyingLengthOverLimit_failsWhileReading() throws Exception {
        Request request = request("https://example.com/logo.png");
        chainReturns(request, response(request, 200,
                body(new ZerosSource(MAX + 1), "image/png", 10)));

        Response result = interceptor.intercept(chain);

        assertThrows(IOException.class, () -> drain(result.body().source()));
    }
    /**
     * A source that produces exactly the maximum size must be read in full without an error,
     * since only more than the limit is rejected
     */
    @Test
    public void limitedSource_exactlyMaxBytes_isReadInFull() throws Exception {
        ImageInterceptor.LimitedSource limited =
                new ImageInterceptor.LimitedSource(new ZerosSource(MAX));

        assertEquals(MAX, drain(limited));
    }
    /**
     * A source that produces one byte more than the maximum size must fail with the dedicated
     * error, even though each single read is small, because the bytes are counted across reads
     */
    @Test
    public void limitedSource_oneByteOverMax_throwsTooLargeError() {
        ImageInterceptor.LimitedSource limited =
                new ImageInterceptor.LimitedSource(new ZerosSource(MAX + 1));

        IOException error = assertThrows(IOException.class, () -> drain(limited));

        assertTrue(Objects.requireNonNull(error.getMessage()).startsWith(IMAGE_TOO_LARGE_ERROR));
    }
    /**
     * An exhausted source must report the end with -1 and not fail, so an empty body is read
     * normally
     */
    @Test
    public void limitedSource_exhaustedSource_returnsMinusOne() throws Exception {
        ImageInterceptor.LimitedSource limited =
                new ImageInterceptor.LimitedSource(new ZerosSource(0));

        assertEquals(-1, limited.read(new Buffer(), CHUNK));
    }
}
