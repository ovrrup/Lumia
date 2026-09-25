package lumia.tracker.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lumia.tracker.model.UserProfile
import lumia.tracker.ui.components.GlassCapsule
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.components.ScholarCardDefaults

private fun getProfileThemeColor(themeName: String): Color {
    return when (themeName.lowercase()) {
        "emerald" -> Color(0xFF4BC27D)
        "gold" -> Color(0xFFFFC646)
        "rose" -> Color(0xFFE52F28)
        "sage" -> Color(0xFFACBDAA)
        "twilight" -> Color(0xFF958CE8)
        else -> Color(0xFF3197D6) // Ocean
    }
}

@Composable
fun ProfileItem(profile: UserProfile, onClick: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val themeColor = getProfileThemeColor(profile.starterTheme)

    ScholarCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        containerColor = if (isDark) {
            MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.65f)
        } else {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.80f)
        },
        border = ScholarCardDefaults.glassBorder(isDark, accentColor = themeColor),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Circular Avatar Ring
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(themeColor.copy(alpha = 0.12f))
                    .border(2.dp, themeColor.copy(alpha = 0.6f), CircleShape)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(themeColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    val isLocalImage = profile.avatarEmoji.startsWith("/") ||
                            profile.avatarEmoji.startsWith("file://") ||
                            profile.avatarEmoji.startsWith("content://")

                    if (isLocalImage) {
                        coil.compose.AsyncImage(
                            model = profile.avatarEmoji,
                            contentDescription = profile.name,
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        val fallback = if (profile.avatarEmoji.isNotBlank() &&
                            profile.avatarEmoji.length <= 2 &&
                            profile.avatarEmoji != "A" &&
                            profile.avatarEmoji != "U"
                        ) {
                            profile.avatarEmoji.uppercase()
                        } else {
                            profile.name.take(2).uppercase().ifBlank { "ME" }
                        }
                        Text(
                            text = fallback,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = themeColor
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Profile Info
            Text(
                text = profile.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            if (profile.alias.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = profile.alias,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(12.dp))

            // Capsule Action Button
            GlassCapsule(
                containerColor = themeColor.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, themeColor.copy(alpha = 0.4f)),
                onClick = onClick
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Open",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = themeColor
                    )
                    Icon(
                        imageVector = Icons.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = themeColor,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}
