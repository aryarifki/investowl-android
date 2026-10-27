package me.cugarete.investowl.core.database

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "watchlist_table")
data class WatchlistEntity(
    @PrimaryKey
    val ticker: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Dao
interface WatchlistDao {

    @Query("SELECT ticker FROM watchlist_table ORDER BY addedAt DESC")
    fun getWatchlistTickers(): Flow<List<String>>

    @Query("SELECT ticker FROM watchlist_table ORDER BY addedAt DESC")
    suspend fun getWatchlistTickersList(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: WatchlistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<WatchlistEntity>)

    @Query("DELETE FROM watchlist_table WHERE ticker = :ticker")
    suspend fun delete(ticker: String)

    @Query("DELETE FROM watchlist_table")
    suspend fun clear()

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist_table WHERE ticker = :ticker)")
    fun isWatchlisted(ticker: String): Flow<Boolean>
}

@Database(entities = [WatchlistEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun watchlistDao(): WatchlistDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "investowl_local.db"
            ).build()
    }
}
