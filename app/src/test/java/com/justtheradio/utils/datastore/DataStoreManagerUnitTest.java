package com.justtheradio.utils.datastore;

import static com.justtheradio.utils.constant.LocalCacheConstants.NO_KEY_WITH_THAT_NAME_IN_DATASTORE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Application;

import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.NoSuchElementException;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Function;

/**
 * Unit tests for {@link DataStoreManager}.
 * The real DataStore is never created. The builder that the manager uses is
 * replaced with {@code mockConstruction} so that it hands out a mocked DataStore, and the
 * singleton instance is cleared by reflection before and after each test.
 */
public class DataStoreManagerUnitTest {
    /** Name of the key used by the tests that read or write a value. */
    private static final String KEY = "country";

    /** Mocked application, passed to the manager. */
    private Application application;
    /** Mocked DataStore, whose results drive every read and write. */
    private RxDataStore<Preferences> rxDataStore;
    /** Replaces every builder the manager creates, closed after each test. */
    private MockedConstruction<RxPreferenceDataStoreBuilder> builderConstruction;
    /** The manager under test, built on the mocked DataStore. */
    private DataStoreManager manager;

    /**
     * Clears any singleton left by a previous test, then builds the manager on a mocked
     * DataStore, so that no file is created and no Android context is needed
     *
     * @throws ReflectiveOperationException If the singleton field cannot be cleared
     */
    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws ReflectiveOperationException {
        resetInstance();

        application = mock(Application.class);
        rxDataStore = mock(RxDataStore.class);
        builderConstruction = Mockito.mockConstruction(RxPreferenceDataStoreBuilder.class,
                (mock, context) -> when(mock.build()).thenReturn(rxDataStore));

        manager = DataStoreManager.getInstance(application);
    }

    /**
     * Construction mocks are bound to the thread that opened them and the singleton is global:
     * both must be undone after each test, or they leak into the following ones
     *
     * @throws ReflectiveOperationException If the singleton field cannot be cleared
     */
    @After
    public void tearDown() throws ReflectiveOperationException {
        builderConstruction.close();
        resetInstance();
    }

    /**
     * Clears the singleton instance of the manager, so that the next call to getInstance builds
     * a new one.
     *
     * @throws ReflectiveOperationException If the field cannot be found or written
     */
    private static void resetInstance() throws ReflectiveOperationException {
        Field instance = DataStoreManager.class.getDeclaredField("INSTANCE");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    /**
      Every call must return the same instance, since only one DataStore may be active for a
      given file at a time
     */
    @Test
    public void getInstance_calledAgain_returnsSameInstance() {
        assertSame(manager, DataStoreManager.getInstance(application));
        assertSame(manager, DataStoreManager.getInstance(application));
    }
    /**
     * The DataStore must be created only the first time the instance is requested, and not
     * again by the following calls
     */
    @Test
    public void getInstance_calledAgain_createsDataStoreOnce() {
        DataStoreManager.getInstance(application);
        DataStoreManager.getInstance(application);

        assertEquals(1, builderConstruction.constructed().size());
    }
    /**
     * Saving must update the DataStore by setting the value under the given key on a copy of
     * the current preferences, and emit the updated preferences
     */
    @Test
    public void saveStringResource_savesValueUnderKey() {
        Preferences current = mock(Preferences.class);
        MutablePreferences updated = mock(MutablePreferences.class);
        when(current.toMutablePreferences()).thenReturn(updated);
        when(rxDataStore.updateDataAsync(any())).thenAnswer(invocation -> {
            Function<Preferences, Single<Preferences>> transform = invocation.getArgument(0);
            return transform.apply(current);
        });

        manager.saveStringResource(KEY, "IT")
                .test()
                .assertValue(updated);

        verify(updated).set(PreferencesKeys.stringKey(KEY), "IT");
    }
    /**
     * A failed update must reach the caller as an error with the original exception
     */
    @Test
    public void saveStringResource_updateFails_propagatesError() {
        RuntimeException error = new RuntimeException("disk full");
        when(rxDataStore.updateDataAsync(any())).thenReturn(Single.<Preferences>error(error));

        manager.saveStringResource(KEY, "IT")
                .test()
                .assertError(error);
    }
    /**
     * A stored value must be emitted as it is
     */
    @Test
    public void getStringResource_valuePresent_emitsValue() {
        Preferences prefs = mock(Preferences.class);
        when(prefs.get(PreferencesKeys.stringKey(KEY))).thenReturn("IT");
        when(rxDataStore.data()).thenReturn(Flowable.just(prefs));

        manager.getStringResource(KEY)
                .test()
                .assertValue("IT");
    }
    /**
     * A key that is not stored must come out as a NoSuchElementException with the dedicated
     * message, and not as a null value, since RxJava cannot emit null
     */
    @Test
    public void getStringResource_keyMissing_errorsWithNoSuchElement() {
        Preferences prefs = mock(Preferences.class);
        when(rxDataStore.data()).thenReturn(Flowable.just(prefs));

        manager.getStringResource(KEY)
                .test()
                .assertError(error -> error instanceof NoSuchElementException
                        && NO_KEY_WITH_THAT_NAME_IN_DATASTORE.equals(error.getMessage()));
    }
    /**
     * Only the current value must be read, even when the DataStore keeps emitting newer ones,
     * since the stream must not be left open to follow later changes
     */
    @Test
    public void getStringResource_severalEmissions_readsOnlyTheFirst() {
        Preferences first = mock(Preferences.class);
        Preferences second = mock(Preferences.class);
        when(first.get(PreferencesKeys.stringKey(KEY))).thenReturn("IT");
        when(second.get(PreferencesKeys.stringKey(KEY))).thenReturn("FR");
        when(rxDataStore.data()).thenReturn(Flowable.just(first, second));

        manager.getStringResource(KEY)
                .test()
                .assertValue("IT")
                .assertComplete();
    }
    /**
     * A failure reading the DataStore must reach the caller as an error with the original
     * exception
     */
    @Test
    public void getStringResource_readFails_propagatesError() {
        RuntimeException error = new RuntimeException("corrupted file");
        when(rxDataStore.data()).thenReturn(Flowable.<Preferences>error(error));

        manager.getStringResource(KEY)
                .test()
                .assertError(error);
    }
}
