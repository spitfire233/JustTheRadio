package com.justtheradio.source.user;

import static com.justtheradio.utils.constant.Constants.NO_KEY_WITH_THAT_NAME_IN_DATASTORE;

import android.app.Application;

import com.justtheradio.utils.datastore.DataStoreManagerSingleton;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

public class UserPreferencesLocalDataSource extends BaseUserPreferencesLocalDataSource {

    private final DataStoreManagerSingleton dataStoreManagerSingleton;
    private final CompositeDisposable compositeDisposable;

    public UserPreferencesLocalDataSource(Application application) {
        dataStoreManagerSingleton = DataStoreManagerSingleton.getInstance(application);
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void saveCountryPreference(String key, String country) {
        Disposable disposable = dataStoreManagerSingleton.saveStringResource(key, country)
                .subscribe(preferences -> userCallback.onSavingUserPreferenceSuccess(),
                        throwable -> userCallback.onFailureSavingOnLocal(new Exception(throwable)));
        compositeDisposable.add(disposable);
    }

    @Override
    public void getStringResource(String key) {
        Disposable disposable = dataStoreManagerSingleton.getStringResource(key)
                .subscribe(string -> {
                    if (string != null)
                        userCallback.onRetrievingUserPreferenceSuccess(string);
                    else
                        userCallback.onFailureRetrievingOnLocal(new Exception(NO_KEY_WITH_THAT_NAME_IN_DATASTORE));
                }, throwable -> userCallback.onFailureRetrievingOnLocal(new Exception(throwable)));
        compositeDisposable.add(disposable);
    }

}
