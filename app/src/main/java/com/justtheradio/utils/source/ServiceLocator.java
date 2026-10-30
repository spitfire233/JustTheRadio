package com.justtheradio.utils.source;

import static com.justtheradio.utils.constant.RadioBrowserConstants.RADIO_BROWSER_AGENT;
import static com.justtheradio.utils.constant.RadioBrowserConstants.RADIO_BROWSER_TIMEOUT_MS;

import android.app.Application;

import com.justtheradio.database.RadioRoomDatabase;
import com.justtheradio.repository.RadioRepository;
import com.justtheradio.source.BaseRadioBrowserRemoteDataSource;
import com.justtheradio.source.BaseRadioLocalDataSource;
import com.justtheradio.source.RadioLocalDataSource;
import com.justtheradio.paging.RadioRemoteMediator;
import com.justtheradio.source.RadioBrowserRemoteDataSource;
import com.justtheradio.utils.datastore.DataStoreManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service locator that performs the dependency injection operations.
 * This class is a singleton and is shared between all the objects of the application.
 */
public class ServiceLocator {
    /**
     * Private constructor for {@link ServiceLocator}
     * This constructor is private to comply with the Singleton pattern.
     */
    private ServiceLocator() { }
    /** The only instance of the class. */
    private static volatile ServiceLocator INSTANCE = null;
    /** Executor shared by the app for background work; created the first time it is requested. */
    private ExecutorService executorService = null;
    /**
     * Returns the only instance of this class, creating it the first time it is requested.
     *
     * @return The only instance of {@link ServiceLocator}
     */
    public static ServiceLocator getInstance() {
        if(INSTANCE == null) {
            synchronized (ServiceLocator.class) {
                if(INSTANCE == null)
                    INSTANCE = new ServiceLocator();
            }
        }
        return INSTANCE;
    }
    /**
     * Returns an executor used for background work, creating it the first time it is
     * requested. It is a fixed thread pool with one thread for each available processor.
     *
     * @return The shared {@link ExecutorService}
     */
    public ExecutorService getExecutorService() {
        if (this.executorService == null)
            this.executorService =
                    Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        return executorService;
    }
    /**
     * Returns the Room database of the app, using the shared executor.
     *
     * @param application The application, used to create the database if it does not exist yet
     * @return The {@link RadioRoomDatabase}
     */
    public RadioRoomDatabase getRadioDatabase(Application application) {
        return RadioRoomDatabase.getInstance(application, getExecutorService());
    }

    /**
     * Builds a {@link RadioRepository}.
     * A new repository, with new data sources, is created at every call: to share the same
     * data between the screens, get the repository only once and keep it in a ViewModel.
     *
     * @param application The application, needed to create the database and the DataStore
     * @return A new {@link RadioRepository}
     */
    public RadioRepository getRadioRepository(Application application) {
        BaseRadioBrowserRemoteDataSource radioBrowserRemoteDataSource =
                new RadioBrowserRemoteDataSource(
                        RADIO_BROWSER_AGENT,
                        RADIO_BROWSER_TIMEOUT_MS
                );
        BaseRadioLocalDataSource radioLocalDataSource =
                new RadioLocalDataSource(application, getRadioDatabase(application));
        RadioRemoteMediator radioRemoteMediator = new RadioRemoteMediator(
                DataStoreManager.getInstance(application),
                radioBrowserRemoteDataSource,
                radioLocalDataSource);
        return new RadioRepository(radioBrowserRemoteDataSource, radioLocalDataSource, radioRemoteMediator);
    }
}
