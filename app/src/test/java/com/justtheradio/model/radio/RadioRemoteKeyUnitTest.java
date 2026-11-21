package com.justtheradio.model.radio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for {@link RadioRemoteKey}.
 * The class holds only data, so nothing is mocked.
 */
public class RadioRemoteKeyUnitTest {
    /**
     * The empty constructor, used by Room when reading rows, must produce a key with the fixed
     * id, an offset of 0 and the end not reached
     */
    @Test
    public void emptyConstructor_hasDefaultValues() {
        RadioRemoteKey key = new RadioRemoteKey();

        assertEquals(1, key.getId());
        assertEquals(0, key.getNextOffset());
        assertFalse(key.isEndReached());
    }
    /**
     * The full constructor must store the offset and the end flag it is given, each in its own
     * field
     */
    @Test
    public void constructor_withValues_setsOffsetAndEndReached() {
        RadioRemoteKey key = new RadioRemoteKey(60, true);

        assertEquals(60, key.getNextOffset());
        assertTrue(key.isEndReached());
    }
    /**
     * The full constructor must keep the end flag false when it is given false, since a key with
     * more pages to load must not stop the loading
     */
    @Test
    public void constructor_endNotReached_keepsFlagFalse() {
        RadioRemoteKey key = new RadioRemoteKey(20, false);

        assertEquals(20, key.getNextOffset());
        assertFalse(key.isEndReached());
    }
    /**
     * The full constructor must leave the id at 1, so that the table keeps holding a single row
     * that is replaced after every loaded page
     */
    @Test
    public void constructor_withValues_keepsFixedId() {
        RadioRemoteKey key = new RadioRemoteKey(60, true);

        assertEquals(1, key.getId());
    }
    /**
     * The id must be returned as it was set, since Room fills it in through the setter when
     * reading a row
     */
    @Test
    public void setId_thenGetId_returnsSameValue() {
        RadioRemoteKey key = new RadioRemoteKey();

        key.setId(5);

        assertEquals(5, key.getId());
    }
    /**
     * The offset must be returned as it was set, and the other fields must stay untouched
     */
    @Test
    public void setNextOffset_thenGetNextOffset_returnsSameValue() {
        RadioRemoteKey key = new RadioRemoteKey();

        key.setNextOffset(40);

        assertEquals(40, key.getNextOffset());
        assertEquals(1, key.getId());
        assertFalse(key.isEndReached());
    }
    /**
     * The end flag must follow the last value set, in both directions, so that a key can be
     * marked as finished and later reopened
     */
    @Test
    public void setEndReached_followsLastValueSet() {
        RadioRemoteKey key = new RadioRemoteKey();

        key.setEndReached(true);
        assertTrue(key.isEndReached());

        key.setEndReached(false);
        assertFalse(key.isEndReached());
    }
}
