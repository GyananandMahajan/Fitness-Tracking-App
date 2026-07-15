package com.example.data.db

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.data.model.CommunityShare
import com.example.data.model.LeaderboardEntry
import com.example.data.model.UserPreferences
import com.example.data.model.WorkoutLog
import kotlinx.coroutines.flow.Flow

@Dao
interface FitnessDao {

    // --- User Preferences ---
    @Query("SELECT * FROM user_preferences WHERE id = 0")
    fun getUserPreferences(): Flow<UserPreferences?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserPreferences(prefs: UserPreferences)

    // --- Workout Logs ---
    @Query("SELECT * FROM workout_logs ORDER BY timestamp DESC")
    fun getAllWorkouts(): Flow<List<WorkoutLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutLog): Long

    @Query("DELETE FROM workout_logs")
    suspend fun deleteAllWorkouts()

    // --- Leaderboard ---
    @Query("SELECT * FROM leaderboard ORDER BY rank ASC")
    fun getLeaderboard(): Flow<List<LeaderboardEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaderboard(entries: List<LeaderboardEntry>)

    @Query("DELETE FROM leaderboard")
    suspend fun clearLeaderboard()

    // --- Community Feed ---
    @Query("SELECT * FROM community_shares ORDER BY timestamp DESC")
    fun getCommunityShares(): Flow<List<CommunityShare>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShare(share: CommunityShare)

    @Update
    suspend fun updateShare(share: CommunityShare)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShares(shares: List<CommunityShare>)
}

@Database(
    entities = [
        UserPreferences::class,
        WorkoutLog::class,
        LeaderboardEntry::class,
        CommunityShare::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fitnessDao(): FitnessDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fitness_tracker_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
