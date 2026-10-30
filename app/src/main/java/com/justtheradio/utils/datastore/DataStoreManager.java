package com.justtheradio.utils.datastore;

import static com.justtheradio.utils.constant.LocalCacheConstants.DATASTORE_NAME;

import android.app.Application;

import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;

import io.reactivex.rxjava3.core.Single;

/**
 * Singleton that wraps the app DataStore.
 * It is a singleton because only one DataStore instance may be active for a given file at
 * the same time. The operations are exposed with RxJava, as {@link Single}, and only
 * support values stored as strings.
 * Disposables from the methods in this class should be managed by the caller class.
 */
public class DataStoreManager {
    /** The DataStore holding the preferences. */
    private final RxDataStore<Preferences> rxDataStore;
    /** The only instance of the class. */
    private static volatile DataStoreManager INSTANCE;
    /**
     * Private constructor for the {@link DataStoreManager} class.
     * This constructor is private to adhere to the singleton pattern.
     *
     * @param application The application, used to create the DataStore file
     */
    private DataStoreManager(Application application) {
        rxDataStore = new RxPreferenceDataStoreBuilder(application, DATASTORE_NAME).build();
    }
    /**
     * Returns the only instance of this class, creating it the first time it is requested.
     *
     * @param application The application, used to create the DataStore file
     * @return The only instance of {@link DataStoreManager}
     */
    public static DataStoreManager getInstance(Application application) {
        if (INSTANCE == null) {
            synchronized (DataStoreManager.class) {
                if(INSTANCE == null)
                    INSTANCE = new DataStoreManager(application);
            }
        }
        return INSTANCE;
    }
    /**
     * Saves a string in the DataStore, replacing the previous value of the key if there is one.
     * The update is atomic and is performed only when the returned {@link Single} is
     * subscribed to.
     *
     * @param key   The name of the key
     * @param value The string to save
     * @return A {@link Single} emitting the updated {@link Preferences} once the value has
     * been saved, or an error if the update fails
     */
    public Single<Preferences> saveStringResource(String key, String value) {
        Preferences.Key<String> resourceKey = PreferencesKeys.stringKey(key);
        return rxDataStore.updateDataAsync(prefs -> {
            MutablePreferences mutablePreferences = prefs.toMutablePreferences();
            mutablePreferences.set(resourceKey, value);
            return Single.just(mutablePreferences);
        });
    }
    /**
     * Reads a string from the DataStore. Only the current value is read: the stream is not
     * kept open to follow later changes.
     *
     * @param key The name of the key
     * @return A {@link Single} emitting the saved string, or an error if the key is not
     * present or the DataStore cannot be read
     */
    public Single<String> getStringResource(String key) {
        Preferences.Key<String> resourceKey = PreferencesKeys.stringKey(key);
        return rxDataStore.data()
                .map(prefs -> prefs.get(resourceKey))
                .firstOrError();
    }
}