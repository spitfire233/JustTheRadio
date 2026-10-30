package com.justtheradio.utils.datastore;

import static com.justtheradio.utils.constant.LocalCacheConstants.DATASTORE_NAME;

import android.app.Application;

import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;

import io.reactivex.rxjava3.core.Single;

public class DataStoreManagerSingleton {

    private final RxDataStore<Preferences> rxDataStore;
    private static volatile DataStoreManagerSingleton INSTANCE;

    private DataStoreManagerSingleton(Application application) {
        rxDataStore = new RxPreferenceDataStoreBuilder(application, DATASTORE_NAME).build();
    }

    public static DataStoreManagerSingleton getInstance(Application application) {
        if (INSTANCE == null) {
            synchronized (DataStoreManagerSingleton.class) {
                if(INSTANCE == null)
                    INSTANCE = new DataStoreManagerSingleton(application);
            }
        }
        return INSTANCE;
    }

    public Single<Preferences> saveStringResource(String key, String value) {
        Preferences.Key<String> resourceKey = PreferencesKeys.stringKey(key);
        return rxDataStore.updateDataAsync(prefs -> {
            MutablePreferences mutablePreferences = prefs.toMutablePreferences();
            mutablePreferences.set(resourceKey, value);
            return Single.just(mutablePreferences);
        });
    }

    public Single<String> getStringResource(String key) {
        Preferences.Key<String> resourceKey = PreferencesKeys.stringKey(key);
        return rxDataStore.data()
                .map(prefs -> prefs.get(resourceKey))
                .firstOrError();
    }

}
