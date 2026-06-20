package lumia.tracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.viewmodel.ScholarViewModel
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyTextButton
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.Color
import lumia.tracker.model.AchievementSystem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileMenuScreen(navController: NavController, viewModel: ScholarViewModel) {
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    
    var showEditDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile & Menu", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Edit Profile")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(Modifier.height(16.dp))
                // Profile Summary Card with Streaks, Points and Achievements display
                val currentStreak by viewModel.currentStreak.collectAsStateWithLifecycle()
                
                val ranking = when (activeProfile.level) {
                    in 1..2 -> "Bronze Tier"
                    in 3..4 -> "Silver Tier"
                    in 5..9 -> "Gold Tier"
                    in 10..14 -> "Platinum Tier"
                    in 15..19 -> "Diamond Tier"
                    in 20..29 -> "Master Tier"
                    in 30..49 -> "Grandmaster Tier"
                    in 50..74 -> "Elite Tier"
                    in 75..99 -> "Legendary Tier"
                    else -> "Maximum Tier"
                }
                
                val rankingColor = when (activeProfile.level) {
                    in 1..2 -> Color(0xFFCD7F32) // Bronze
                    in 3..4 -> Color(0xFFC0C0C0) // Silver
                    in 5..9 -> Color(0xFFFFD700) // Gold
                    in 10..14 -> Color(0xFF4BC27D) // Emerald
                    in 15..19 -> Color(0xFF5BA7FF) // Diamond
                    in 20..29 -> Color(0xFF9888E4) // Vanguard
                    in 30..49 -> Color(0xFFFF4081) // Titan pinkish-red
                    in 50..74 -> Color(0xFFFFEB3B) // Cosmic bright-gold
                    in 75..99 -> Color(0xFF00E676) // Universal bright-green
                    else -> Color(0xFFE040FB) // Eternal neon-purple
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f))
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // No Dropdown Menu Since TopBar has Edit and MenuList items cover the rest

                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f), CircleShape)
                                        .clip(CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val isLocalImage = activeProfile.avatarEmoji.startsWith("/") || activeProfile.avatarEmoji.startsWith("file://") || activeProfile.avatarEmoji.startsWith("content://")
                                    if (isLocalImage) {
                                        coil.compose.AsyncImage(
                                            model = activeProfile.avatarEmoji,
                                            contentDescription = "Profile Picture",
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        val fallback = if (activeProfile.avatarEmoji.isNotBlank() && activeProfile.avatarEmoji.length <= 2 && activeProfile.avatarEmoji != "A" && activeProfile.avatarEmoji != "U") {
                                            activeProfile.avatarEmoji.uppercase()
                                        } else {
                                            activeProfile.name.take(2).uppercase()
                                        }
                                        Text(
                                            text = fallback,
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                                Spacer(Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = activeProfile.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    if (activeProfile.alias.isNotBlank()) {
                                        Text(
                                            text = "@${activeProfile.alias}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                            fontWeight = FontWeight.Bold
                                        )
                                    } else {
                                        Text(
                                            text = "No alias configured",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                                        )
                                    }
                                    
                                    if (activeProfile.selectedBadge.isNotBlank()) {
                                        val badgeDef = AchievementSystem.ACHIEVEMENTS.find { it.id == activeProfile.selectedBadge }
                                        if (badgeDef != null) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                modifier = Modifier
                                                    .padding(top = 4.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                val badgeIcon = when (badgeDef.iconEmoji) {
                                                    "Novice" -> Icons.Rounded.School
                                                    "Scroll" -> Icons.Rounded.MenuBook
                                                    "Cap" -> Icons.Rounded.School
                                                    "Crown" -> Icons.Rounded.MilitaryTech
                                                    "Star" -> Icons.Rounded.Star
                                                    "Check" -> Icons.Rounded.CheckCircle
                                                    "Timer" -> Icons.Rounded.Timer
                                                    "Fire" -> Icons.Rounded.Whatshot
                                                    "Book" -> Icons.Rounded.MenuBook
                                                    else -> Icons.Rounded.EmojiEvents
                                                }
                                                Icon(
                                                    imageVector = badgeIcon,
                                                    contentDescription = badgeDef.title,
                                                    tint = Color(0xFFFFD700), // Gold color for badge
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = badgeDef.title,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Black,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            // Ranking & streak badges
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFFF5722).copy(alpha = 0.2f))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Whatshot,
                                            contentDescription = "Streak",
                                            tint = Color(0xFFFF5722),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "$currentStreak Days",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
            
        // Progression removed


            item {
                // Personalization Card
                SettingsGroupCard(title = "Personalization", icon = Icons.Rounded.Palette) {
                    SettingsActionItemInCard(
                        title = "Appearance & Theme",
                        subtitle = "Themes, color palettes, and layout modifiers",
                        icon = Icons.Rounded.Palette,
                        onClick = { navController.navigate("settings/appearance") }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                SettingsGroupCard(title = "Profile Selection", icon = Icons.Rounded.Person) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    SettingsActionItemInCard(
                        title = "Switch Profile",
                        subtitle = "Currently using: ${activeProfile.name}",
                        icon = Icons.Rounded.SwapHoriz,
                        onClick = { 
                            // Tell main activity to show profile selector
                            val intent = android.content.Intent(context, lumia.tracker.MainActivity::class.java)
                            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            intent.putExtra("OPEN_PROFILE_SELECTOR", true)
                            if (context is android.app.Activity) {
                                context.finish()
                            }
                            context.startActivity(intent)
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                // System configuration
                SettingsGroupCard(title = "System Details", icon = Icons.Rounded.Settings) {
                    SettingsActionItemInCard(
                        title = "System Configuration",
                        subtitle = "Advanced background features and interconnections",
                        icon = Icons.Rounded.Settings,
                        onClick = { navController.navigate("settings/system") }
                    )
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                    
                    SettingsActionItemInCard(
                        title = "Experimental Features",
                        subtitle = "Quick tools and layouts",
                        icon = Icons.Rounded.Check,
                        onClick = { navController.navigate("settings/beta") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                // Safety & Notifications
                SettingsGroupCard(title = "Alerts & Security", icon = Icons.Rounded.Lock) {
                    SettingsActionItemInCard(
                        title = "Safety System Guard",
                        subtitle = "Automatic alerts and smart recommendations",
                        icon = Icons.Rounded.Lock,
                        onClick = { navController.navigate("settings/safety") }
                    )
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                    SettingsActionItemInCard(
                        title = "Notifications Management",
                        subtitle = "Tones, schedules, and active alerts",
                        icon = Icons.Rounded.Notifications,
                        onClick = { navController.navigate("settings/notifications") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                // Data Management
                SettingsGroupCard(title = "Storage & Versioning", icon = Icons.Rounded.Storage) {
                    SettingsActionItemInCard(
                        title = "Database & Management",
                        subtitle = "Manage secure active backups, exports & resets",
                        icon = Icons.Rounded.Storage,
                        onClick = { navController.navigate("settings/data") }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                    SettingsActionItemInCard(
                        title = "About App",
                        subtitle = "Developer info, update status & open source details",
                        icon = Icons.Rounded.Info,
                        onClick = { navController.navigate("settings/about") }
                    )
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }

    if (showEditDialog) {
        var editName by remember { mutableStateOf(activeProfile.name) }
        var editAlias by remember { mutableStateOf(activeProfile.alias) }
        var editImagePath by remember { mutableStateOf(activeProfile.avatarEmoji) }
        var editGamification by remember { mutableStateOf(activeProfile.gamificationEnabled) }
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
                    editImagePath = destFile.absolutePath
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Profile") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Display Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editAlias,
                        onValueChange = { editAlias = it },
                        label = { Text("Alias / Nickname") },
                        modifier = Modifier.fillMaxWidth()
                    )
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
                        val isLocal = editImagePath.startsWith("/") || editImagePath.startsWith("file://") || editImagePath.startsWith("content://")
                        if (isLocal) {
                            coil.compose.AsyncImage(
                                model = editImagePath,
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
                                Text("Choose Photo", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer, textAlign = TextAlign.Center)
                            }
                        }
                    }

                }
            },
            confirmButton = {
                BouncyButton(onClick = {
                    if (editName.isNotBlank()) {
                        viewModel.updateProfile(editName, editImagePath.ifBlank { editName.take(2).uppercase() }, editAlias, false)
                        showEditDialog = false
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                BouncyTextButton(onClick = { showEditDialog = false }) { Text("Cancel") }
            }
        )
    }
}