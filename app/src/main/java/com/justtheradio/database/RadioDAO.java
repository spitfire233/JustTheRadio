package com.justtheradio.database;

import androidx.paging.PagingSource;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Upsert;

import com.justtheradio.model.radio.BaseRadioStation;
import com.justtheradio.model.radio.FavouriteStation;
import com.justtheradio.model.radio.RadioRemoteKey;
import com.justtheradio.model.radio.RadioStation;

import java.util.List;
import java.util.UUID;

import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface RadioDAO {

    // RADIO STATION
    @Query("SELECT * FROM RadioStation ORDER BY id ASC")
    PagingSource<Integer, RadioStation> pagingSource();

    @Insert
    void insertAll(List<RadioStation> stations);

    @Query("DELETE FROM RadioStation")
    void clearCache();

    // Radio Remote key
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrReplace(RadioRemoteKey radioRemoteKey);
    @Query("SELECT * FROM RadioRemoteKey WHERE id = 1")
    RadioRemoteKey getRadioRemoteKey();
    @Query("DELETE FROM RadioRemoteKey")
    void clearKey();

    @Transaction
    default void savePage(boolean refresh, List<RadioStation> page, RadioRemoteKey radioRemoteKey) {
        if (refresh) {
            clearCache();
            clearKey();
        }
        insertAll(page);
        insertOrReplace(radioRemoteKey);
    }

    // Favourites
    @Query("SELECT UUID From FavouriteStation")
    Flowable<List<UUID>> observeFavouritesUUIDs();

    @Query("SELECT * From FavouriteStation ORDER BY addedAt ASC")
    Flowable<List<FavouriteStation>> observeFavourites();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertFavourite(FavouriteStation favouriteStation);

    @Query("DELETE FROM FavouriteStation WHERE UUID = :UUID")
    void deleteFavourite(UUID UUID);
}
