package lumia.tracker.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lumia.tracker.viewmodel.ScholarViewModel

@Composable
fun ProfileSetupPage(
    isActive: Boolean,
    viewModel: ScholarViewModel,
    onSaved: (String, String, String, String) -> Unit
) {
    val scale by animateFloatAsState(if (isActive) 1f else 0.85f, tween(600), label = "profile_scale")
    val alpha by animateFloatAsState(if (isActive) 1f else 0f, tween(600), label = "profile_alpha")

    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("Main User") }
    var alias by remember { mutableStateOf("Student") }
    var starterTheme by remember { mutableStateOf("Ocean") }
    var selectedImagePath by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(activeProfile) {
        if (activeProfile.name.isNotBlank()) {
            name = activeProfile.name
        }
        if (activeProfile.alias.isNotBlank()) {
            alias = activeProfile.alias
        }
        if (activeProfile.starterTheme.isNotBlank()) {
            starterTheme = activeProfile.starterTheme
        }
        selectedImagePath = if (activeProfile.avatarEmoji.startsWith("/") || activeProfile.avatarEmoji.startsWith("file://") || activeProfile.avatarEmoji.startsWith("content://")) activeProfile.avatarEmoji else ""
    }
    
    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val avatarDir = java.io.File(context.filesDir, "avatars").apply { mkdirs() }
                val destFile = java.io.File(avatarDir, "profile_avatar_${System.currentTimeMillis()}.jpg")
                val outputStream = java.io.FileOutputStream(destFile)
                inputStream?.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                selectedImagePath = destFile.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Trigger saved state whenever user changes input
    LaunchedEffect(name, alias, starterTheme, selectedImagePath) {
        val finalAvatar = selectedImagePath.ifBlank { name.take(2).uppercase().ifBlank { "ME" } }
        onSaved(name, alias, starterTheme, finalAvatar)
    }

    val themesList = listOf(
        "Ocean" to Color(0xFF3197D6),
        "Emerald" to Color(0xFF4BC27D),
        "Gold" to Color(0xFFFFC646),
        "Rose" to Color(0xFFE52F28),
        "Sage" to Color(0xFFACBDAA),
        "Twilight" to Color(0xFF958CE8)
    )

    val selectedThemeColor = themesList.find { it.first == starterTheme }?.second ?: Color(0xFF3197D6)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .scale(scale)
            .alpha(alpha),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Form Title
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Text(
                text = "Identity & Style",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Configure your cockpit details and select a theme aesthetic to begin tracking your academics.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }

        // Live Real-Time ID Card Preview
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = selectedThemeColor.copy(alpha = 0.5f)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(24.dp))
            ) {
                // Subtle color gradient bar representing theme color
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(8.dp)
                        .background(selectedThemeColor)
                        .align(Alignment.CenterStart)
                )

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 24.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Clickable Avatar circle
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(selectedThemeColor.copy(alpha = 0.15f))
                            .border(1.5.dp, selectedThemeColor, CircleShape)
                            .clickable { pickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImagePath.isNotEmpty()) {
                            coil.compose.AsyncImage(
                                model = selectedImagePath,
                                contentDescription = "Avatar Preview",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Face,
                                    contentDescription = "Upload",
                                    tint = selectedThemeColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    "ADD",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = selectedThemeColor,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 8.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Right: User Details Info
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "LUMIA COCKPIT ID",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = selectedThemeColor,
                                letterSpacing = 1.sp
                            )
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(selectedThemeColor.copy(alpha = 0.1f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = starterTheme.uppercase(),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = selectedThemeColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = name.ifBlank { "Main User" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = alias.ifBlank { "Lumia Student" },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Action guidance to upload
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { pickerLauncher.launch("image/*") }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.PhotoCamera,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Tap preview image circle to upload profile photo",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Form fields Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Profile Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display Name") },
                    singleLine = true,
                    placeholder = { Text("e.g. Rachel") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = selectedThemeColor,
                        focusedLabelColor = selectedThemeColor
                    )
                )

                OutlinedTextField(
                    value = alias,
                    onValueChange = { alias = it },
                    label = { Text("Alias / Nickname") },
                    singleLine = true,
                    placeholder = { Text("e.g. Academic Warrior") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = selectedThemeColor,
                        focusedLabelColor = selectedThemeColor
                    )
                )
            }
        }

        // Theme Selector Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f)
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Choose Your Starter Theme",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    themesList.forEach { (tName, tColor) ->
                        val isSelected = starterTheme == tName
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(tColor)
                                .clickable { starterTheme = tName }
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Rounded.Check,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}
