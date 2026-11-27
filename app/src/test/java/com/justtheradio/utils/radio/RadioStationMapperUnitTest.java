package com.justtheradio.utils.radio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import com.justtheradio.model.radio.RadioStation;
import com.justtheradio.utils.security.SecurityUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.UUID;

import de.sfuhrm.radiobrowser4j.Station;


/**
 * Unit tests for {@link RadioStationMapper}.
 * The stations of the API are mocked and the validation of the URLs is mocked
 * with {@code mockStatic}, so each test decides which URLs are valid. A URL that a test does
 * not declare as valid is invalid.
 */
public class RadioStationMapperUnitTest {

    /** Resolved stream URL of the stations built by the tests. */
    private static final String RESOLVED = "https://stream.example.com/resolved";
    /** Original stream URL of the stations built by the tests. */
    private static final String UNRESOLVED = "http://stream.example.com/original";
    /** Favicon URL of the stations built by the tests. */
    private static final String FAVICON = "https://example.com/logo.png";
    /** Name of the stations built by the tests. */
    private static final String NAME = "Radio Test";
    /** Identifier of the stations built by the tests. */
    private static final UUID STATION_UUID =
            UUID.fromString("11111111-2222-3333-4444-555555555555");

    /** Static mock replacing the validation of the URLs, closed after each test. */
    private MockedStatic<SecurityUtils> securityStatic;

    /**
     * Replaces the validation of the URLs with a mock that answers that no URL is valid,
     * unless a test says otherwise
     */
    @Before
    public void setUp() {
        securityStatic = Mockito.mockStatic(SecurityUtils.class);
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
     * Builds a mocked station of the API.
     *
     * @param resolved   The resolved stream URL, or {@code null} for none
     * @param unresolved The original stream URL, or {@code null} for none
     * @return The station, with the favicon, the name and the identifier shared by the tests
     */
    private static Station station(String resolved, String unresolved) {
        Station station = mock(Station.class);
        when(station.getUrlResolved()).thenReturn(resolved);
        when(station.getUrl()).thenReturn(unresolved);
        when(station.getFavicon()).thenReturn(FAVICON);
        when(station.getName()).thenReturn(NAME);
        when(station.getStationUUID()).thenReturn(STATION_UUID);
        return station;
    }

    /**
     * Tells the validation to accept a URL.
     *
     * @param url         The URL to accept
     * @param mustBeHttps The value of the flag the mapper is expected to pass with the URL
     */
    private void accept(String url, boolean mustBeHttps) {
        securityStatic.when(() -> SecurityUtils.isValidUrl(url, mustBeHttps)).thenReturn(true);
    }

    /**
     * The resolved URL must be preferred when it is valid, even if the original one is valid
     * too, and the original one must not even be checked
     */
    @Test
    public void map_resolvedUrlValid_usesResolvedUrl() {
        accept(RESOLVED, false);
        accept(UNRESOLVED, false);
        accept(FAVICON, true);

        RadioStation result = RadioStationMapper.fromStationToRadioStationModel(
                station(RESOLVED, UNRESOLVED));

        assertNotNull(result);
        assertEquals(RESOLVED, result.getURL());
        securityStatic.verify(() -> SecurityUtils.isValidUrl(UNRESOLVED, false), never());
    }
    /**
     * The original URL must be used when the resolved one is not valid
     */
    @Test
    public void map_resolvedUrlInvalid_fallsBackToOriginalUrl() {
        accept(UNRESOLVED, false);
        accept(FAVICON, true);

        RadioStation result = RadioStationMapper.fromStationToRadioStationModel(
                station(RESOLVED, UNRESOLVED));

        assertNotNull(result);
        assertEquals(UNRESOLVED, result.getURL());
    }
    /**
     * The original URL must be used when the station has no resolved one at all
     */
    @Test
    public void map_resolvedUrlMissing_fallsBackToOriginalUrl() {
        accept(UNRESOLVED, false);
        accept(FAVICON, true);

        RadioStation result = RadioStationMapper.fromStationToRadioStationModel(
                station(null, UNRESOLVED));

        assertNotNull(result);
        assertEquals(UNRESOLVED, result.getURL());
    }
    /**
     * A station with no valid stream URL must be discarded, and its favicon must not even be
     * checked
     */
    @Test
    public void map_noValidStreamUrl_returnsNull() {
        accept(FAVICON, true);

        RadioStation result = RadioStationMapper.fromStationToRadioStationModel(
                station(RESOLVED, UNRESOLVED));

        assertNull(result);
        securityStatic.verify(() -> SecurityUtils.isValidUrl(FAVICON, true), never());
    }
    /**
     * A station with a valid stream URL but an invalid favicon must be discarded
     */
    @Test
    public void map_faviconInvalid_returnsNull() {
        accept(RESOLVED, false);

        RadioStation result = RadioStationMapper.fromStationToRadioStationModel(
                station(RESOLVED, UNRESOLVED));

        assertNull(result);
    }
    /**
     * The stream may be served over HTTP while the favicon must use HTTPS, so the stream URLs
     * must be validated without requiring HTTPS and the favicon with it, and never the other
     * way round
     */
    @Test
    public void map_streamAllowsHttpAndFaviconRequiresHttps() {
        accept(RESOLVED, false);
        accept(FAVICON, true);

        RadioStationMapper.fromStationToRadioStationModel(station(RESOLVED, UNRESOLVED));

        securityStatic.verify(() -> SecurityUtils.isValidUrl(RESOLVED, false));
        securityStatic.verify(() -> SecurityUtils.isValidUrl(FAVICON, true));
        securityStatic.verify(() -> SecurityUtils.isValidUrl(RESOLVED, true), never());
        securityStatic.verify(() -> SecurityUtils.isValidUrl(FAVICON, false), never());
    }
    /**
     * A valid station must be converted with its identifier, its stream URL, its favicon and
     * its name, each in the right field
     */
    @Test
    public void map_validStation_copiesAllFields() {
        accept(RESOLVED, false);
        accept(FAVICON, true);

        RadioStation result = RadioStationMapper.fromStationToRadioStationModel(
                station(RESOLVED, UNRESOLVED));

        assertNotNull(result);
        assertEquals(STATION_UUID, result.getUUID());
        assertEquals(RESOLVED, result.getURL());
        assertEquals(FAVICON, result.getFaviconURL());
        assertEquals(NAME, result.getName());
    }
}
