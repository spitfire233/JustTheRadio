package com.justtheradio.source;

import static com.justtheradio.utils.constant.RadioBrowserConstants.ERROR_ENDPOINT_NOT_FOUND;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.justtheradio.exceptions.EndpointNotFoundException;
import com.justtheradio.source.callback.RadioCallback;
import com.justtheradio.utils.source.ServiceLocator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import de.sfuhrm.radiobrowser4j.AdvancedSearch;
import de.sfuhrm.radiobrowser4j.EndpointDiscovery;
import de.sfuhrm.radiobrowser4j.Paging;
import de.sfuhrm.radiobrowser4j.RadioBrowser;
import de.sfuhrm.radiobrowser4j.Station;

/**
 * Unit tests for {@link RadioBrowserRemoteDataSource}.
 * The endpoint discovery and the Radio Browser client are
 * created inside the class under test, so they are replaced with {@code mockConstruction}, and
 * the executor is replaced by one that runs tasks synchronously.
 */
public class RadioRemoteDataSourceUnitTest {
    /** Agent string passed to the data source. */
    private static final String AGENT = "JustTheRadio/test";
    /** Timeout passed to the data source. */
    private static final int TIMEOUT = 1000;
    /** Endpoint returned by the mocked discovery, unless a test replaces it. */
    private static final String ENDPOINT = "https://de1.api.radio-browser.info";

    /**
     * Describes how a test wants the mocked {@link RadioBrowser} client to behave. It is applied
     * to the client at the moment the data source builds it.
     */
    private interface ClientStub {
        /**
         * Stubs the given client.
         *
         * @param client The mocked client that was just built by the data source
         * @throws Throwable Because stubbing some of the client's methods throws checked
         *                   exceptions
         */
        void apply(RadioBrowser client) throws Throwable;
    }
    /** Mocked callback, used to verify which outcome the data source reports. */
    private RadioCallback callback;
    /** Executor that runs every task immediately, used to verify that work is scheduled. */
    private ExecutorService directExecutor;
    /** Endpoint that the mocked discovery returns; empty simulates no reachable server. */
    private Optional<String> discoveredEndpoint;
    /** Stubbing applied to the mocked client when it is built; {@code null} leaves defaults. */
    private ClientStub clientStub;
    /** Static mock replacing {@code ServiceLocator.getInstance}, closed after each test. */
    private MockedStatic<ServiceLocator> serviceLocatorStatic;
    /** Replaces every {@link EndpointDiscovery} the data source creates, closed after each test. */
    private MockedConstruction<EndpointDiscovery> discoveryConstruction;
    /** Replaces every {@link RadioBrowser} the data source creates, closed after each test. */
    private MockedConstruction<RadioBrowser> clientConstruction;
    /** The data source under test, built on the mocked dependencies. */
    private RadioBrowserRemoteDataSource source;

    /**
     * Builds the data source with the discovery, the client and the executor mocked, so that
     * every assertion can follow the call directly with no network and no background thread
     */
    @Before
    public void setUp() {
        callback = mock(RadioCallback.class);
        discoveredEndpoint = Optional.of(ENDPOINT);
        clientStub = null;

        // Executor that runs every task immediately on the calling thread.
        directExecutor = mock(ExecutorService.class);
        doAnswer(invocation -> {
            ((Runnable) invocation.getArgument(0)).run();
            return null;
        }).when(directExecutor).execute(any(Runnable.class));

        ServiceLocator locator = mock(ServiceLocator.class);
        when(locator.getExecutorService()).thenReturn(directExecutor);
        serviceLocatorStatic = Mockito.mockStatic(ServiceLocator.class);
        serviceLocatorStatic.when(ServiceLocator::getInstance).thenReturn(locator);

        discoveryConstruction = Mockito.mockConstruction(EndpointDiscovery.class,
                (mock, context) -> when(mock.discover()).thenReturn(discoveredEndpoint));
        clientConstruction = Mockito.mockConstruction(RadioBrowser.class, (mock, context) -> {
            if (clientStub != null)
                clientStub.apply(mock);
        });

        source = new RadioBrowserRemoteDataSource(AGENT, TIMEOUT);
        source.setCallback(callback);
    }
    /**
     * Static and construction mocks are bound to the thread that opened them: they must be
     * closed after each test, or they leak into the following ones
     */
    @After
    public void tearDown() {
        clientConstruction.close();
        discoveryConstruction.close();
        serviceLocatorStatic.close();
    }
    /**
     * Makes the endpoint discovery fail with the given error. The discovery is created by the
     * class under test, so the mock set up for it is replaced by one that throws
     *
     * @param error The error the discovery has to throw
     */
    private void failDiscoveryWith(IOException error) {
        discoveryConstruction.close();
        discoveryConstruction = Mockito.mockConstruction(EndpointDiscovery.class,
                (mock, context) -> when(mock.discover()).thenThrow(error));
    }
    /**
     * The codes must be published sorted alphabetically and with the malformed ones removed
     */
    @Test
    public void getCountryCodes_success_publishesSortedValidCodes() {
        Map<String, Integer> countries = new HashMap<>();
        countries.put("US", 10);
        countries.put("IT", 20);
        countries.put("FR", 30);
        countries.put("xx", 1);
        countries.put("ITA", 2);
        countries.put("I", 3);
        clientStub = client -> when(client.listCountryCodes()).thenReturn(countries);

        source.getCountryCodes();

        verify(callback).onCountryCodesFetchSuccess(Arrays.asList("FR", "IT", "US"));
        verify(callback, never()).onFailureFetchingCountryCodes(any());
    }
    /**
     * A network error while discovering the endpoint must reach the failure callback unchanged,
     * and the success callback must stay silent: here a connection that drops during the
     * discovery
     */
    @Test
    public void getCountryCodes_discoveryIoException_notifiesFailure() {
        IOException error = new IOException("connection reset");
        failDiscoveryWith(error);

        source.getCountryCodes();

        verify(callback).onFailureFetchingCountryCodes(error);
        verify(callback, never()).onCountryCodesFetchSuccess(any());
    }
    /**
     * When the discovery finds no server the failure callback must receive an
     * EndpointNotFoundException carrying the dedicated message: here a device with no
     * reachable Radio Browser server
     */
    @Test
    public void getCountryCodes_noEndpoint_notifiesEndpointNotFound() {
        discoveredEndpoint = Optional.empty();

        source.getCountryCodes();

        ArgumentCaptor<Exception> captor = ArgumentCaptor.forClass(Exception.class);
        verify(callback).onFailureFetchingCountryCodes(captor.capture());
        assertTrue(captor.getValue() instanceof EndpointNotFoundException);
        assertEquals(ERROR_ENDPOINT_NOT_FOUND, captor.getValue().getMessage());
        verify(callback, never()).onCountryCodesFetchSuccess(any());
    }
    /**
     * The request must be handed to the executor and not run on the caller's thread: here a
     * call that has to be scheduled exactly once
     */
    @Test
    public void getCountryCodes_runsOnExecutor() {
        source.getCountryCodes();

        verify(directExecutor).execute(any(Runnable.class));
    }
    /**
     * The client must be built once and reused by the following requests: here two consecutive
     * calls that must share a single discovery and a single client
     */
    @Test
    public void getCountryCodes_calledTwice_buildsClientOnce() {
        source.getCountryCodes();
        source.getCountryCodes();

        assertEquals(1, discoveryConstruction.constructed().size());
        assertEquals(1, clientConstruction.constructed().size());
        verify(callback, never()).onFailureFetchingCountryCodes(any());
    }
    /**
     * Without a query the search must cover the whole country, with no name filter, and skip
     * the broken stations:
     */
    @Test
    public void getRadioStationsByCountry_withoutQuery_returnsStationsFromClient()
            throws Exception {
        List<Station> stations = Collections.singletonList(mock(Station.class));
        clientStub = client -> when(client.listStationsWithAdvancedSearch(
                any(Paging.class), any(AdvancedSearch.class))).thenReturn(stations);

        List<Station> result = source.getRadioStationsByCountry("IT", 20, 20);

        assertSame(stations, result);
    }
    /**
     * With a query the stations must be returned exactly as the client provides them: here the
     * first page of a search for a name
     */
    @Test
    public void getRadioStationsByCountry_withQuery_returnsStationsFromClient() throws Exception {
        List<Station> stations = Collections.singletonList(mock(Station.class));
        clientStub = client -> when(client.listStationsWithAdvancedSearch(
                any(Paging.class), any(AdvancedSearch.class))).thenReturn(stations);

        List<Station> result = source.getRadioStationsByCountry("IT", "jazz", 0, 10);

        assertSame(stations, result);
    }
    /**
     * A network error while discovering the endpoint must be propagated to the caller
     * unchanged, since the method is blocking and has no callback: here a connection that
     * drops during the discovery
     */
    @Test
    public void getRadioStationsByCountry_discoveryIoException_propagates() {
        IOException error = new IOException("connection reset");
        failDiscoveryWith(error);

        IOException thrown = assertThrows(IOException.class,
                () -> source.getRadioStationsByCountry("IT", 0, 10));

        assertSame(error, thrown);
    }
    /**
     * When the discovery finds no server the call must throw an EndpointNotFoundException with
     * the dedicated message
     */
    @Test
    public void getRadioStationsByCountry_noEndpoint_throwsEndpointNotFound() {
        discoveredEndpoint = Optional.empty();

        EndpointNotFoundException thrown = assertThrows(EndpointNotFoundException.class,
                () -> source.getRadioStationsByCountry("IT", 0, 10));

        assertEquals(ERROR_ENDPOINT_NOT_FOUND, thrown.getMessage());
    }
}
