package com.justtheradio.database;

import androidx.annotation.NonNull;
import androidx.paging.PagingSource;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;
import androidx.room.Upsert;

import com.justtheradio.model.RadioRemoteKey;
import com.justtheradio.model.RadioStation;

import java.util.List;
import java.util.UUID;

import de.sfuhrm.radiobrowser4j.Station;

@Dao
public interface RadioDAO {

    @Query("SELECT * FROM RadioStation")
    PagingSource<Integer, RadioStation> pagingSource();

    @Upsert
    void upsertStations(List<RadioStation> stations);

    @Upsert
    void upsertStation(RadioStation station);

    @Query("DELETE FROM RadioStation WHERE isFavourite = 0")
    void deleteNonFavourites();

    @Query("DELETE FROM RadioStation WHERE isFavourite = 1")
    void deleteFavourites();

    @Query("DELETE FROM RadioStation WHERE UUID = :uuid")
    void deleteStation(UUID uuid);

    @Query("DELETE From RadioStation WHERE UUID IN (:uuids)")
    void deleteByUIIDs(List<UUID> uuids);

    @Query("SELECT * FROM RadioStation WHERE `isFavourite` = 1")
    List<RadioStation> getFavouriteStations();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<RadioStation> radioStations);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrReplace(RadioRemoteKey radioRemoteKey);
    @Query("SELECT * FROM RadioRemoteKey WHERE id = 1")
    RadioRemoteKey getRadioRemoteKey();
    @Query("DELETE FROM RadioRemoteKey")
    void clearKey();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertFavourite(RadioStation radioStation);

    @Transaction
    default void savePage(boolean refresh, List<RadioStation> page, RadioRemoteKey radioRemoteKey) {
        if (refresh) {
            deleteNonFavourites();
            clearKey();
        }
        upsertStations(page);
        insertOrReplace(radioRemoteKey);
    }

    @Transaction
    default void saveFavourite(RadioStation radioStation) {
        upsertStation(radioStation);
        insertFavourite(radioStation);
    }


}
