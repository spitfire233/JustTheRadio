package com.justtheradio.source;

import static com.justtheradio.util.constants.Constants.RADIO_STATIONS_DATABASE_ERROR_SAVING;

import android.util.Log;

import com.justtheradio.database.RadioStationDAO;
import com.justtheradio.database.RadioStationRoomDatabase;
import com.justtheradio.model.RadioStation;

import java.util.List;

public class RadioStationsLocalDataSource extends BaseRadioStationsLocalDataSource {
    private final RadioStationDAO radioStationDAO;

    public RadioStationsLocalDataSource(RadioStationRoomDatabase radioStationRoomDatabase) {
        this.radioStationDAO = radioStationRoomDatabase.getRadioStationsDAO();
    }

    @Override
    public void saveRadioStations(List<RadioStation> radioStationList, boolean isSavingNational) {
        RadioStationRoomDatabase.databaseWriteExecutor.execute(() -> {
            try {
                List<RadioStation> favourites = radioStationDAO.getFavouriteRadioStations();
                for(RadioStation favourite: favourites) {
                    int position = radioStationList.indexOf(favourite);
                    if(position != -1) {
                        radioStationList.get(position).setFavourite(favourite.isFavourite());
                    }
                }
                radioStationDAO.insertList(radioStationList);
                callback.onSuccessSavingFromLocal(radioStationList, isSavingNational);
            } catch (Exception e) {
                Log.e(TAG, RADIO_STATIONS_DATABASE_ERROR_SAVING);
                callback.onFailureFromLocal(e, isSavingNational);
            }
        });
    }
}
