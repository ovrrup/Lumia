package lumia.tracker.ui.screens.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import lumia.tracker.ui.screens.settings.components.SettingsActionItemInCard
import lumia.tracker.ui.screens.settings.components.SettingsGroupCard
import lumia.tracker.viewmodel.ScholarViewModel
import java.io.File
import java.io.FileOutputStream

/**
 * SettingsScreen - Clean, unified academic preferences and configuration hub.
 * Features an integrated Hero Profile card with live active badge,
 * organized categorized group cards, zero circular shortcut loops,
 * and a polished footer with version info & quick actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, viewModel: ScholarViewModel) {
    val context = LocalContext.current
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val allProfiles by viewModel.allProfiles.collectAsStateWithLifecycle()

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
                        fontWeight = FontWeight.Black,
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
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ==========================================
            // 1. HERO SCHOLAR PROFILE CARD
            // ==========================================
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        width = 0.8.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    ),
                    elevation = CardDefaults.outlinedCardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Large Profile Avatar (64dp)
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primaryContainer,
                                                MaterialTheme.colorScheme.secondaryContainer
                                            )
                                        ),
                                        shape = CircleShape
                                    )
                                    .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f), CircleShape)
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
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            Spacer(Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = activeProfile.name.ifBlank { "Scholar" },
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = if (activeProfile.alias.isNotBlank()) "@${activeProfile.alias}" else "Personal Workspace",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Active Profile Status Chip
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.VerifiedUser,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Active Academic Profile",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = "${allProfiles.size} Profile${if (allProfiles.size > 1) "s" else ""}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Quick Action Buttons: Edit & Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            BouncyOutlinedButton(
                                onClick = { showEditProfileSheet = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Edit Profile", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                            }

                            BouncyButton(
                                onClick = { showSwitchProfileSheet = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.SwapHoriz,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Switch Profile", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
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
                    subtitle = "Themes, AMOLED pure black, lighting & custom palettes",
                    icon = Icons.Rounded.Palette,
                    iconBgColor = Color(0xFF007AFF),
                    onClick = { navController.navigate("settings/appearance") }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                    modifier = Modifier.padding(start = 56.dp, end = 8.dp)
                )

                SettingsActionItemInCard(
                    title = "Streak Goals & Requirements",
                    subtitle = "Daily targets, completion thresholds & motivational tones",
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
                    subtitle = "Customize tag colors and global taxonomies",
                    icon = Icons.Rounded.LocalOffer,
                    iconBgColor = Color(0xFF30B0C7),
                    onClick = { navController.navigate("tags_hub") }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                    modifier = Modifier.padding(start = 56.dp, end = 8.dp)
                )

                SettingsActionItemInCard(
                    title = "System Configuration",
                    subtitle = "Course-subject linking, synergy scoring & Pomodoro defaults",
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
                    subtitle = "App PIN lock, biometric protection & safety alerts",
                    icon = Icons.Rounded.Security,
                    iconBgColor = Color(0xFFFF3B30),
                    onClick = { navController.navigate("settings/safety") }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                    modifier = Modifier.padding(start = 56.dp, end = 8.dp)
                )

                SettingsActionItemInCard(
                    title = "Notifications & Reminders",
                    subtitle = "Notification channels, study alarms & reminders",
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
                    title = "Multi-Device P2P Sync",
                    subtitle = "End-to-end encrypted device syncing with 1-time mutual pairing",
                    icon = Icons.Rounded.Sync,
                    iconBgColor = Color(0xFFAF52DE),
                    onClick = { navController.navigate("settings/sync") }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                    modifier = Modifier.padding(start = 56.dp, end = 8.dp)
                )

                SettingsActionItemInCard(
                    title = "Data & Backups",
                    subtitle = "Export, import, database backups & complete resets",
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
                    subtitle = "Prototype study tools, beta workflows & advanced diagnostics",
                    icon = Icons.Rounded.Science,
                    iconBgColor = Color(0xFFE040FB),
                    onClick = { navController.navigate("settings/beta") }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                    modifier = Modifier.padding(start = 56.dp, end = 8.dp)
                )

                SettingsActionItemInCard(
                    title = "About Lumia",
                    subtitle = "Version v1.0.7, license & open source repository",
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
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text(
                    text = "Lumia Tracker • v1.0.7",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Academic Productivity & Mastery Hub",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BouncyTextButton(onClick = { navController.navigate("settings/about") }) {
                        Text("Release Notes", style = MaterialTheme.typography.labelSmall)
                    }
                    Text("•", color = MaterialTheme.colorScheme.outlineVariant)
                    BouncyTextButton(onClick = { navController.navigate("settings/data") }) {
                        Text("Backup Hub", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
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
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 36.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(18.dp)
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
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    BouncyIconButton(onClick = { showEditProfileSheet = false }) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close")
                    }
                }

                // Avatar and Photo Upload
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                            .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
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
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Profile Avatar",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Custom photo or initial icon",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    BouncyButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
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
                    shape = RoundedCornerShape(16.dp)
                )

                OutlinedTextField(
                    value = editAlias,
                    onValueChange = { editAlias = it },
                    label = { Text("Handle / Workspace Alias") },
                    prefix = { Text("@") },
                    placeholder = { Text("username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                // Emoji Presets
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Or choose avatar preset emoji:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val emojiList = listOf("🎓", "📚", "⚡", "🔬", "🚀", "💡", "🧠", "🎯", "💻", "✨", "🪐", "🔥")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(emojiList) { emoji ->
                            val isSelected = editAvatar == emoji
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier
                                    .size(44.dp)
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BouncyOutlinedButton(
                        onClick = { showEditProfileSheet = false },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
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
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Save Changes")
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
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 36.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Sheet Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Scholar Profiles",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Switch workspace or manage profiles",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

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
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    allProfiles.forEach { profile ->
                        val isCurrent = profile.id == activeProfile.id
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                width = if (isCurrent) 1.5.dp else 0.8.dp,
                                color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isCurrent) {
                                    showSwitchProfileSheet = false
                                    viewModel.switchProfileAndRestart(context, profile.id)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
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
                                            fontWeight = FontWeight.Black,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                    }
                                }

                                Spacer(Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        profile.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
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
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primary
                                    ) {
                                        Icon(
                                            Icons.Rounded.Check,
                                            contentDescription = "Active",
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier
                                                .padding(4.dp)
                                                .size(16.dp)
                                        )
                                    }
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
                    "Create New Profile",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        "Each profile maintains its own courses, streaks, tags, and database.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

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
                        label = { Text("Handle / Alias (Optional)") },
                        prefix = { Text("@") },
                        placeholder = { Text("research_hub") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Choose Initial Emoji Avatar:",
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
                                    border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                                    modifier = Modifier
                                        .size(38.dp)
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
                    Text("Create & Switch")
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
