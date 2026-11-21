package com.justtheradio.model.radio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.util.UUID;

/**
 * Unit tests for {@link RadioStation}.
 * The class holds only data, so nothing is mocked.
 */
public class RadioStationUnitTest {
    /** Identifier used by the tests that need a station. */
    private static final UUID STATION_UUID = UUID.fromString("11111111-2222-3333-4444-555555555555");
    /** Stream URL used by the tests that need a station. */
    private static final String STREAM_URL = "https://example.com/stream";
    /** Logo URL used by the tests that need a station. */
    private static final String FAVICON_URL = "https://example.com/logo.png";
    /** Name used by the tests that need a station. */
    private static final String NAME = "Radio Test";

    /**
     * The full constructor must store every value it is given, each in its own field
     */
    @Test
    public void constructor_withValues_setsAllFields() {
        RadioStation station = new RadioStation(STATION_UUID, STREAM_URL, FAVICON_URL, NAME);

        assertEquals(STATION_UUID, station.getUUID());
        assertEquals(STREAM_URL, station.getURL());
        assertEquals(FAVICON_URL, station.getFaviconURL());
        assertEquals(NAME, station.getName());
    }
    /**
     * The full constructor must leave the ID at its default, since Room generates it on insert
     */
    @Test
    public void constructor_withValues_leavesIdAtDefault() {
        RadioStation station = new RadioStation(STATION_UUID, STREAM_URL, FAVICON_URL, NAME);

        assertEquals(0, station.getID());
    }
    /**
     * The empty constructor, used by Room when reading rows, must produce a station with no
     * data and the default ID
     */
    @Test
    public void emptyConstructor_leavesEverythingUnset() {
        RadioStation station = new RadioStation();

        assertEquals(0, station.getID());
        assertNull(station.getUUID());
        assertNull(station.getURL());
        assertNull(station.getFaviconURL());
        assertNull(station.getName());
    }
    /**
     * The ID must be returned as it was set, since Room fills it in through the setter when
     * reading a row
     */
    @Test
    public void setID_thenGetID_returnsSameValue() {
        RadioStation station = new RadioStation();

        station.setID(42);

        assertEquals(42, station.getID());
    }
    /**
     * Setting the ID must not touch the station's data
     */
    @Test
    public void setID_doesNotChangeOtherFields() {
        RadioStation station = new RadioStation(STATION_UUID, STREAM_URL, FAVICON_URL, NAME);

        station.setID(7);

        assertEquals(STATION_UUID, station.getUUID());
        assertEquals(STREAM_URL, station.getURL());
        assertEquals(FAVICON_URL, station.getFaviconURL());
        assertEquals(NAME, station.getName());
    }
}
