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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSelectionScreen(
    profiles: List<UserProfile>,
    onProfileSelected: (String) -> Unit,
    onCreateProfile: (String, String, String, String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Who's using Lumia?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.widthIn(max = 400.dp)
        ) {
            items(profiles) { profile ->
                ProfileItem(
                    profile = profile,
                    onClick = { onProfileSelected(profile.id) }
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .clickable { showAddDialog = true }
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Rounded.Add,
                                contentDescription = "Add Profile",
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Add Profile",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var alias by remember { mutableStateOf("") }
        var starterTheme by remember { mutableStateOf("Ocean") }
        var selectedImagePath by remember { mutableStateOf("") }
        val context = androidx.compose.ui.platform.LocalContext.current

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

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Create Profile") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Profile Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = alias,
                        onValueChange = { alias = it },
                        label = { Text("Alias / Nickname") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))

                    Text("Select Starter Theme", style = MaterialTheme.typography.titleSmall, modifier = Modifier.align(Alignment.Start))
                    Spacer(Modifier.height(8.dp))
                    
                    val themesList = listOf(
                        "Ocean" to Color(0xFF3197D6),
                        "Emerald" to Color(0xFF4BC27D),
                        "Gold" to Color(0xFFFFC646),
                        "Rose" to Color(0xFFE52F28),
                        "Sage" to Color(0xFFACBDAA),
                        "Twilight" to Color(0xFF958CE8)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        themesList.forEach { (tName, tColor) ->
                            val isSelected = starterTheme == tName
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(tColor)
                                    .clickable { starterTheme = tName }
                                    .then(
                                        if (isSelected) Modifier.background(Color.White.copy(alpha = 0.3f), CircleShape)
                                        else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        Icons.Rounded.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    Text("Profile Picture", style = MaterialTheme.typography.titleSmall, modifier = Modifier.align(Alignment.Start))
                    Spacer(Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { pickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImagePath.isNotEmpty()) {
                            coil.compose.AsyncImage(
                                model = selectedImagePath,
                                contentDescription = "Preview",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Rounded.Photo,
                                    contentDescription = "Pick Photo",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(28.dp)
                                )
                                Text("Choose Device Photo", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                BouncyButton(onClick = {
                    if (name.isNotBlank()) {
                        onCreateProfile(name, selectedImagePath.ifBlank { name.take(2).uppercase() }, alias, starterTheme)
                        showAddDialog = false
                    }
                }) {
                    Text("Create")
                }
            },
            dismissButton = {
                BouncyTextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

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
