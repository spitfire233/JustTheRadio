package com.justtheradio.database;

import static com.justtheradio.utils.constant.Constants.ROOM_DATABASE_NAME;
import static com.justtheradio.utils.constant.Constants.ROOM_DATABASE_VERSION;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.justtheradio.model.radio.FavouriteStation;
import com.justtheradio.model.radio.RadioRemoteKey;
import com.justtheradio.model.radio.RadioStation;

import java.util.concurrent.ExecutorService;


@Database(entities = {RadioStation.class, RadioRemoteKey.class, FavouriteStation.class},
        version = ROOM_DATABASE_VERSION, exportSchema = false)
public abstract class RadioRoomDatabase extends RoomDatabase {

    public abstract RadioDAO radioDAO();
    private static volatile RadioRoomDatabase INSTANCE;
    private static ExecutorService databaseWriteExecutor;

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

    public static ExecutorService getDatabaseWriteExecutor() {
        return databaseWriteExecutor;
    }
}
