package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WorkoutLog
import com.example.ui.theme.BorderColor
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonLime
import com.example.ui.theme.RealSurface
import com.example.ui.theme.RealSurfaceVariant
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.FitnessViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TrendsScreen(
    viewModel: FitnessViewModel,
    modifier: Modifier = Modifier
) {
    val workouts by viewModel.allWorkouts.collectAsState()

    // Aggregate monthly statistics
    val totalWorkouts = workouts.size
    val totalCalories = workouts.sumOf { it.caloriesBurned }
    val avgBpm = if (workouts.isNotEmpty()) workouts.map { it.avgHeartRate }.average().toInt() else 0

    // Simulated weekly calorie data for custom monthly trends chart (e.g., 4 weeks)
    // We add current logged workout values to Week 4 to make the chart react live!
    val currentWeekUserCal = workouts.filter {
        it.timestamp > System.currentTimeMillis() - 7 * 86400000
    }.sumOf { it.caloriesBurned }

    val weeklyCals = listOf(
        1450f, // Week 1
        1920f, // Week 2
        1100f, // Week 3
        1200f + currentWeekUserCal.toFloat() // Week 4 (Reacts live!)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Stats Cards Grid
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(90.dp),
                colors = CardDefaults.cardColors(containerColor = RealSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("TOTAL ACTIVE", color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$totalWorkouts logs",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = NeonCyan
                    )
                }
            }
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(90.dp),
                colors = CardDefaults.cardColors(containerColor = RealSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("MONTHLY BURN", color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$totalCalories kcal",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = NeonLime
                    )
                }
            }
        }

        // Custom Monthly Trends Canvas Chart
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            colors = CardDefaults.cardColors(containerColor = RealSurface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TrendingUp, contentDescription = "Trends", tint = NeonCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Monthly Trends",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }
                    Text(
                        text = "Calories Burn (kcal)",
                        color = TextGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Smooth Custom Progress Bars in Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val maxCal = 3000f
                        val barWidth = 45.dp.toPx()
                        val spacing = (size.width - (barWidth * 4)) / 5

                        val weeks = listOf("Week 1", "Week 2", "Week 3", "Week 4")

                        repeat(4) { idx ->
                            val value = weeklyCals[idx]
                            val barHeight = (value / maxCal) * size.height
                            val startX = spacing + idx * (barWidth + spacing)
                            val startY = size.height - barHeight

                            // Draw beautiful rounded linear gradient bar
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(NeonCyan, NeonLime)
                                ),
                                topLeft = Offset(startX, startY),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                            )
                        }
                    }
                }

                // X-Axis Labels
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Text("Week 1", color = TextGray, fontSize = 11.sp)
                    Text("Week 2", color = TextGray, fontSize = 11.sp)
                    Text("Week 3", color = TextGray, fontSize = 11.sp)
                    Text("Week 4", color = NeonLime, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Historic Logs list
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "WORKOUT HISTORY",
                style = MaterialTheme.typography.labelMedium,
                color = TextGray,
                fontWeight = FontWeight.ExtraBold
            )
            Button(
                onClick = { viewModel.resetOnboarding() },
                colors = ButtonDefaults.buttonColors(containerColor = RealSurface, contentColor = TextGray),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.testTag("reset_onboarding_button")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset Tutorial", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Restart Onboarding", fontSize = 10.sp)
            }
        }

        if (workouts.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(containerColor = RealSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsRun,
                        contentDescription = "Run",
                        tint = TextGray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No workout logs available", color = TextWhite, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Complete your first workout to see trends",
                        color = TextGray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(workouts) { workout ->
                    WorkoutHistoryRow(workout)
                }
            }
        }
    }
}

@Composable
fun WorkoutHistoryRow(workout: WorkoutLog) {
    var expanded by remember { mutableStateOf(false) }

    val icon = when (workout.workoutType) {
        "Running" -> Icons.Default.DirectionsRun
        "Strength" -> Icons.Default.FitnessCenter
        "Yoga" -> Icons.Default.SelfImprovement
        "Cardio" -> Icons.Default.Pool
        else -> Icons.Default.DirectionsRun
    }

    val iconColor = when (workout.workoutType) {
        "HIIT" -> NeonCrimson
        "Running" -> NeonCyan
        "Yoga" -> NeonLime
        else -> NeonCyan
    }

    val dateStr = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
        .format(Date(workout.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .testTag("workout_log_item_${workout.id}"),
        colors = CardDefaults.cardColors(containerColor = RealSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(iconColor.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = icon, contentDescription = "Workout", tint = iconColor, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(workout.workoutType, color = TextWhite, fontWeight = FontWeight.Bold)
                        Text(dateStr, color = TextGray, fontSize = 11.sp)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("${workout.caloriesBurned} kcal", color = NeonLime, fontWeight = FontWeight.Black)
                        Text("${workout.durationMinutes} mins • Avg ${workout.avgHeartRate} bpm", color = TextGray, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowDown else Icons.Default.ChevronRight,
                        contentDescription = "Expand",
                        tint = TextGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(RealSurfaceVariant)
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI Insight", tint = NeonCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Gemini Recovery Insights", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = workout.aiInsights ?: "No feedback cached for this training log.",
                        color = TextWhite,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
