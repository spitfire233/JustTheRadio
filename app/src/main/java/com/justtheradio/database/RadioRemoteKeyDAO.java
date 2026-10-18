package com.justtheradio.database;

import androidx.room3.Dao;
import androidx.room3.Insert;
import androidx.room3.OnConflictStrategy;
import androidx.room3.Query;

import com.justtheradio.model.RadioRemoteKey;

@Dao
public interface RadioRemoteKeyDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrReplace(RadioRemoteKey radioRemoteKey);
    @Query("SELECT * FROM RadioRemoteKey WHERE id = 1")
    RadioRemoteKey getRadioRemoteKey();
    @Query("DELETE FROM RadioRemoteKey")
    void clear();
}
