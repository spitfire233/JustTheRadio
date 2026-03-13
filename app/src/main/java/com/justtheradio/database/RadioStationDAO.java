package com.justtheradio.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.justtheradio.model.RadioStation;
import com.justtheradio.model.StationType;

import java.util.List;

@Dao
public interface RadioStationDAO {

    @Query("SELECT * FROM RadioStation")
    public List<RadioStation> getAll();

    @Query("SELECT * FROM RadioStation LIMIT :limit OFFSET :offset")
    public List<RadioStation> getAll(int limit, int offset);

    @Query("SELECT * FROM RadioStation WHERE id = :id")
    public RadioStation getRadioStation(long id);

    @Query("SELECT * FROM RadioStation WHERE isFavourite = 1")
    public List<RadioStation> getFavouriteRadioStations();

    @Query("SELECT * FROM RadioStation WHERE stationType = :stationType LIMIT :limit OFFSET :offset")
    public List<RadioStation> getRadioStationsByType(int limit, int offset, StationType stationType);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insert(RadioStation ... radioStations);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertList(List<RadioStation> radioStations);

    @Delete
    public void delete(RadioStation radioStation);

    @Query("DELETE FROM RadioStation WHERE id = :id")
    public void deleteByID(long id);

    @Query("DELETE FROM RadioStation")
    public void deleteAll();

    @Update
    public void updateRadioStation(RadioStation radioStation);
}
