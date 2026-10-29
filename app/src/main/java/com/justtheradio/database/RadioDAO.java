package com.justtheradio.database;

import androidx.paging.PagingSource;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.justtheradio.model.radio.FavouriteStation;
import com.justtheradio.model.radio.RadioRemoteKey;
import com.justtheradio.model.radio.RadioStation;

import java.util.List;
import java.util.UUID;

import io.reactivex.rxjava3.core.Flowable;
/**
 * Data Access Object to interact with the local room database. This DAO provides methods to
 * retrieve information for the cached radio stations and the remote key used for paging.
 * To know more about the paging mechanism, refer to the {@link
 * <a href="https://developer.android.com/topic/libraries/architecture/paging/v3-network-db">
 *     official documentation
 * </a>
 * }
 */
@Dao
public interface RadioDAO {
    /**
     * Provides the paged list of cached stations, ordered by insertion id.
     * Room generates the {@link PagingSource} and invalidates it automatically
     * whenever the RadioStation table changes.
     *
     * @return A {@link PagingSource} that loads the cached stations page by page
     */
    @Query("SELECT * FROM RadioStation ORDER BY id ASC")
    PagingSource<Integer, RadioStation> pagingSource();
    /**
     * Insert all the items in the provided {@link List} in the local database
     * @param stations The list of {@link RadioStation} to insert.
     */
    @Insert
    void insertAll(List<RadioStation> stations);
    /**
     * Clears completely the radio station local cache.
     */
    @Query("DELETE FROM RadioStation")
    void clearCache();
    /**
     * Insert a new {@link RadioRemoteKey} in the local Room database. If a remote key is
     * already present, it will be replaced by the new one.
     * @param radioRemoteKey The {@link RadioRemoteKey} to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrReplace(RadioRemoteKey radioRemoteKey);
    /**
     * Get the cached {@link RadioRemoteKey} from the local Room database.
     * @return The cached {@link RadioRemoteKey}
     */
    @Query("SELECT * FROM RadioRemoteKey WHERE id = 1")
    RadioRemoteKey getRadioRemoteKey();
    /**
     * Clears the cached {@link RadioRemoteKey} from the local database.
     */
    @Query("DELETE FROM RadioRemoteKey")
    void clearKey();
    /**
     * Resets primary keys for the cached {@link RadioStation} table.
     */
    @Query("DELETE FROM sqlite_sequence WHERE name = 'RadioStation'")
    void resetPrimaryKeys();
    /**
     * Saves a loaded page and its remote key in a single transaction, so the cache and the key
     * can never get out of sync (e.g. if the app is killed halfway through).
     * On refresh, the old stations and the old key are removed first.
     *
     * @param refresh        true if this is a refresh and the existing cache must be discarded
     * @param page           The stations to store
     * @param radioRemoteKey The key to store for loading the next page
     */
    @Transaction
    default void savePage(boolean refresh, List<RadioStation> page, RadioRemoteKey radioRemoteKey) {
        if (refresh) {
            clearCache();
            clearKey();
            resetPrimaryKeys();
        }
        insertAll(page);
        insertOrReplace(radioRemoteKey);
    }
    /**
     * Observes the UUIDs of all favourite stations.
     * Emits a new list every time the favourites change.
     *
     * @return A {@link Flowable} emitting the current list of favourite UUIDs
     */
    @Query("SELECT UUID From FavouriteStation")
    Flowable<List<UUID>> observeFavouritesUUIDs();
    /**
     * Observes the favourite stations, ordered by when they were added (oldest first).
     * Emits a new list every time the favourites change.
     *
     * @return A {@link Flowable} emitting the current list of favourites
     */
    @Query("SELECT * From FavouriteStation ORDER BY addedAt ASC")
    Flowable<List<FavouriteStation>> observeFavourites();
    /**
     * Adds a station to the favourites. If a favourite with the same UUID already exists,
     * it is replaced.
     *
     * @param favouriteStation The station to add
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertFavourite(FavouriteStation favouriteStation);
    /**
     * Removes a station from the favourites.
     *
     * @param UUID The UUID of the station to remove
     */
    @Query("DELETE FROM FavouriteStation WHERE UUID = :UUID")
    void deleteFavourite(UUID UUID);
}
