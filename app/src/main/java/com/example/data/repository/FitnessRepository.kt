package com.example.data.repository

import com.example.BuildConfig
import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import com.example.data.db.FitnessDao
import com.example.data.model.CommunityShare
import com.example.data.model.LeaderboardEntry
import com.example.data.model.UserPreferences
import com.example.data.model.WorkoutLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.random.Random

class FitnessRepository(private val fitnessDao: FitnessDao) {

    val userPreferences: Flow<UserPreferences?> = fitnessDao.getUserPreferences()
    val allWorkouts: Flow<List<WorkoutLog>> = fitnessDao.getAllWorkouts()
    val leaderboard: Flow<List<LeaderboardEntry>> = fitnessDao.getLeaderboard()
    val communityShares: Flow<List<CommunityShare>> = fitnessDao.getCommunityShares()

    suspend fun savePreferences(prefs: UserPreferences) = withContext(Dispatchers.IO) {
        fitnessDao.saveUserPreferences(prefs)
    }

    suspend fun insertWorkout(workout: WorkoutLog): Long = withContext(Dispatchers.IO) {
        fitnessDao.insertWorkout(workout)
    }

    suspend fun addShare(share: CommunityShare) = withContext(Dispatchers.IO) {
        fitnessDao.insertShare(share)
    }

    suspend fun likeShare(share: CommunityShare) = withContext(Dispatchers.IO) {
        val updated = share.copy(
            likesCount = if (share.isLikedByMe) share.likesCount - 1 else share.likesCount + 1,
            isLikedByMe = !share.isLikedByMe
        )
        fitnessDao.updateShare(updated)
    }

    suspend fun clearLeaderboard() = withContext(Dispatchers.IO) {
        fitnessDao.clearLeaderboard()
    }

    suspend fun insertLeaderboard(entries: List<LeaderboardEntry>) = withContext(Dispatchers.IO) {
        fitnessDao.insertLeaderboard(entries)
    }

    // Pre-populate DB with premium mock data
    suspend fun populateInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        // Ensure default preferences exist
        val defaultPrefs = UserPreferences(id = 0, userName = "Marley Bro", onboardingCompleted = false)
        fitnessDao.saveUserPreferences(defaultPrefs)

        // Seed Leaderboard
        val board = listOf(
            LeaderboardEntry(rank = 1, name = "Alex Rivera", score = 2450, badge = "🔥 Cardio Beast", avatarColorHex = "#FF4CAF50"),
            LeaderboardEntry(rank = 2, name = "Marley Bro (You)", score = 1980, badge = "⭐ Heart King", avatarColorHex = "#FF00F2FE", isCurrentUser = true),
            LeaderboardEntry(rank = 3, name = "Jordan Lee", score = 1620, badge = "🧘 Yoga Master", avatarColorHex = "#FFFFEB3B"),
            LeaderboardEntry(rank = 4, name = "Sarah Connor", score = 1200, badge = "🏋️ Strength Pro", avatarColorHex = "#FFFF5722"),
            LeaderboardEntry(rank = 5, name = "Taylor Swift", score = 950, badge = "🏃 Swift Runner", avatarColorHex = "#FFE91E63")
        )
        fitnessDao.insertLeaderboard(board)

        // Seed Community Share Feed
        val feeds = listOf(
            CommunityShare(
                authorName = "Alex Rivera",
                message = "Crushed my morning HIIT cardio routine! Heart rate hit 174 BPM peak. Keep pushing!",
                workoutDetails = "HIIT Workout • 42 mins • 580 kcal • 155 bpm avg",
                likesCount = 14,
                avatarColorHex = "#FF4CAF50"
            ),
            CommunityShare(
                authorName = "Jordan Lee",
                message = "Slowing it down with some deep breathing and core stretches. Absolute mindfulness.",
                workoutDetails = "Yoga • 30 mins • 110 kcal • 94 bpm avg",
                likesCount = 8,
                avatarColorHex = "#FFFFEB3B"
            )
        )
        fitnessDao.insertShares(feeds)
    }

    // --- Biometric Wearable Heart Rate Simulation ---
    // Simulates a wearable Bluetooth / ANT+ heart rate sensor stream.
    // Heart rate starts at baseline, increases during active workout, rests otherwise.
    fun simulateHeartRateStream(isWorkoutActive: Boolean, workoutType: String): Flow<Int> = flow {
        var baseBpm = if (isWorkoutActive) 110 else 72
        val maxBpm = when (workoutType) {
            "HIIT" -> 180
            "Running" -> 170
            "Cardio" -> 160
            "Strength" -> 140
            "Yoga" -> 105
            else -> 120
        }

        while (true) {
            if (isWorkoutActive) {
                // Drift up slowly towards max with noise
                if (baseBpm < maxBpm) {
                    baseBpm += Random.nextInt(1, 4)
                } else {
                    baseBpm += Random.nextInt(-2, 3)
                }
                // bound
                baseBpm = baseBpm.coerceIn(90, maxBpm)
            } else {
                // Drift down slowly to rest
                if (baseBpm > 72) {
                    baseBpm -= Random.nextInt(1, 3)
                } else {
                    baseBpm += Random.nextInt(-1, 2)
                }
                baseBpm = baseBpm.coerceIn(60, 85)
            }
            emit(baseBpm)
            delay(1000) // update every second
        }
    }.flowOn(Dispatchers.Default)

    // --- Google Fit & Apple Health Sync Simulation ---
    // Performs simulated sync. Triggers progress emissions from 0 to 100%.
    // When completed, syncs 3 mock historic workout items from Google Fit & Apple Health.
    fun syncHealthPlatform(platform: String): Flow<SyncState> = flow {
        emit(SyncState.Syncing(0f))
        for (progress in 10..100 step 15) {
            delay(300)
            emit(SyncState.Syncing(progress / 100f))
        }
        delay(200)

        // Inject simulated historic synced workouts
        val historicWorkouts = listOf(
            WorkoutLog(
                workoutType = "Running",
                durationMinutes = 25,
                avgHeartRate = 145,
                maxHeartRate = 168,
                caloriesBurned = 320,
                timestamp = System.currentTimeMillis() - 86400000 * 2, // 2 days ago
                aiInsights = "Synced via $platform. Excellent high-cadence pacing! Your aerobic threshold is improving beautifully."
            ),
            WorkoutLog(
                workoutType = "Strength",
                durationMinutes = 45,
                avgHeartRate = 118,
                maxHeartRate = 142,
                caloriesBurned = 280,
                timestamp = System.currentTimeMillis() - 86400000 * 5, // 5 days ago
                aiInsights = "Synced via $platform. Strong mechanical stress during lifting. Make sure to rest 48 hours before retraining those muscle groups."
            )
        )

        for (workout in historicWorkouts) {
            fitnessDao.insertWorkout(workout)
        }

        emit(SyncState.Completed)
    }.flowOn(Dispatchers.IO)

    // --- Gemini AI Personalized Workout Insights ---
    suspend fun getPersonalizedAiInsight(
        workoutType: String,
        duration: Int,
        avgHeartRate: Int,
        maxHeartRate: Int,
        calories: Int
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getLocalFallbackInsight(workoutType, avgHeartRate, calories)
        }

        val prompt = """
            You are an elite biometric fitness coach in the 'Real Fitness Tracking App'. 
            Marley Bro has completed a $workoutType workout with:
            - Duration: $duration minutes
            - Average Heart Rate: $avgHeartRate BPM
            - Max Heart Rate: $maxHeartRate BPM
            - Calories Burned: $calories kcal.
            
            Provide 2 highly direct, encouraging, elite athletic coaching tips for Marley Bro.
            Write it in first person ("I suggest...", "Your heart rate...").
            Focus directly on biometrics, safety, recovery and threshold progression.
            Keep the text extremely concise, action-focused, under 60 words total, formatted in 2 clean sentences or bullet points.
            Do not include introductory or sign-off fluff.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt))))
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                ?: getLocalFallbackInsight(workoutType, avgHeartRate, calories)
        } catch (e: Exception) {
            getLocalFallbackInsight(workoutType, avgHeartRate, calories)
        }
    }

    private fun getLocalFallbackInsight(workoutType: String, avgBpm: Int, calories: Int): String {
        return when (workoutType) {
            "HIIT" -> "Superb anaerobic capacity training! With an average of $avgBpm BPM, you spent key intervals building explosive power. Fuel up on electrolytes and aim for active mobility tomorrow."
            "Running" -> "Excellent aerobic cardiovascular conditioning. Burning $calories kcal at stable heart zones is perfect for strengthening your stroke volume. Stay hydrated, Marley Bro!"
            "Strength" -> "Phenomenal hypertrophy response. Keeping heart rates balanced around $avgBpm BPM shows great rest-to-work integrity. Focus on fast-absorbing protein for optimal fiber repair."
            "Yoga" -> "Mindful parasitic nerve suppression. Your steady resting heart patterns prove elite parasympathetic balance. Great job restoring flexibility and lung capacity!"
            else -> "Splendid cardiovascular load! Marley Bro, you logged an active $calories kcal burn with steady cardiac efficiency. Perfect discipline towards your milestone goals."
        }
    }
}

sealed interface SyncState {
    data class Syncing(val progress: Float) : SyncState
    object Completed : SyncState
    object Idle : SyncState
}
