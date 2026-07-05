package lumia.tracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Photo
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import lumia.tracker.model.UserProfile
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyTextButton
import androidx.compose.ui.text.style.TextAlign
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

@Composable
fun ProfileItem(profile: UserProfile, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                val isLocalImage = profile.avatarEmoji.startsWith("/") || profile.avatarEmoji.startsWith("file://") || profile.avatarEmoji.startsWith("content://")
                if (isLocalImage) {
                    coil.compose.AsyncImage(
                        model = profile.avatarEmoji,
                        contentDescription = profile.name,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    val fallback = if (profile.avatarEmoji.isNotBlank() && profile.avatarEmoji.length <= 2 && profile.avatarEmoji != "A" && profile.avatarEmoji != "U") {
                        profile.avatarEmoji.uppercase()
                    } else {
                        profile.name.take(2).uppercase()
                    }
                    Text(
                        text = fallback,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            profile.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
    }
}
