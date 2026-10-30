package com.justtheradio.database;

import static com.justtheradio.utils.constant.LocalCacheConstants.ROOM_DATABASE_NAME;
import static com.justtheradio.utils.constant.LocalCacheConstants.ROOM_DATABASE_VERSION;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.justtheradio.model.radio.FavouriteStation;
import com.justtheradio.model.radio.RadioRemoteKey;
import com.justtheradio.model.radio.RadioStation;

import java.util.concurrent.ExecutorService;

/**
 * Room database of the app. It holds the cached radio stations, the remote key used by the
 * RemoteMediator for paging, and the user's favourite stations.
 */
@Database(entities = {RadioStation.class, RadioRemoteKey.class, FavouriteStation.class},
        version = ROOM_DATABASE_VERSION, exportSchema = false)
public abstract class RadioRoomDatabase extends RoomDatabase {
    /**
     * Gives access to the {@link RadioDAO}. Room provides the implementation.
     * @return The {@link RadioDAO} for this database.
     */
    public abstract RadioDAO radioDAO();
    /**
     * The single instance of the database
     */
    private static volatile RadioRoomDatabase INSTANCE;
    /**
     * Executor service used to run write operations off the main thread
     */
    private static ExecutorService databaseWriteExecutor;

    /**
     * Returns the singleton instance of the database.
     * @param context           A {@link Context} object used to create the database. Use the
     *                          application context to avoid memory leaks.
     * @param executorService   A {@link ExecutorService} to run write operations
     * @return                  The single instance of {@link RadioRoomDatabase}
     */
    public static RadioRoomDatabase getInstance(Context context, ExecutorService executorService) {
        if (INSTANCE == null) {
            synchronized (RadioRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    RadioRoomDatabase.class, ROOM_DATABASE_NAME)
                            .build();
                    databaseWriteExecutor = executorService;
                }

            }
        }
        return INSTANCE;
    }
    /**
     * Getter method for the write executor service
     * @return The database {@link ExecutorService}
     */
    @Nullable
    public static ExecutorService getDatabaseWriteExecutor() {
        return databaseWriteExecutor;
    }
}
