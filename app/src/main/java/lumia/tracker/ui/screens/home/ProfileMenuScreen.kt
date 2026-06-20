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
                // Profile Summary Card with Triple Dots, Streaks, Points and Achievements display
                var showDropdownMenu by remember { mutableStateOf(false) }
                val currentStreak by viewModel.currentStreak.collectAsStateWithLifecycle()
                
                val ranking = when (activeProfile.level) {
                    in 1..2 -> "Bronze Scholar"
                    in 3..4 -> "Silver Spark"
                    in 5..9 -> "Gold Sage"
                    in 10..14 -> "Emerald Master"
                    in 15..19 -> "Diamond Lord"
                    in 20..29 -> "Scholar Vanguard"
                    in 30..49 -> "Immortal Titan"
                    in 50..74 -> "Cosmic Sovereign"
                    in 75..99 -> "Universal Legend"
                    else -> "Eternal Luminary"
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
                        // Dropdown Menu (on Three Dots Tapped)
                        Box(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)) {
                            IconButton(onClick = { showDropdownMenu = true }) {
                                Icon(
                                    Icons.Rounded.MoreVert,
                                    contentDescription = "Options",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            DropdownMenu(
                                expanded = showDropdownMenu,
                                onDismissRequest = { showDropdownMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Profile Editing") },
                                    leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) },
                                    onClick = {
                                        showDropdownMenu = false
                                        showEditDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Level Rewards") },
                                    leadingIcon = { Icon(Icons.Rounded.Star, contentDescription = null) },
                                    onClick = {
                                        showDropdownMenu = false
                                        navController.navigate("level_rewards")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Achievements") },
                                    leadingIcon = { Icon(Icons.Rounded.EmojiEvents, contentDescription = null) },
                                    onClick = {
                                        showDropdownMenu = false
                                        navController.navigate("achievements_screen")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Plus Shop") },
                                    leadingIcon = { Icon(Icons.Rounded.Storefront, contentDescription = null) },
                                    onClick = {
                                        showDropdownMenu = false
                                        navController.navigate("plus_shop")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Switch User") },
                                    leadingIcon = { Icon(Icons.Rounded.SwapHoriz, contentDescription = null) },
                                    onClick = {
                                        showDropdownMenu = false
                                        navController.navigate("switch_user")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Settings") },
                                    leadingIcon = { Icon(Icons.Rounded.Settings, contentDescription = null) },
                                    onClick = {
                                        showDropdownMenu = false
                                        navController.navigate("settings")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("About App") },
                                    leadingIcon = { Icon(Icons.Rounded.Info, contentDescription = null) },
                                    onClick = {
                                        showDropdownMenu = false
                                        showAboutDialog = true
                                    }
                                )
                            }
                        }

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

                            // Level, ranking & streak badges
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Lv. ${activeProfile.level}",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(rankingColor.copy(alpha = 0.25f))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = ranking,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }

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

                            Spacer(Modifier.height(16.dp))

                            // Points display
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.MonetizationOn,
                                        contentDescription = "Points",
                                        tint = Color(0xFFFFC107),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "${activeProfile.points} Profile Points",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            // Achievements display progress
                            Text(
                                text = "Achievements: ${activeProfile.unlockedAchievements.size} / ${AchievementSystem.ACHIEVEMENTS.size} Badges",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                             )
                             Spacer(Modifier.height(6.dp))
                             LinearProgressIndicator(
                                 progress = { if (AchievementSystem.ACHIEVEMENTS.isNotEmpty()) activeProfile.unlockedAchievements.size.toFloat() / AchievementSystem.ACHIEVEMENTS.size else 0f },
                                 modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                                 color = MaterialTheme.colorScheme.onPrimaryContainer,
                                 trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.25f)
                             )
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
            
            item {
                val unlockedBadges = AchievementSystem.ACHIEVEMENTS.filter { activeProfile.unlockedAchievements.contains(it.id) }
                if (unlockedBadges.isNotEmpty()) {
                    Column(modifier = Modifier.padding(bottom = 24.dp)) {
                        Text(
                            text = "Unlocked Badge Shelf",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                        )
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Tap to equip active profile badge:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    unlockedBadges.forEach { badge ->
                                        val isSelected = activeProfile.selectedBadge == badge.id
                                        val badgeIcon = when (badge.iconEmoji) {
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
                                            Box(
                                                modifier = Modifier
                                                    .size(52.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (isSelected) MaterialTheme.colorScheme.primary
                                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                                                    )
                                                    .clickable {
                                                        viewModel.selectProfileBadge(if (isSelected) "" else badge.id)
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = badgeIcon,
                                                    contentDescription = badge.title,
                                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                    }
                                }
                                val selectedBadgeDef = AchievementSystem.ACHIEVEMENTS.find { it.id == activeProfile.selectedBadge }
                                if (selectedBadgeDef != null) {
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = "Active Badge: ${selectedBadgeDef.title} \u2014 ${selectedBadgeDef.description}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            item {
                Text("Progression", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 8.dp, bottom = 8.dp))
                MenuListItem(icon = Icons.Rounded.Star, title = "Level Rewards", subtitle = "View future level progression perks") {
                    navController.navigate("level_rewards")
                }
                MenuListItem(icon = Icons.Rounded.EmojiEvents, title = "Achievements", subtitle = "View unlocked and locked achievements") {
                    navController.navigate("achievements_screen")
                }
                MenuListItem(icon = Icons.Rounded.Storefront, title = "Plus Shop", subtitle = "Spend points to unlock Plus features") {
                    navController.navigate("plus_shop")
                }
                Spacer(Modifier.height(16.dp))
            }

            item {
                Text("App & Account", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 8.dp, bottom = 8.dp))
                MenuListItem(icon = Icons.Rounded.SwapHoriz, title = "Switch User", subtitle = "Change active profile or create new") {
                    // This can restart the app or just show a bottom sheet.
                    navController.navigate("switch_user")
                }
                MenuListItem(icon = Icons.Rounded.Settings, title = "Settings", subtitle = "Preferences and customization") {
                    navController.navigate("settings")
                }
                MenuListItem(icon = Icons.Rounded.Info, title = "About App", subtitle = "Version and info") {
                    showAboutDialog = true
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.RocketLaunch,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("About App", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        "Lumia FOSS Study Tracker",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("Version 2.4.0-foss", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Lumia is a 100% serverless, private, and customizable study assistant. All points, statistics, levels, and lists are maintained strictly on-device in an encrypted spatial architecture. Dive into Deep Pomodoro work with full security and elegant custom layouts.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                BouncyTextButton(onClick = { showAboutDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    if (showEditDialog) {
        var editName by remember { mutableStateOf(activeProfile.name) }
        var editAlias by remember { mutableStateOf(activeProfile.alias) }
        var editImagePath by remember { mutableStateOf(activeProfile.avatarEmoji) }
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
                        viewModel.updateProfile(editName, editImagePath.ifBlank { editName.take(2).uppercase() }, editAlias)
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