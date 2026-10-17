package com.justtheradio.database;

import androidx.room3.Dao;
import androidx.room3.Insert;
import androidx.room3.OnConflictStrategy;
import androidx.room3.Query;
import androidx.room3.Update;

import com.justtheradio.model.radio.RadioStation;

import java.util.List;

@Dao
public interface RadioDAO {

    @Query("SELECT * FROM RadioStation LIMIT :limit OFFSET :offset")
    List<RadioStation> getStationsWithPaging(int offset, int limit);

    @Query("SELECT * FROM RadioStation WHERE `isFavourite` = 1")
    List<RadioStation> getFavouriteStations();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<RadioStation> radioStations);

    @Update
    int updateRadioStation(RadioStation radioStation);

}
