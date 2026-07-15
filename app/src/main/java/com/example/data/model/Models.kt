package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_preferences")
data class UserPreferences(
    @PrimaryKey val id: Int = 0,
    val userName: String = "Marley Bro",
    val onboardingCompleted: Boolean = false,
    val dailyRemindersEnabled: Boolean = true,
    val googleFitSynced: Boolean = false,
    val appleHealthSynced: Boolean = false,
    val profilePictureUri: String? = null
)

@Entity(tableName = "workout_logs")
data class WorkoutLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val workoutType: String, // "Cardio", "Strength", "HIIT", "Yoga", "Running"
    val durationMinutes: Int,
    val avgHeartRate: Int,
    val maxHeartRate: Int,
    val caloriesBurned: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val aiInsights: String? = null
)

@Entity(tableName = "leaderboard")
data class LeaderboardEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val rank: Int,
    val name: String,
    val score: Int, // calories burned this week
    val badge: String = "", // E.g., "🔥 Streak King", "❤️ Heart Master"
    val avatarColorHex: String = "#FF00F2FE",
    val isCurrentUser: Boolean = false
)

@Entity(tableName = "community_shares")
data class CommunityShare(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val authorName: String,
    val message: String,
    val workoutDetails: String,
    val likesCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val avatarColorHex: String = "#FFFF0844",
    val isLikedByMe: Boolean = false
)
