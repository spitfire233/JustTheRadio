package com.justtheradio.adapter.diff;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.recyclerview.widget.DiffUtil;

import com.justtheradio.model.radio.FavouriteStation;

import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

/**
 * Unit tests for the {@link DiffUtil.ItemCallback} returned by
 * {@link RadioStationDiff#getDiffCallback()}.
 */
public class RadioStationDiffUnitTest {
    /**
     * The callback under test
     */
    private DiffUtil.ItemCallback<FavouriteStation> callback;

    /**
     * Creates a fresh callback before every test
     */
    @Before
    public void setUp() {
        callback = RadioStationDiff.getDiffCallback();
    }
    /**
     * Helper that builds a station with the given identifying data. The favicon is fixed, tests
     * that care about it change it explicitly.
     *
     * @param uuid The unique identifier of the station
     * @param name The name of the station
     * @param url  The audio stream URL of the station
     * @return The station
     */
    private FavouriteStation createStation(UUID uuid, String name, String url) {
        FavouriteStation station = new FavouriteStation();
        station.setUUID(uuid);
        station.setName(name);
        station.setURL(url);
        station.setFaviconURL("http://example.com/logo.png");
        return station;
    }
    /**
     * Two stations with the same UUID are the same item, even if all the other fields differ
     * (for example because the station was renamed)
     */
    @Test
    public void areItemsTheSame_sameUuidDifferentData_returnsTrue() {
        UUID uuid = UUID.randomUUID();
        FavouriteStation oldItem = createStation(uuid, "Old name", "http://old/stream");
        FavouriteStation newItem = createStation(uuid, "New name", "http://new/stream");
        assertTrue(callback.areItemsTheSame(oldItem, newItem));
    }
    /**
     * Two stations with different UUIDs are different items, even if every other field is equal
     */
    @Test
    public void areItemsTheSame_differentUuidSameData_returnsFalse() {
        FavouriteStation oldItem =
                createStation(UUID.randomUUID(), "Radio", "http://radio/stream");
        FavouriteStation newItem =
                createStation(UUID.randomUUID(), "Radio", "http://radio/stream");
        assertFalse(callback.areItemsTheSame(oldItem, newItem));
    }
    /**
     * A station is always the same item as itself
     */
    @Test
    public void areItemsTheSame_sameInstance_returnsTrue() {
        FavouriteStation station =
                createStation(UUID.randomUUID(), "Radio", "http://radio/stream");

        assertTrue(callback.areItemsTheSame(station, station));
    }
    /**
     * Stations with the same name and URL have the same contents
     */
    @Test
    public void areContentsTheSame_sameNameAndUrl_returnsTrue() {
        UUID uuid = UUID.randomUUID();
        FavouriteStation oldItem = createStation(uuid, "Radio", "http://radio/stream");
        FavouriteStation newItem = createStation(uuid, "Radio", "http://radio/stream");

        assertTrue(callback.areContentsTheSame(oldItem, newItem));
    }
    /**
     * A different name means different contents, so the item must be rebound
     */
    @Test
    public void areContentsTheSame_differentName_returnsFalse() {
        UUID uuid = UUID.randomUUID();
        FavouriteStation oldItem = createStation(uuid, "Old name", "http://radio/stream");
        FavouriteStation newItem = createStation(uuid, "New name", "http://radio/stream");

        assertFalse(callback.areContentsTheSame(oldItem, newItem));
    }
    /**
     * A different stream URL means different contents
     */
    @Test
    public void areContentsTheSame_differentUrl_returnsFalse() {
        UUID uuid = UUID.randomUUID();
        FavouriteStation oldItem = createStation(uuid, "Radio", "http://old/stream");
        FavouriteStation newItem = createStation(uuid, "Radio", "http://new/stream");

        assertFalse(callback.areContentsTheSame(oldItem, newItem));
    }
    /**
     * Only name and URL are compared: this test documents that a change of the favicon or of the
     * {@code addedAt} timestamp does NOT count as a content change.
     */
    @Test
    public void areContentsTheSame_onlyFaviconAndAddedAtDiffer_returnsTrue() {
        UUID uuid = UUID.randomUUID();
        FavouriteStation oldItem = createStation(uuid, "Radio", "http://radio/stream");
        FavouriteStation newItem = createStation(uuid, "Radio", "http://radio/stream");
        newItem.setFaviconURL("http://example.com/other-logo.png");
        newItem.setAddedAt(123456789L);

        assertTrue(callback.areContentsTheSame(oldItem, newItem));
    }
}
