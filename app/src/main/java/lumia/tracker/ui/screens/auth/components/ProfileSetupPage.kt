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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.BorderStroke
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lumia.tracker.viewmodel.ScholarViewModel
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.GlassCapsule
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.components.ScholarCardDefaults

@Composable
fun ProfileSetupPage(
    isActive: Boolean,
    viewModel: ScholarViewModel,
    onSaved: (String, String, String, String) -> Unit
) {
    val scale by animateFloatAsState(if (isActive) 1f else 0.85f, tween(600), label = "profile_scale")
    val alpha by animateFloatAsState(if (isActive) 1f else 0f, tween(600), label = "profile_alpha")
    val isDark = isSystemInDarkTheme()

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
        // Modern Form Title without cluttered introductory paragraph
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
        }

        // Live Real-Time ID Card Preview with Glassmorphic Styling
        ScholarCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(26.dp),
            containerColor = if (isDark) {
                MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.65f)
            } else {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
            },
            border = ScholarCardDefaults.glassBorder(isDark, accentColor = selectedThemeColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Top Header Row with Capsule Badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlassCapsule(
                        containerColor = selectedThemeColor.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, selectedThemeColor.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = "LUMIA COCKPIT ID",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = selectedThemeColor,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    GlassCapsule(
                        containerColor = selectedThemeColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, selectedThemeColor.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(selectedThemeColor)
                            )
                            Text(
                                text = starterTheme.uppercase(),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = selectedThemeColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Profile Avatar & Info Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Circular Avatar Ring
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(selectedThemeColor.copy(alpha = 0.12f))
                            .border(2.dp, selectedThemeColor.copy(alpha = 0.6f), CircleShape)
                            .padding(4.dp)
                            .clickable { pickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(selectedThemeColor.copy(alpha = 0.2f)),
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
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // User Details Info
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = name.ifBlank { "Main User" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(2.dp))

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

        // Capsule Action Button for Photo Upload
        GlassCapsule(
            containerColor = selectedThemeColor.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, selectedThemeColor.copy(alpha = 0.35f)),
            onClick = { pickerLauncher.launch("image/*") }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.PhotoCamera,
                    contentDescription = null,
                    tint = selectedThemeColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (selectedImagePath.isNotEmpty()) "Change Photo" else "Upload Photo",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = selectedThemeColor
                )
            }
        }

        // Form Fields in Glassmorphic Card
        ScholarCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            containerColor = if (isDark) {
                MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
            },
            border = ScholarCardDefaults.glassBorder(isDark)
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

        // Theme Selector in Glassmorphic Card
        ScholarCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            containerColor = if (isDark) {
                MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
            },
            border = ScholarCardDefaults.glassBorder(isDark)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Starter Theme",
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
