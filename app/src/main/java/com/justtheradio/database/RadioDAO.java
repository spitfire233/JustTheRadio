package com.justtheradio.database;

import androidx.paging.PagingSource;
import androidx.room3.Dao;
import androidx.room3.Insert;
import androidx.room3.OnConflictStrategy;
import androidx.room3.Query;
import androidx.room3.Update;

import com.justtheradio.model.RadioStation;

import java.util.List;
import java.util.UUID;

@Dao
public interface RadioDAO {

    @Query("SELECT * FROM RadioStation ORDER BY position ASC")
    PagingSource<Integer, RadioStation> pagingSource();

    @Query("DELETE FROM RadioStation WHERE isFavourite = 0")
    void deleteNonFavourites();

    @Query("DELETE From RadioStation WHERE UUID IN (:uuids)")
    void deleteByUIIDs(List<UUID> uuids);

    @Query("SELECT * FROM RadioStation WHERE `isFavourite` = 1")
    List<RadioStation> getFavouriteStations();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<RadioStation> radioStations);

    @Update
    int updateRadioStation(RadioStation radioStation);

}
