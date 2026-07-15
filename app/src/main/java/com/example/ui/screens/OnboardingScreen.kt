package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.NeonLime
import com.example.ui.theme.RealBg
import com.example.ui.theme.RealSurface
import com.example.ui.theme.RealSurfaceVariant
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite
import com.example.data.repository.SyncState
import com.example.ui.components.ProfileAvatar
import com.example.ui.viewmodel.FitnessViewModel
import kotlin.math.roundToInt

@Composable
fun OnboardingScreen(
    viewModel: FitnessViewModel,
    modifier: Modifier = Modifier
) {
    val step by viewModel.onboardingStep.collectAsState()
    val prefs by viewModel.userPreferences.collectAsState()
    var targetCalories by remember { mutableStateOf(600f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(RealBg)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with Steps Progress Indicators
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(36.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(4) { idx ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(6.dp)
                                .width(if (step == idx + 1) 24.dp else 8.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (step == idx + 1) NeonLime else RealSurfaceVariant)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "REAL FITNESS",
                    style = MaterialTheme.typography.labelLarge,
                    color = NeonLime,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
            }

            // Central Animated Steps Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when (step) {
                    1 -> OnboardingWelcomeStep(
                        profilePictureUri = prefs?.profilePictureUri,
                        userName = prefs?.userName ?: "Marley Bro",
                        onImageSelected = { viewModel.updateProfilePicture(it) }
                    )
                    2 -> OnboardingBiometricsStep(
                        targetCalories = targetCalories,
                        onCaloriesChange = { targetCalories = it }
                    )
                    3 -> OnboardingSyncStep(viewModel = viewModel)
                    4 -> OnboardingGetReadyStep()
                }
            }

            // Bottom Navigation Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step > 1) {
                    OutlinedButton(
                        onClick = { viewModel.prevOnboardingStep() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                        modifier = Modifier
                            .testTag("onboarding_back_button")
                            .height(54.dp)
                    ) {
                        Text("Back", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Spacer(modifier = Modifier.width(80.dp))
                }

                Button(
                    onClick = { viewModel.nextOnboardingStep() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonLime,
                        contentColor = RealBg
                    ),
                    modifier = Modifier
                        .testTag("onboarding_next_button")
                        .height(54.dp)
                        .weight(1f)
                        .padding(start = 16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (step == 4) "Get Started" else "Next Step",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = RealBg
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Next Step",
                            modifier = Modifier.size(18.dp),
                            tint = RealBg
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingWelcomeStep(
    profilePictureUri: String?,
    userName: String,
    onImageSelected: (String) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Highly attractive interactive profile picture selector
        Box(
            modifier = Modifier.size(170.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(NeonLime.copy(alpha = 0.25f), Color.Transparent),
                        center = center,
                        radius = size.width / 1.5f
                    )
                )
            }
            ProfileAvatar(
                profilePictureUri = profilePictureUri,
                userName = userName,
                size = 140.dp,
                onImageSelected = onImageSelected
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Tap circle to set profile photo",
            style = MaterialTheme.typography.labelMedium,
            color = NeonLime,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Welcome Marley Bro\nin Real Fitness",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            color = TextWhite,
            lineHeight = 36.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Your elite biometrics dashboard. Real-time heart rate sensor telemetry, Google Fit synchronizations, and advanced Gemini-driven workout coaching.",
            style = MaterialTheme.typography.bodyLarge,
            color = TextGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
            lineHeight = 24.sp
        )
    }
}

@Composable
fun OnboardingBiometricsStep(
    targetCalories: Float,
    onCaloriesChange: (Float) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = RealSurface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎯 Daily Active Target",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Adjust your daily active metabolic calorie burn standard.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "${targetCalories.roundToInt()} kcal",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = NeonLime
                )
                Spacer(modifier = Modifier.height(24.dp))
                Slider(
                    value = targetCalories,
                    onValueChange = onCaloriesChange,
                    valueRange = 200f..1500f,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonLime,
                        activeTrackColor = NeonLime,
                        inactiveTrackColor = RealSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("200 kcal", color = TextGray, fontSize = 12.sp)
                    Text("1500 kcal", color = TextGray, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun OnboardingSyncStep(viewModel: FitnessViewModel) {
    val prefs by viewModel.userPreferences.collectAsState()
    val gfSync by viewModel.googleFitSyncState.collectAsState()
    val ahSync by viewModel.appleHealthSyncState.collectAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "🔗 Cross-Platform Sync",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextWhite
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Securely synchronize workouts, pulse history, and metadata via Android Google Fit & Apple Health frameworks.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Google Fit Integration Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
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
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Google Fit",
                        tint = NeonLime,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Google Fit Sync", color = TextWhite, fontWeight = FontWeight.Bold)
                        Text(
                            text = when (gfSync) {
                                is SyncState.Syncing -> "Syncing data..."
                                else -> if (prefs?.googleFitSynced == true) "Connected" else "Disconnected"
                            },
                            color = if (prefs?.googleFitSynced == true) NeonLime else TextGray,
                            fontSize = 12.sp
                        )
                    }
                }
                Button(
                    onClick = { viewModel.toggleGoogleFitSync() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (prefs?.googleFitSynced == true) RealSurfaceVariant else NeonLime,
                        contentColor = if (prefs?.googleFitSynced == true) TextWhite else RealBg
                    ),
                    modifier = Modifier.testTag("google_fit_sync_onboarding")
                ) {
                    Text(
                        text = if (prefs?.googleFitSynced == true) "Disconnect" else "Connect",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Apple Health Integration Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
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
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Apple Health",
                        tint = NeonCyan,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Apple Health Sync", color = TextWhite, fontWeight = FontWeight.Bold)
                        Text(
                            text = when (ahSync) {
                                is SyncState.Syncing -> "Syncing data..."
                                else -> if (prefs?.appleHealthSynced == true) "Connected" else "Disconnected"
                            },
                            color = if (prefs?.appleHealthSynced == true) NeonCyan else TextGray,
                            fontSize = 12.sp
                        )
                    }
                }
                Button(
                    onClick = { viewModel.toggleAppleHealthSync() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (prefs?.appleHealthSynced == true) RealSurfaceVariant else NeonCyan,
                        contentColor = if (prefs?.appleHealthSynced == true) TextWhite else RealBg
                    ),
                    modifier = Modifier.testTag("apple_health_sync_onboarding")
                ) {
                    Text(
                        text = if (prefs?.appleHealthSynced == true) "Disconnect" else "Connect",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingGetReadyStep() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(NeonLime.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Alert",
                tint = NeonLime,
                modifier = Modifier.size(54.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Keep Accountable! 🔔",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = TextWhite
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "We will issue daily push notification reminders to ensure you complete your cardio thresholds, maintain heart-rate health, and conquer the community leaderboard.",
            style = MaterialTheme.typography.bodyLarge,
            color = TextGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
            lineHeight = 24.sp
        )
    }
}
