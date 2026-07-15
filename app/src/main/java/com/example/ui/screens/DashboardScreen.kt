package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderColor
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonLime
import com.example.ui.theme.RealBg
import com.example.ui.theme.RealSurface
import com.example.ui.theme.RealSurfaceVariant
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite
import com.example.ui.components.ProfileAvatar
import com.example.ui.viewmodel.AiInsightState
import com.example.ui.viewmodel.FitnessViewModel
import kotlin.math.sin

@Composable
fun DashboardScreen(
    viewModel: FitnessViewModel,
    modifier: Modifier = Modifier
) {
    val isWorkoutActive by viewModel.isWorkoutActive.collectAsState()
    val liveBpm by viewModel.liveHeartRate.collectAsState()
    val workoutType by viewModel.selectedWorkoutType.collectAsState()
    val durationSeconds by viewModel.workoutDurationSeconds.collectAsState()
    val calories by viewModel.workoutCalories.collectAsState()
    val aiInsightStatus by viewModel.aiInsightStatus.collectAsState()
    val lastWorkoutList by viewModel.allWorkouts.collectAsState()
    val prefs by viewModel.userPreferences.collectAsState()

    val profilePictureUri = prefs?.profilePictureUri
    val userName = prefs?.userName ?: "Marley Bro"
    val lastWorkout = lastWorkoutList.firstOrNull()

    val workoutTypes = listOf("HIIT", "Running", "Cardio", "Strength", "Yoga")

    // Infinite heartbeat pulsing animation
    val infiniteTransition = rememberInfiniteTransition(label = "heartbeat")
    val scaleFactor by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isWorkoutActive) 400 else 800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Dynamic Header welcoming Marley Bro
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = RealSurface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Welcome Marley Bro",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = TextWhite,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "in Real Fitness Tracking App",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = NeonLime
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(if (isWorkoutActive) NeonLime else TextGray, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isWorkoutActive) "Active Workout: $workoutType" else "Wearable Connected • Standing By",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGray
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                ProfileAvatar(
                    profilePictureUri = profilePictureUri,
                    userName = userName,
                    size = 64.dp,
                    onImageSelected = { viewModel.updateProfilePicture(it) }
                )
            }
        }

        // Animated Pulse Heart Rate Sensor Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = RealSurfaceVariant),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BIOMETRICS SENSOR",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextGray,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeonCrimson.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("LIVE FEED", color = NeonCrimson, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Pulsating Heart & Waveform UI
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .background(NeonCrimson.copy(alpha = 0.05f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(70.dp * scaleFactor)
                                .background(NeonCrimson.copy(alpha = 0.15f), CircleShape)
                        )
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Pulsing Heart",
                            tint = NeonCrimson,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(24.dp))

                    Column {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = liveBpm.toString(),
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Black,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "BPM",
                                style = MaterialTheme.typography.titleMedium,
                                color = NeonCrimson,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        Text(
                            text = if (liveBpm < 90) "Resting Pace" else "Cardio Threshold",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Custom Canvas ECG Waveform drawing
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    val pulseFactor = if (isWorkoutActive) 2.5f else 1.0f
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val path = Path()
                        val points = size.width.toInt()
                        path.moveTo(0f, size.height / 2f)

                        for (x in 0..points) {
                            val rawY = sin(x * 0.05f) * size.height / 4f
                            // Generate ECG styled sharp triggers
                            val ecgPulse = if (x % 160 in 65..75) {
                                if (x % 160 == 70) -size.height * 0.4f else size.height * 0.3f
                            } else {
                                rawY * 0.2f
                            }
                            path.lineTo(x.toFloat(), (size.height / 2f) + ecgPulse * pulseFactor)
                        }

                        drawPath(
                            path = path,
                            color = NeonCrimson,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }
            }
        }

        // Live Training Tracking Board (Activated or Deactivated)
        Text(
            text = "TRAINING WORKSPACE",
            style = MaterialTheme.typography.labelMedium,
            color = TextGray,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (!isWorkoutActive) {
            // Select Workout Type Row
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(workoutTypes) { type ->
                    val isSelected = workoutType == type
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) NeonLime else RealSurface)
                            .clickable { viewModel.selectWorkoutType(type) }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = type,
                            color = if (isSelected) RealBg else TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Core Workout Activation Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = RealSurface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isWorkoutActive) {
                    // Active Workout Stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val mins = durationSeconds / 60
                            val secs = durationSeconds % 60
                            val formattedTime = String.format("%02d:%02d", mins, secs)
                            Text(
                                text = formattedTime,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black,
                                color = NeonCyan
                            )
                            Text("DURATION", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${calories.toInt()} kcal",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black,
                                color = NeonLime
                            )
                            Text("METABOLIC BURN", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.stopWorkout() },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCrimson, contentColor = TextWhite),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("stop_workout_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = "Stop", modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("STOP WORKOUT & LOG", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                } else {
                    // Resting Ready to start
                    Text(
                        text = "Ready for your $workoutType session?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Button(
                        onClick = { viewModel.startWorkout() },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonLime, contentColor = RealBg),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("start_workout_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play", modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("START ACTIVE SESSION", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RealBg)
                    }
                }
            }
        }

        // Live AI Recovery Coach Card (Gemini)
        Text(
            text = "BIOMETRIC AI COACHING",
            style = MaterialTheme.typography.labelMedium,
            color = TextGray,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = RealSurfaceVariant),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Coach",
                        tint = NeonLime,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Gemini AI Recovery Coach",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                when (aiInsightStatus) {
                    is AiInsightState.Loading -> {
                        Text(
                            text = "Analyzing biometric telemetry from your last training... Generating coach feedback...",
                            color = TextGray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    else -> {
                        val displayText = lastWorkout?.aiInsights
                            ?: "No sessions recorded yet today! Complete an active workout or trigger standard sync above to receive elite custom biometrics recommendations from your Gemini coach."
                        Text(
                            text = displayText,
                            color = if (lastWorkout?.aiInsights != null) TextWhite else TextGray,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        }

        // Daily Reminder push simulator button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = RealSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Bell",
                        tint = NeonLime,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Daily Reminder",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = {
                        viewModel.triggerPushNotification(
                            "Don't lose your streak! 🔥",
                            "Hey Marley Bro, it's time for your daily biometrics check! Open the app to log your session."
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RealSurfaceVariant, contentColor = NeonLime)
                ) {
                    Text("Simulate Notification", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NeonLime)
                }
            }
        }
    }
}
