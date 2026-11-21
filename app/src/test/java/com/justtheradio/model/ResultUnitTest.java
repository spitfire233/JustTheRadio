package com.justtheradio.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class ResultUnitTest {
    /**
     * A Success must report that the operation succeeded: here a Success holding some data
     */
    @Test
    public void isSuccess_success_returnsTrue() {
        Result result = new Result.Success<>("data");

        assertTrue(result.isSuccess());
    }
    /**
     * A Success with no data must still report success, since the data is optional
     */
    @Test
    public void isSuccess_successWithoutData_returnsTrue() {
        Result result = new Result.Success<>();

        assertTrue(result.isSuccess());
    }

    /**
     * A Failure must report that the operation did not succeed
     */
    @Test
    public void isSuccess_failure_returnsFalse() {
        Result result = new Result.Failure("error");

        assertFalse(result.isSuccess());
    }
    /**
     * The data must be returned exactly as it was given, not copied
     */
    @Test
    public void success_getData_returnsSameInstance() {
        List<String> stations = Arrays.asList("a", "b");

        Result.Success<List<String>> success = new Result.Success<>(stations);

        assertSame(stations, success.getData());
    }
    /**
     * A Success built with no arguments must hold no data
     */
    @Test
    public void success_noArgumentsConstructor_hasNullData() {
        Result.Success<String> success = new Result.Success<>();

        assertNull(success.getData());
    }
    /**
     * A Success built with an explicit null must keep it and still be a Success
     */
    @Test
    public void success_nullData_isStillSuccess() {
        Result.Success<String> success = new Result.Success<>(null);

        assertNull(success.getData());
        assertTrue(success.isSuccess());
    }
    /**
     * The message must be returned exactly as it was given
     */
    @Test
    public void failure_getMessage_returnsGivenMessage() {
        Result.Failure failure = new Result.Failure("connection lost");

        assertEquals("connection lost", failure.getMessage());
    }
    /**
     * A Failure built with a null message must keep it and still be a Failure
     */
    @Test
    public void failure_nullMessage_isStillFailure() {
        Result.Failure failure = new Result.Failure(null);

        assertNull(failure.getMessage());
        assertFalse(failure.isSuccess());
    }
}
