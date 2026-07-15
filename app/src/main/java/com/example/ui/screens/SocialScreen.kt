package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CommunityShare
import com.example.data.model.LeaderboardEntry
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.NeonLime
import com.example.ui.theme.RealBg
import com.example.ui.theme.RealSurface
import com.example.ui.theme.RealSurfaceVariant
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite
import com.example.ui.components.ProfileAvatar
import com.example.ui.viewmodel.FitnessViewModel

@Composable
fun SocialScreen(
    viewModel: FitnessViewModel,
    modifier: Modifier = Modifier
) {
    val leaderboard by viewModel.leaderboard.collectAsState()
    val feedShares by viewModel.communityShares.collectAsState()
    val workouts by viewModel.allWorkouts.collectAsState()
    val prefs by viewModel.userPreferences.collectAsState()

    val profilePictureUri = prefs?.profilePictureUri
    val userName = prefs?.userName ?: "Marley Bro"

    var selectedTab by remember { mutableIntStateOf(0) }
    var shareMessage by remember { mutableStateOf("") }

    val lastWorkout = workouts.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Tab row navigation
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = RealSurface,
            contentColor = TextWhite,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = NeonLime
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .padding(bottom = 16.dp)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Competitive Leaderboard", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                icon = { Icon(Icons.Default.Leaderboard, contentDescription = "Leaderboard", modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Community Feed", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                icon = { Icon(Icons.Default.Share, contentDescription = "Feed", modifier = Modifier.size(18.dp)) }
            )
        }

        if (selectedTab == 0) {
            // Competitive Leaderboard List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = "WEEKLY LEADERS",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextGray,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                items(leaderboard) { entry ->
                    LeaderboardCard(entry = entry, currentUserProfilePic = profilePictureUri)
                }
            }
        } else {
            // Community Feed & Status Share Box
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Post Draft Box (Visible if there is at least one completed workout log)
                item {
                    if (lastWorkout != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = RealSurface),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Share Your Last Session",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonLime
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Active: ${lastWorkout.workoutType} • ${lastWorkout.caloriesBurned} kcal • ${lastWorkout.durationMinutes} min",
                                    color = TextGray,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = shareMessage,
                                    onValueChange = { shareMessage = it },
                                    placeholder = { Text("Tell Marley Bro's friends about this milestone!", color = TextGray, fontSize = 13.sp) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("status_input"),
                                    maxLines = 2,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NeonLime,
                                        unfocusedBorderColor = RealSurfaceVariant,
                                        focusedTextColor = TextWhite,
                                        unfocusedTextColor = TextWhite
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        if (shareMessage.isNotBlank()) {
                                            viewModel.shareWorkoutToFeed(shareMessage, lastWorkout)
                                            shareMessage = ""
                                        }
                                    },
                                    enabled = shareMessage.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = NeonLime,
                                        contentColor = RealBg,
                                        disabledContainerColor = RealSurfaceVariant,
                                        disabledContentColor = TextGray
                                    ),
                                    modifier = Modifier
                                        .align(Alignment.End)
                                        .testTag("post_button")
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(16.dp), tint = RealBg)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Post to Feed", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = RealBg)
                                }
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = RealSurface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Complete an active workout to unlock social post sharing features!",
                                color = TextGray,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Community Shares List
                items(feedShares) { share ->
                    CommunityShareCard(
                        share = share,
                        currentUserProfilePic = profilePictureUri,
                        onLike = { viewModel.toggleLikeShare(share) }
                    )
                }
            }
        }
    }
}

@Composable
fun LeaderboardCard(entry: LeaderboardEntry, currentUserProfilePic: String?) {
    val highlightColor = when (entry.rank) {
        1 -> Color(0xFFFFD700) // Gold
        2 -> NeonLime
        3 -> Color(0xFFCD7F32) // Bronze
        else -> TextGray
    }

    val parsedBg = try { Color(android.graphics.Color.parseColor(entry.avatarColorHex)) } catch (e: Exception) { NeonLime }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("leaderboard_item_${entry.rank}"),
        colors = CardDefaults.cardColors(
            containerColor = if (entry.isCurrentUser) RealSurfaceVariant else RealSurface
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Rank Circle
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(highlightColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = entry.rank.toString(),
                        color = highlightColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Avatar Placeholder or real ProfileAvatar
                if (entry.isCurrentUser && currentUserProfilePic != null) {
                    ProfileAvatar(
                        profilePictureUri = currentUserProfilePic,
                        userName = entry.name,
                        size = 38.dp
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(parsedBg, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = entry.name.first().toString(),
                            color = RealBg,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = entry.name,
                            color = TextWhite,
                            fontWeight = if (entry.isCurrentUser) FontWeight.Black else FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        if (entry.isCurrentUser) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(NeonLime.copy(alpha = 0.15f))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                  Text("YOU", color = NeonLime, fontWeight = FontWeight.Bold, fontSize = 8.sp)
                            }
                        }
                    }
                    if (entry.badge.isNotBlank()) {
                        Text(entry.badge, color = TextGray, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${entry.score} kcal",
                    color = NeonLime,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
                Text("weekly active", color = TextGray, fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun CommunityShareCard(
    share: CommunityShare,
    currentUserProfilePic: String?,
    onLike: () -> Unit
) {
    val avatarBg = try { Color(android.graphics.Color.parseColor(share.avatarColorHex)) } catch (e: Exception) { NeonLime }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("community_post_${share.id}"),
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
                    val isMe = share.authorName.contains("You") || share.authorName.contains("Marley Bro")
                    if (isMe && currentUserProfilePic != null) {
                        ProfileAvatar(
                            profilePictureUri = currentUserProfilePic,
                            userName = "Marley Bro",
                            size = 36.dp
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(avatarBg, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = share.authorName.first().toString(),
                                color = RealBg,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(share.authorName, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Active Athlete", color = TextGray, fontSize = 10.sp)
                    }
                }

                // Workout Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NeonLime.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = share.workoutDetails.split("•").first().trim(),
                        color = NeonLime,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = share.message,
                color = TextWhite,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Session Stats Badge inside the post
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(RealSurfaceVariant)
                    .padding(8.dp)
            ) {
                Text(
                    text = share.workoutDetails,
                    color = TextGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Likes Counter & Tap feedback Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onLike,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (share.isLikedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (share.isLikedByMe) NeonCrimson else TextGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${share.likesCount} likes",
                    color = if (share.isLikedByMe) NeonCrimson else TextGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
