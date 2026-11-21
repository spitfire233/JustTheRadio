package com.justtheradio.model.radio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

import java.util.UUID;

/**
 * Unit tests for {@link FavouriteStation}.
 * The class holds only data, so nothing is mocked. A real {@link RadioStation}
 * is used as the source station
 */
public class FavouriteStationUnitTest {
    /** Identifier used by the tests that need a station. */
    private static final UUID STATION_UUID = UUID.fromString("11111111-2222-3333-4444-555555555555");
    /** Stream URL used by the tests that need a station. */
    private static final String STREAM_URL = "https://example.com/stream";
    /** Logo URL used by the tests that need a station. */
    private static final String FAVICON_URL = "https://example.com/logo.png";
    /** Name used by the tests that need a station. */
    private static final String NAME = "Radio Test";
    /** Timestamp used by the tests that need a time of addition. */
    private static final long ADDED_AT = 1700000000000L;

    /**
     * The constructor must copy every value of the source station, each in its own field
     */
    @Test
    public void constructor_withStation_copiesAllStationFields() {
        RadioStation source = new RadioStation(STATION_UUID, STREAM_URL, FAVICON_URL, NAME);

        FavouriteStation favourite = new FavouriteStation(source, ADDED_AT);

        assertEquals(STATION_UUID, favourite.getUUID());
        assertEquals(STREAM_URL, favourite.getURL());
        assertEquals(FAVICON_URL, favourite.getFaviconURL());
        assertEquals(NAME, favourite.getName());
    }
    /**
     * The constructor must store the time it is given, since it is the only place it is set
     * when a station is saved
     */
    @Test
    public void constructor_withStation_setsAddedAt() {
        RadioStation source = new RadioStation(STATION_UUID, STREAM_URL, FAVICON_URL, NAME);

        FavouriteStation favourite = new FavouriteStation(source, ADDED_AT);

        assertEquals(ADDED_AT, favourite.getAddedAt());
    }
    /**
     * The favourite must keep the values it copied when the source station changes later
     */
    @Test
    public void constructor_sourceChangedAfterwards_favouriteKeepsOriginalValues() {
        RadioStation source = new RadioStation(STATION_UUID, STREAM_URL, FAVICON_URL, NAME);
        FavouriteStation favourite = new FavouriteStation(source, ADDED_AT);

        source.setName("Another name");
        source.setURL("https://example.com/other");

        assertEquals(NAME, favourite.getName());
        assertEquals(STREAM_URL, favourite.getURL());
    }

    /**
     * A missing station must be rejected and not produce a half-empty favourite
     */
    @Test
    public void constructor_nullStation_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new FavouriteStation(null, ADDED_AT));
    }
    /**
     * The empty constructor, used by Room when reading rows, must produce a favourite with no
     * data and a time of 0
     */
    @Test
    public void emptyConstructor_leavesEverythingUnset() {
        FavouriteStation favourite = new FavouriteStation();

        assertEquals(0L, favourite.getAddedAt());
        assertNull(favourite.getUUID());
        assertNull(favourite.getURL());
        assertNull(favourite.getFaviconURL());
        assertNull(favourite.getName());
    }
    /**
     * The time must be returned as it was set, since Room fills it in through the setter when
     * reading a row
     */
    @Test
    public void setAddedAt_thenGetAddedAt_returnsSameValue() {
        FavouriteStation favourite = new FavouriteStation();

        favourite.setAddedAt(ADDED_AT);

        assertEquals(ADDED_AT, favourite.getAddedAt());
    }
}
