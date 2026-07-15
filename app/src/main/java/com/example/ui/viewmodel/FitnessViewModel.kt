package com.example.ui.viewmodel

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.db.AppDatabase
import com.example.data.model.CommunityShare
import com.example.data.model.LeaderboardEntry
import com.example.data.model.UserPreferences
import com.example.data.model.WorkoutLog
import com.example.data.repository.FitnessRepository
import com.example.data.repository.SyncState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

class FitnessViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val database = AppDatabase.getDatabase(context)
    private val repository = FitnessRepository(database.fitnessDao())

    // --- DB Flow Expositions ---
    val userPreferences: StateFlow<UserPreferences?> = repository.userPreferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allWorkouts: StateFlow<List<WorkoutLog>> = repository.allWorkouts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val leaderboard: StateFlow<List<LeaderboardEntry>> = repository.leaderboard
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val communityShares: StateFlow<List<CommunityShare>> = repository.communityShares
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Onboarding Flow State ---
    private val _onboardingStep = MutableStateFlow(1)
    val onboardingStep = _onboardingStep.asStateFlow()

    // --- Interactive Live Workout States ---
    private val _isWorkoutActive = MutableStateFlow(false)
    val isWorkoutActive = _isWorkoutActive.asStateFlow()

    private val _selectedWorkoutType = MutableStateFlow("HIIT")
    val selectedWorkoutType = _selectedWorkoutType.asStateFlow()

    private val _liveHeartRate = MutableStateFlow(72)
    val liveHeartRate = _liveHeartRate.asStateFlow()

    private val _workoutDurationSeconds = MutableStateFlow(0)
    val workoutDurationSeconds = _workoutDurationSeconds.asStateFlow()

    private val _workoutCalories = MutableStateFlow(0f)
    val workoutCalories = _workoutCalories.asStateFlow()

    private val _aiInsightStatus = MutableStateFlow<AiInsightState>(AiInsightState.Idle)
    val aiInsightStatus = _aiInsightStatus.asStateFlow()

    // --- Wearable Sync States ---
    private val _googleFitSyncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val googleFitSyncState = _googleFitSyncState.asStateFlow()

    private val _appleHealthSyncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val appleHealthSyncState = _appleHealthSyncState.asStateFlow()

    // --- Jobs for Heart Rate Stream & Live Timer ---
    private var heartRateJob: Job? = null
    private var timerJob: Job? = null

    // Heart rate values collected during current workout session (to calc average & max)
    private val sessionHeartRates = mutableListOf<Int>()

    init {
        // Create Notification Channel for Daily Reminders
        createNotificationChannel()

        // Populate initial data to give immediate professional feel
        viewModelScope.launch {
            repository.populateInitialDataIfEmpty()
        }

        // Start listening to background rest heart rate
        startRestingHeartRateStream()
    }

    private fun startRestingHeartRateStream() {
        heartRateJob?.cancel()
        heartRateJob = viewModelScope.launch {
            repository.simulateHeartRateStream(false, "Rest").collectLatest { bpm ->
                _liveHeartRate.value = bpm
            }
        }
    }

    // --- Onboarding Actions ---
    fun nextOnboardingStep() {
        if (_onboardingStep.value < 4) {
            _onboardingStep.value += 1
        } else {
            completeOnboarding()
        }
    }

    fun prevOnboardingStep() {
        if (_onboardingStep.value > 1) {
            _onboardingStep.value -= 1
        }
    }

    private fun completeOnboarding() {
        viewModelScope.launch {
            val currentPrefs = userPreferences.value ?: UserPreferences()
            repository.savePreferences(currentPrefs.copy(onboardingCompleted = true))
        }
    }

    fun updateProfilePicture(uriString: String?) {
        viewModelScope.launch {
            val currentPrefs = userPreferences.value ?: UserPreferences()
            repository.savePreferences(currentPrefs.copy(profilePictureUri = uriString))
        }
    }

    fun resetOnboarding() {
        viewModelScope.launch {
            _onboardingStep.value = 1
            val currentPrefs = userPreferences.value ?: UserPreferences()
            repository.savePreferences(currentPrefs.copy(onboardingCompleted = false))
        }
    }

    // --- Workout Control Actions ---
    fun selectWorkoutType(type: String) {
        if (!_isWorkoutActive.value) {
            _selectedWorkoutType.value = type
        }
    }

    fun startWorkout() {
        if (_isWorkoutActive.value) return
        _isWorkoutActive.value = true
        _workoutDurationSeconds.value = 0
        _workoutCalories.value = 0f
        sessionHeartRates.clear()

        // Cancel resting heart rate, start active workout heart rate stream
        heartRateJob?.cancel()
        heartRateJob = viewModelScope.launch {
            repository.simulateHeartRateStream(true, _selectedWorkoutType.value).collectLatest { bpm ->
                _liveHeartRate.value = bpm
                sessionHeartRates.add(bpm)
            }
        }

        // Start timer and calories accumulator
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            val calPerSec = when (_selectedWorkoutType.value) {
                "HIIT" -> 12f / 60f
                "Running" -> 10f / 60f
                "Cardio" -> 8f / 60f
                "Strength" -> 6f / 60f
                "Yoga" -> 3f / 60f
                else -> 5f / 60f
            }
            while (true) {
                delay(1000)
                _workoutDurationSeconds.value += 1
                _workoutCalories.value += calPerSec + Random.nextFloat() * 0.02f // slight variation
            }
        }
    }

    fun stopWorkout() {
        if (!_isWorkoutActive.value) return
        _isWorkoutActive.value = false

        timerJob?.cancel()
        timerJob = null

        val finalDurationSec = _workoutDurationSeconds.value
        val finalCalories = _workoutCalories.value.roundToInt()
        val rates = sessionHeartRates.toList()

        // Revert to resting heart rate stream
        startRestingHeartRateStream()

        if (finalDurationSec < 5) {
            // Workout too short, don't save
            return
        }

        val avgHeartRate = if (rates.isNotEmpty()) rates.average().roundToInt() else 115
        val maxHeartRate = if (rates.isNotEmpty()) rates.maxOrNull() ?: 140 else 140
        val durationMinutes = (finalDurationSec / 60).coerceAtLeast(1)

        viewModelScope.launch {
            _aiInsightStatus.value = AiInsightState.Loading

            // 1. Insert Workout log
            val workout = WorkoutLog(
                workoutType = _selectedWorkoutType.value,
                durationMinutes = durationMinutes,
                avgHeartRate = avgHeartRate,
                maxHeartRate = maxHeartRate,
                caloriesBurned = finalCalories,
                timestamp = System.currentTimeMillis()
            )
            val insertedId = repository.insertWorkout(workout)

            // 2. Fetch Personalized AI Insights with Gemini
            val insight = repository.getPersonalizedAiInsight(
                workoutType = workout.workoutType,
                duration = workout.durationMinutes,
                avgHeartRate = workout.avgHeartRate,
                maxHeartRate = workout.maxHeartRate,
                calories = workout.caloriesBurned
            )

            // 3. Update workout log with Gemini insight
            repository.insertWorkout(workout.copy(id = insertedId.toInt(), aiInsights = insight))

            _aiInsightStatus.value = AiInsightState.Success(insight)

            // Post real status bar notification for achieving milestone
            triggerPushNotification(
                "Workout Accomplished! 🎉",
                "Marley Bro, you completed $durationMinutes min of ${workout.workoutType}! Burned $finalCalories kcal."
            )

            // Boost leaderboard score for Marley Bro
            boostLeaderboardScore(finalCalories)
        }
    }

    private fun boostLeaderboardScore(caloriesBurned: Int) {
        viewModelScope.launch {
            val list = leaderboard.value.map { entry ->
                if (entry.isCurrentUser) {
                    entry.copy(score = entry.score + caloriesBurned)
                } else {
                    entry
                }
            }
            repository.clearLeaderboard()
            repository.insertLeaderboard(list)
        }
    }

    // --- Social Sharing Actions ---
    fun shareWorkoutToFeed(message: String, workout: WorkoutLog) {
        viewModelScope.launch {
            val share = CommunityShare(
                authorName = "Marley Bro (You)",
                message = message,
                workoutDetails = "${workout.workoutType} • ${workout.durationMinutes} mins • ${workout.caloriesBurned} kcal • ${workout.avgHeartRate} bpm avg",
                likesCount = 0,
                avatarColorHex = "#FF00F2FE"
            )
            repository.addShare(share)
        }
    }

    fun toggleLikeShare(share: CommunityShare) {
        viewModelScope.launch {
            repository.likeShare(share)
        }
    }

    // --- Health Sync Toggles ---
    fun toggleGoogleFitSync() {
        viewModelScope.launch {
            val currentPrefs = userPreferences.value ?: UserPreferences()
            if (currentPrefs.googleFitSynced) {
                // disconnect
                repository.savePreferences(currentPrefs.copy(googleFitSynced = false))
            } else {
                // start syncing progress
                repository.syncHealthPlatform("Google Fit").collect { state ->
                    _googleFitSyncState.value = state
                    if (state is SyncState.Completed) {
                        repository.savePreferences(
                            (userPreferences.value ?: UserPreferences()).copy(googleFitSynced = true)
                        )
                        _googleFitSyncState.value = SyncState.Idle
                    }
                }
            }
        }
    }

    fun toggleAppleHealthSync() {
        viewModelScope.launch {
            val currentPrefs = userPreferences.value ?: UserPreferences()
            if (currentPrefs.appleHealthSynced) {
                // disconnect
                repository.savePreferences(currentPrefs.copy(appleHealthSynced = false))
            } else {
                // start syncing progress
                repository.syncHealthPlatform("Apple Health").collect { state ->
                    _appleHealthSyncState.value = state
                    if (state is SyncState.Completed) {
                        repository.savePreferences(
                            (userPreferences.value ?: UserPreferences()).copy(appleHealthSynced = true)
                        )
                        _appleHealthSyncState.value = SyncState.Idle
                    }
                }
            }
        }
    }

    // --- Notification Configurations & Action ---
    fun toggleDailyReminders() {
        viewModelScope.launch {
            val currentPrefs = userPreferences.value ?: UserPreferences()
            val nextState = !currentPrefs.dailyRemindersEnabled
            repository.savePreferences(currentPrefs.copy(dailyRemindersEnabled = nextState))
            if (nextState) {
                triggerPushNotification("Daily Reminders Enabled 🔔", "We'll keep you accountable on your fitness goals!")
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Real Fitness Notifications"
            val descriptionText = "Workout updates and daily reminders"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("real_fitness_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun triggerPushNotification(title: String, body: String) {
        val currentPrefs = userPreferences.value ?: UserPreferences()
        if (!currentPrefs.dailyRemindersEnabled) return

        val builder = NotificationCompat.Builder(context, "real_fitness_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(Random.nextInt(1000, 9999), builder.build())
    }

    override fun onCleared() {
        super.onCleared()
        heartRateJob?.cancel()
        timerJob?.cancel()
    }
}

sealed interface AiInsightState {
    object Idle : AiInsightState
    object Loading : AiInsightState
    data class Success(val insight: String) : AiInsightState
}

class FitnessViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FitnessViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FitnessViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
