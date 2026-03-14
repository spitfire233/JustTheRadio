package com.justtheradio.database;

import static com.justtheradio.util.constants.Constants.RADIO_STATIONS_DATABASE_NAME;
import static com.justtheradio.util.constants.Constants.RADIO_STATIONS_DATABASE_VERSION;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.justtheradio.model.RadioStation;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {RadioStation.class}, version = RADIO_STATIONS_DATABASE_VERSION, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class RadioStationRoomDatabase extends RoomDatabase {

    public abstract RadioStationDAO getRadioStationsDAO();

    private static volatile RadioStationRoomDatabase INSTANCE;

    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public static RadioStationRoomDatabase getDatabase(final Context context) {
        if(INSTANCE == null) {
            synchronized (RadioStationRoomDatabase.class) {
                if(INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            RadioStationRoomDatabase.class, RADIO_STATIONS_DATABASE_NAME)
                            .fallbackToDestructiveMigration(true)
                            .build();
                }
            }
        }
        return INSTANCE;
    }




}
