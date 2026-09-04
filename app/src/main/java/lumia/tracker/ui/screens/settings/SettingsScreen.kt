package lumia.tracker.ui.screens.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
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
import lumia.tracker.ui.components.BouncyOutlinedButton
import lumia.tracker.ui.components.BouncyTextButton
import lumia.tracker.sync.P2PSyncEngine
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.ui.screens.settings.components.SettingsActionItemInCard
import lumia.tracker.ui.screens.settings.components.SettingsGroupCard
import lumia.tracker.ui.screens.settings.components.SettingsToggleItem
import lumia.tracker.viewmodel.ScholarViewModel
import java.io.File
import java.io.FileOutputStream

/**
 * SettingsScreen - Clean, unified academic preferences and configuration hub.
 * Features a decluttered Hero Profile card, simplified preference groups with concise subtitles,
 * and a streamlined footer.
 */
@ValueScore(
    score = 96,
    importance = Importance.CRITICAL,
    description = "Academic settings hub with decluttered hero profile card and concise preference groups",
    category = "Settings"
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, viewModel: ScholarViewModel) {
    val context = LocalContext.current
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val allProfiles by viewModel.allProfiles.collectAsStateWithLifecycle()
    val syncEngine = remember { P2PSyncEngine.getInstance(context) }
    val isBackgroundSyncActive by syncEngine.isForegroundServiceEnabled.collectAsStateWithLifecycle()

    var showEditProfileSheet by remember { mutableStateOf(false) }
    var showSwitchProfileSheet by remember { mutableStateOf(false) }
    var showCreateProfileDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
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
                .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ==========================================
            // 1. HERO SCHOLAR PROFILE CARD
            // ==========================================
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Simplified Avatar Monogram / Photo
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        CircleShape
                                    )
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
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            Spacer(Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = activeProfile.name.ifBlank { "Scholar" },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (activeProfile.alias.isNotBlank()) {
                                    Spacer(Modifier.height(1.dp))
                                    Text(
                                        text = "@${activeProfile.alias}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Streamlined Active Profile Status Chip
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.VerifiedUser,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Active Profile",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = "${allProfiles.size} profile${if (allProfiles.size > 1) "s" else ""}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Streamlined Quick Action Buttons: Edit & Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BouncyOutlinedButton(
                                onClick = { showEditProfileSheet = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Edit Profile", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                            }

                            BouncyButton(
                                onClick = { showSwitchProfileSheet = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.SwapHoriz,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Switch Profile", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 2. PERSONALIZATION GROUP
            // ==========================================
            SettingsGroupCard(
                title = "Personalization",
                icon = Icons.Rounded.Palette
            ) {
                SettingsActionItemInCard(
                    title = "Appearance & Theme",
                    subtitle = "Themes, dark mode & layout",
                    icon = Icons.Rounded.Palette,
                    iconBgColor = Color(0xFF007AFF),
                    onClick = { navController.navigate("settings/appearance") }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                    modifier = Modifier.padding(start = 52.dp, end = 8.dp)
                )

                SettingsActionItemInCard(
                    title = "Streak Goals & Requirements",
                    subtitle = "Daily goals & streak targets",
                    icon = Icons.Rounded.LocalFireDepartment,
                    iconBgColor = Color(0xFFFF9500),
                    onClick = { navController.navigate("settings/streaks") }
                )
            }

            // ==========================================
            // 3. ACADEMIC & STUDY SYSTEM
            // ==========================================
            SettingsGroupCard(
                title = "Academic & Study System",
                icon = Icons.Rounded.School
            ) {
                SettingsActionItemInCard(
                    title = "Tag Management",
                    subtitle = "Organize academic tags",
                    icon = Icons.Rounded.LocalOffer,
                    iconBgColor = Color(0xFF30B0C7),
                    onClick = { navController.navigate("tags_hub") }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                    modifier = Modifier.padding(start = 52.dp, end = 8.dp)
                )

                SettingsActionItemInCard(
                    title = "System Configuration",
                    subtitle = "App behavior & defaults",
                    icon = Icons.Rounded.Tune,
                    iconBgColor = Color(0xFF5856D6),
                    onClick = { navController.navigate("settings/system") }
                )
            }

            // ==========================================
            // 4. SECURITY & ALERTS
            // ==========================================
            SettingsGroupCard(
                title = "Security & Alerts",
                icon = Icons.Rounded.Shield
            ) {
                SettingsActionItemInCard(
                    title = "Safety System Guard",
                    subtitle = "Biometrics & app security",
                    icon = Icons.Rounded.Security,
                    iconBgColor = Color(0xFFFF3B30),
                    onClick = { navController.navigate("settings/safety") }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                    modifier = Modifier.padding(start = 52.dp, end = 8.dp)
                )

                SettingsActionItemInCard(
                    title = "Notifications & Reminders",
                    subtitle = "Alerts & study alarms",
                    icon = Icons.Rounded.Notifications,
                    iconBgColor = Color(0xFF34C759),
                    onClick = { navController.navigate("settings/notifications") }
                )
            }

            // ==========================================
            // 5. DATA & CONNECTIVITY
            // ==========================================
            SettingsGroupCard(
                title = "Data & Connectivity",
                icon = Icons.Rounded.Storage
            ) {
                SettingsActionItemInCard(
                    title = "Device Sync",
                    subtitle = "Instant wireless sync between your devices",
                    icon = Icons.Rounded.Sync,
                    iconBgColor = Color(0xFFAF52DE),
                    onClick = { navController.navigate("settings/sync") }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                    modifier = Modifier.padding(start = 52.dp, end = 8.dp)
                )

                SettingsToggleItem(
                    title = "Background Sync",
                    subtitle = "Keep data synced even when the app is closed",
                    checked = isBackgroundSyncActive,
                    icon = Icons.Rounded.Sync,
                    iconBgColor = Color(0xFF5856D6),
                    onCheckedChange = { isEnabled ->
                        syncEngine.setForegroundServiceEnabled(isEnabled)
                    }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                    modifier = Modifier.padding(start = 52.dp, end = 8.dp)
                )

                SettingsActionItemInCard(
                    title = "Data & Backups",
                    subtitle = "Export, import & storage",
                    icon = Icons.Rounded.Storage,
                    iconBgColor = Color(0xFF8E8E93),
                    onClick = { navController.navigate("settings/data") }
                )
            }

            // ==========================================
            // 6. ABOUT & LABS
            // ==========================================
            SettingsGroupCard(
                title = "About & Labs",
                icon = Icons.Rounded.Science
            ) {
                SettingsActionItemInCard(
                    title = "Experimental Features & Labs",
                    subtitle = "Beta features & previews",
                    icon = Icons.Rounded.Science,
                    iconBgColor = Color(0xFFE040FB),
                    onClick = { navController.navigate("settings/beta") }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                    modifier = Modifier.padding(start = 52.dp, end = 8.dp)
                )

                SettingsActionItemInCard(
                    title = "About Lumia",
                    subtitle = "Version, build & licenses",
                    icon = Icons.Rounded.Info,
                    iconBgColor = Color(0xFF636366),
                    onClick = { navController.navigate("settings/about") }
                )
            }

            // ==========================================
            // 7. FOOTER SECTION
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Lumia Tracker • v1.0.7",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BouncyTextButton(onClick = { navController.navigate("settings/about") }) {
                        Text("Release Notes", style = MaterialTheme.typography.labelSmall)
                    }
                    Text("•", color = MaterialTheme.colorScheme.outlineVariant)
                    BouncyTextButton(onClick = { navController.navigate("settings/data") }) {
                        Text("Backups", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    // ==========================================
    // MODAL BOTTOM SHEET: EDIT PROFILE
    // ==========================================
    if (showEditProfileSheet) {
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

        ModalBottomSheet(
            onDismissRequest = { showEditProfileSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Edit Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    BouncyIconButton(onClick = { showEditProfileSheet = false }) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close")
                    }
                }

                // Avatar & Photo Upload
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                        .padding(14.dp),
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
                                text = editAvatar.ifBlank { editName.take(2).uppercase().ifBlank { "SC" } },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Avatar Photo",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Choose a photo or emoji",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    BouncyButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Rounded.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Upload", style = MaterialTheme.typography.labelMedium)
                    }
                }

                // Text fields
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text("Profile Name") },
                    placeholder = { Text("e.g. Alex Rivera") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                OutlinedTextField(
                    value = editAlias,
                    onValueChange = { editAlias = it },
                    label = { Text("Handle (Optional)") },
                    prefix = { Text("@") },
                    placeholder = { Text("username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                // Emoji Presets
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Emoji Presets",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val emojiList = listOf("🎓", "📚", "⚡", "🔬", "🚀", "💡", "🧠", "🎯", "💻", "✨", "🪐", "🔥")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(emojiList) { emoji ->
                            val isSelected = editAvatar == emoji
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickable { editAvatar = emoji }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(emoji, fontSize = 18.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BouncyOutlinedButton(
                        onClick = { showEditProfileSheet = false },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Cancel")
                    }

                    BouncyButton(
                        onClick = {
                            if (editName.isNotBlank()) {
                                viewModel.updateProfile(editName.trim(), editAvatar, editAlias.trim())
                                showEditProfileSheet = false
                            }
                        },
                        enabled = editName.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }

    // ==========================================
    // MODAL BOTTOM SHEET: SWITCH PROFILES
    // ==========================================
    if (showSwitchProfileSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSwitchProfileSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Sheet Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Switch Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    BouncyButton(
                        onClick = {
                            showSwitchProfileSheet = false
                            showCreateProfileDialog = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("New Profile", style = MaterialTheme.typography.labelMedium)
                    }
                }

                // Profile Items List
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    allProfiles.forEach { profile ->
                        val isCurrent = profile.id == activeProfile.id
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isCurrent) {
                                   showSwitchProfileSheet = false
                                   viewModel.switchProfileAndRestart(context, profile.id)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
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
                                            profile.avatarEmoji.ifBlank { profile.name.take(2).uppercase().ifBlank { "SC" } },
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                    }
                                }

                                Spacer(Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        profile.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
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

                Spacer(Modifier.height(4.dp))

                BouncyTextButton(
                    onClick = { showSwitchProfileSheet = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }
            }
        }
    }

    // ==========================================
    // ALERT DIALOG: CREATE NEW PROFILE
    // ==========================================
    if (showCreateProfileDialog) {
        var createName by remember { mutableStateOf("") }
        var createAlias by remember { mutableStateOf("") }
        var createAvatar by remember { mutableStateOf("🎓") }

        AlertDialog(
            onDismissRequest = { showCreateProfileDialog = false },
            title = {
                Text(
                    "Create Profile",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = createName,
                        onValueChange = { createName = it },
                        label = { Text("Profile Name") },
                        placeholder = { Text("e.g. Master's Thesis") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    OutlinedTextField(
                        value = createAlias,
                        onValueChange = { createAlias = it },
                        label = { Text("Handle (Optional)") },
                        prefix = { Text("@") },
                        placeholder = { Text("username") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Avatar Emoji",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        val emojiList = listOf("🎓", "📚", "⚡", "🔬", "🚀", "💡", "🧠", "🎯")
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(emojiList) { emoji ->
                                val isSelected = createAvatar == emoji
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clickable { createAvatar = emoji }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(emoji, fontSize = 16.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                BouncyButton(
                    onClick = {
                        if (createName.isNotBlank()) {
                            val newId = viewModel.createProfile(createName.trim(), createAvatar, createAlias.trim())
                            showCreateProfileDialog = false
                            viewModel.switchProfileAndRestart(context, newId)
                        }
                    },
                    enabled = createName.isNotBlank(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                BouncyTextButton(onClick = { showCreateProfileDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }
}
