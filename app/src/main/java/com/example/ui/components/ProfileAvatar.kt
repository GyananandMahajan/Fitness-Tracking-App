package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.NeonLime
import com.example.ui.theme.RealBg
import com.example.ui.theme.RealSurfaceVariant

@Composable
fun ProfileAvatar(
    profilePictureUri: String?,
    userName: String,
    modifier: Modifier = Modifier,
    size: Dp = 54.dp,
    onImageSelected: ((String) -> Unit)? = null
) {
    val context = LocalContext.current

    // Set up the gallery image launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Persist read URI permission if needed, otherwise convert to string
            onImageSelected?.invoke(it.toString())
        }
    }

    val isEditable = onImageSelected != null

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(NeonLime.copy(alpha = 0.25f), Color.Transparent)
                )
            )
            .border(2.dp, NeonLime, CircleShape)
            .clickable(enabled = isEditable) {
                launcher.launch("image/*")
            }
            .testTag("profile_avatar"),
        contentAlignment = Alignment.Center
    ) {
        if (!profilePictureUri.isNullOrEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(Uri.parse(profilePictureUri))
                    .crossfade(true)
                    .build(),
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        } else {
            // Gorgeous letter placeholder styled like the gold/neon sports theme
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(RealSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (userName.isNotBlank()) {
                    Text(
                        text = userName.trim().first().uppercaseChar().toString(),
                        color = NeonLime,
                        fontSize = (size.value * 0.45f).sp,
                        fontWeight = FontWeight.Black
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Default User",
                        tint = NeonLime,
                        modifier = Modifier.fillMaxSize(0.6f)
                    )
                }
            }
        }

        // Overlay small visual cue if editable
        if (isEditable) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f))
                    .clip(CircleShape),
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(bottom = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = "Upload Photo",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size((size.value * 0.25f).dp)
                    )
                }
            }
        }
    }
}
