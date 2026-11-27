package com.justtheradio.utils.network;

import static com.justtheradio.utils.constant.NetworkConstants.BLOCKED_URL_ERROR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.justtheradio.utils.security.SecurityUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Unit tests for {@link StreamInterceptor}.
 * The requests and the responses are real OkHttp objects and the chain is
 * mocked, so no network is used. The validation of the host is mocked with {@code mockStatic}.
 */
@SuppressWarnings("resource")
public class StreamInterceptorUnitTest {
    /** Host used by the tests. */
    private static final String HOST = "example.com";
    /** Address of a stream on the host above, used by the tests that need a request. */
    private static final String STREAM_URL = "https://example.com/stream";
    /** Mocked chain, which hands the request to the interceptor and returns the response. */
    private Interceptor.Chain chain;
    /** Static mock replacing the validation of the host, closed after each test. */
    private MockedStatic<SecurityUtils> securityStatic;
    /** The interceptor under test. */
    private StreamInterceptor interceptor;

    /**
     * Builds the interceptor and a chain, with the host validation replaced by a mock
     */
    @Before
    public void setUp() {
        chain = mock(Interceptor.Chain.class);
        securityStatic = Mockito.mockStatic(SecurityUtils.class);
        interceptor = new StreamInterceptor();
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
     * Builds a request for the given address.
     *
     * @param url The address of the request
     * @return The request
     */
    private static Request request(String url) {
        return new Request.Builder().url(url).build();
    }

    /**
     * Builds a response with no body.
     *
     * @param request The request the response answers
     * @return The response
     */
    private static Response response(Request request) {
        return new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .build();
    }

    /**
     * Makes the host validation answer the same for the host used by the tests.
     *
     * @param local The answer of the validation
     */
    private void hostIsLocal(boolean local) {
        securityStatic.when(() -> SecurityUtils.isLocalHost(HOST)).thenReturn(local);
    }

    /**
     * A request to an accepted host must proceed unchanged and its response must be returned
     * as it is
     */
    @Test
    public void intercept_validHost_proceedsAndReturnsResponseUnchanged() throws Exception {
        Request request = request(STREAM_URL);
        Response response = response(request);
        hostIsLocal(false);
        when(chain.request()).thenReturn(request);
        when(chain.proceed(request)).thenReturn(response);

        Response result = interceptor.intercept(chain);

        assertSame(response, result);
        verify(chain).proceed(request);
    }
    /**
     * Only the host decides, so a request over plain HTTP to an accepted host must not be
     * rejected by this interceptor, since radio streams are often served without TLS
     */
    @Test
    public void intercept_validHostOverHttp_isNotRejected() throws Exception {
        Request request = request("http://example.com/stream");
        Response response = response(request);
        hostIsLocal(false);
        when(chain.request()).thenReturn(request);
        when(chain.proceed(request)).thenReturn(response);

        assertSame(response, interceptor.intercept(chain));
    }
    /**
     * A request to a rejected host must be blocked before it is sent, with an error that names
     * the host
     */
    @Test
    public void intercept_localHost_isBlockedBeforeProceeding() throws Exception {
        hostIsLocal(true);
        when(chain.request()).thenReturn(request(STREAM_URL));

        IOException error = assertThrows(IOException.class, () -> interceptor.intercept(chain));

        assertEquals(BLOCKED_URL_ERROR + HOST, error.getMessage());
        verify(chain, never()).proceed(any());
    }
    /**
     * Only the host of the request must be validated, without the port, the path or the query,
     * and with the flag that this interceptor always passes as false
     */
    @Test
    public void intercept_checksOnlyTheHost() throws Exception {
        Request request = request("https://example.com:8443/stream?quality=high");
        hostIsLocal(false);
        when(chain.request()).thenReturn(request);
        when(chain.proceed(request)).thenReturn(response(request));

        interceptor.intercept(chain);

        securityStatic.verify(() -> SecurityUtils.isLocalHost(HOST));
    }
    /**
     * A failure of the request itself must reach the caller unchanged, since the interceptor
     * only checks the host and does not handle network errors
     */
    @Test
    public void intercept_requestFails_propagatesException() throws Exception {
        Request request = request(STREAM_URL);
        IOException error = new IOException("connection reset");
        hostIsLocal(false);
        when(chain.request()).thenReturn(request);
        when(chain.proceed(request)).thenThrow(error);

        IOException thrown = assertThrows(IOException.class, () -> interceptor.intercept(chain));

        assertSame(error, thrown);
    }
}
