package lumia.tracker.ui.screens.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.BouncyTextButton
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.screens.settings.components.SettingsActionItemInCard
import lumia.tracker.ui.screens.settings.components.SettingsGroupCard
import lumia.tracker.viewmodel.ScholarViewModel
import java.io.File
import java.io.FileOutputStream

/**
 * SettingsScreen - Clean, unified academic preferences and configuration hub.
 * Features an integrated profile header, zero circular shortcut loops,
 * and theme-consistent layout aligned with the rest of the application.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, viewModel: ScholarViewModel) {
    val context = LocalContext.current
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val allProfiles by viewModel.allProfiles.collectAsStateWithLifecycle()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showSwitchProfileDialog by remember { mutableStateOf(false) }
    var showCreateProfileDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Settings",
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    BouncyIconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Integrated Scholar Profile Hero Card
            ScholarCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Profile Avatar
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val isLocalImage = activeProfile.avatarEmoji.startsWith("/") ||
                                    activeProfile.avatarEmoji.startsWith("file://") ||
                                    activeProfile.avatarEmoji.startsWith("content://")
                            if (isLocalImage) {
                                AsyncImage(
                                    model = activeProfile.avatarEmoji,
                                    contentDescription = "Profile Picture",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                val fallback = if (activeProfile.avatarEmoji.isNotBlank() &&
                                    activeProfile.avatarEmoji.length <= 3 &&
                                    !activeProfile.avatarEmoji.startsWith("/")
                                ) {
                                    activeProfile.avatarEmoji.uppercase()
                                } else {
                                    activeProfile.name.take(2).uppercase().ifBlank { "SC" }
                                }
                                Text(
                                    text = fallback,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Spacer(Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = activeProfile.name.ifBlank { "Scholar" },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (activeProfile.alias.isNotBlank()) "@${activeProfile.alias}" else "Active Workspace",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Quick Actions: Edit & Switch
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            BouncyIconButton(onClick = { showEditProfileDialog = true }) {
                                Icon(
                                    Icons.Rounded.Edit,
                                    contentDescription = "Edit Profile",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            BouncyIconButton(onClick = { showSwitchProfileDialog = true }) {
                                Icon(
                                    Icons.Rounded.SwapHoriz,
                                    contentDescription = "Switch Profile",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.VerifiedUser,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Active Academic Profile • ${allProfiles.size} Profile${if (allProfiles.size > 1) "s" else ""} Available",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // 2. Personalization Group
            SettingsGroupCard(title = "Personalization", icon = Icons.Rounded.Palette) {
                SettingsActionItemInCard(
                    title = "Appearance & Theme",
                    subtitle = "Themes, AMOLED pure black, lighting & custom palettes",
                    icon = Icons.Rounded.Palette,
                    iconBgColor = Color(0xFF007AFF),
                    onClick = { navController.navigate("settings/appearance") }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    modifier = Modifier.padding(start = 48.dp)
                )

                SettingsActionItemInCard(
                    title = "Streak Goals & Requirements",
                    subtitle = "Daily targets, completion thresholds & motivational tones",
                    icon = Icons.Rounded.LocalFireDepartment,
                    iconBgColor = Color(0xFFFF9500),
                    onClick = { navController.navigate("settings/streaks") }
                )
            }

            // 3. Academic System Configuration
            SettingsGroupCard(title = "Academic & Study System", icon = Icons.Rounded.School) {
                SettingsActionItemInCard(
                    title = "Tag Management",
                    subtitle = "Customize tag colors and global taxonomies",
                    icon = Icons.Rounded.LocalOffer,
                    iconBgColor = Color(0xFF30B0C7),
                    onClick = { navController.navigate("tags_hub") }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    modifier = Modifier.padding(start = 48.dp)
                )

                SettingsActionItemInCard(
                    title = "System Configuration",
                    subtitle = "Course-subject linking, synergy scoring & Pomodoro defaults",
                    icon = Icons.Rounded.Settings,
                    iconBgColor = Color(0xFF5856D6),
                    onClick = { navController.navigate("settings/system") }
                )
            }

            // 4. Security & Notifications
            SettingsGroupCard(title = "Security & Alerts", icon = Icons.Rounded.Lock) {
                SettingsActionItemInCard(
                    title = "Safety System Guard",
                    subtitle = "App PIN lock, biometric protection & safety alerts",
                    icon = Icons.Rounded.Lock,
                    iconBgColor = Color(0xFFFF3B30),
                    onClick = { navController.navigate("settings/safety") }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    modifier = Modifier.padding(start = 48.dp)
                )

                SettingsActionItemInCard(
                    title = "Notifications & Reminders",
                    subtitle = "Notification channels, study alarms & reminders",
                    icon = Icons.Rounded.Notifications,
                    iconBgColor = Color(0xFF34C759),
                    onClick = { navController.navigate("settings/notifications") }
                )
            }

            // 5. Data & Connectivity
            SettingsGroupCard(title = "Data & Connectivity", icon = Icons.Rounded.Storage) {
                SettingsActionItemInCard(
                    title = "Multi-Device P2P Sync",
                    subtitle = "End-to-end encrypted device syncing with 1-time mutual pairing",
                    icon = Icons.Rounded.Autorenew,
                    iconBgColor = Color(0xFFAF52DE),
                    onClick = { navController.navigate("settings/sync") }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    modifier = Modifier.padding(start = 48.dp)
                )

                SettingsActionItemInCard(
                    title = "Data & Backups",
                    subtitle = "Export, import, database backups & complete resets",
                    icon = Icons.Rounded.Storage,
                    iconBgColor = Color(0xFF8E8E93),
                    onClick = { navController.navigate("settings/data") }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    modifier = Modifier.padding(start = 48.dp)
                )

                SettingsActionItemInCard(
                    title = "About Lumia",
                    subtitle = "Version v1.0.7, license & open source repository",
                    icon = Icons.Rounded.Info,
                    iconBgColor = Color(0xFF636366),
                    onClick = { navController.navigate("settings/about") }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        var editName by remember { mutableStateOf(activeProfile.name) }
        var editAlias by remember { mutableStateOf(activeProfile.alias) }
        var editAvatar by remember { mutableStateOf(activeProfile.avatarEmoji) }

        val imagePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val avatarDir = File(context.filesDir, "avatars").apply { mkdirs() }
                    val destFile = File(avatarDir, "profile_avatar_${System.currentTimeMillis()}.jpg")
                    val outputStream = FileOutputStream(destFile)
                    inputStream?.use { input ->
                        outputStream.use { output ->
                            input.copyTo(output)
                        }
                    }
                    editAvatar = destFile.absolutePath
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val isLocal = editAvatar.startsWith("/") || editAvatar.startsWith("file://") || editAvatar.startsWith("content://")
                            if (isLocal) {
                                AsyncImage(
                                    model = editAvatar,
                                    contentDescription = "Avatar",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = editAvatar.ifBlank { editName.take(2).uppercase() },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        BouncyTextButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                            Icon(Icons.Rounded.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Upload Photo")
                        }
                    }

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Profile Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = editAlias,
                        onValueChange = { editAlias = it },
                        label = { Text("Handle / Alias") },
                        prefix = { Text("@") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Text(
                        text = "Or choose Avatar Initial / Emoji:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val emojiList = listOf("🎓", "📚", "⚡", "🔬", "🚀", "💡", "🧠", "🎯", "💻", "✨")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(emojiList) { emoji ->
                            val isSelected = editAvatar == emoji
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clickable { editAvatar = emoji }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(emoji, fontSize = 16.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                BouncyButton(
                    onClick = {
                        if (editName.isNotBlank()) {
                            viewModel.updateProfile(editName, editAvatar, editAlias)
                            showEditProfileDialog = false
                        }
                    },
                    enabled = editName.isNotBlank()
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                BouncyTextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Switch / Manage Profiles Dialog
    if (showSwitchProfileDialog) {
        AlertDialog(
            onDismissRequest = { showSwitchProfileDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Scholar Profiles", fontWeight = FontWeight.Bold)
                    BouncyTextButton(onClick = {
                        showSwitchProfileDialog = false
                        showCreateProfileDialog = true
                    }) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add")
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    allProfiles.forEach { profile ->
                        val isCurrent = profile.id == activeProfile.id
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = if (isCurrent) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isCurrent) {
                                    showSwitchProfileDialog = false
                                    viewModel.switchProfileAndRestart(context, profile.id)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                                        .clip(CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val isLocal = profile.avatarEmoji.startsWith("/") || profile.avatarEmoji.startsWith("file://")
                                    if (isLocal) {
                                        AsyncImage(
                                            model = profile.avatarEmoji,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Text(
                                            profile.avatarEmoji.ifBlank { profile.name.take(2).uppercase() },
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }

                                Spacer(Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        profile.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (profile.alias.isNotBlank()) {
                                        Text(
                                            "@${profile.alias}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (isCurrent) {
                                    Icon(
                                        Icons.Rounded.CheckCircle,
                                        contentDescription = "Active",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                BouncyTextButton(onClick = { showSwitchProfileDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Create Profile Dialog
    if (showCreateProfileDialog) {
        var createName by remember { mutableStateOf("") }
        var createAlias by remember { mutableStateOf("") }
        var createAvatar by remember { mutableStateOf("🎓") }

        AlertDialog(
            onDismissRequest = { showCreateProfileDialog = false },
            title = { Text("Create New Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = createName,
                        onValueChange = { createName = it },
                        label = { Text("Profile Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = createAlias,
                        onValueChange = { createAlias = it },
                        label = { Text("Handle / Alias") },
                        prefix = { Text("@") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                BouncyButton(
                    onClick = {
                        if (createName.isNotBlank()) {
                            val newId = viewModel.createProfile(createName, createAvatar, createAlias)
                            showCreateProfileDialog = false
                            viewModel.switchProfileAndRestart(context, newId)
                        }
                    },
                    enabled = createName.isNotBlank()
                ) {
                    Text("Create & Switch")
                }
            },
            dismissButton = {
                BouncyTextButton(onClick = { showCreateProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
