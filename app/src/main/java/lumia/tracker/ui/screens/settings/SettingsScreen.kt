package lumia.tracker.ui.screens.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.BouncyOutlinedButton
import lumia.tracker.ui.components.BouncyTextButton
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.components.ScholarCardDefaults
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import lumia.tracker.ui.screens.settings.components.SettingsActionItemInCard
import lumia.tracker.ui.screens.settings.components.SettingsGroupCard
import lumia.tracker.ui.theme.bouncyClick
import lumia.tracker.viewmodel.ScholarViewModel
import java.io.File
import java.io.FileOutputStream

/**
 * Searchable metadata item for live instant settings search index.
 */
private data class SettingsSearchItem(
    val title: String,
    val subtitle: String,
    val category: String,
    val route: String,
    val icon: ImageVector,
    val iconBgColor: Color
)

/**
 * SettingsScreen - Improvised Academic Preferences & Configuration Hub.
 * Features an ambient Profile Hero card with live stats, instant Live Search,
 * 1-tap Quick Ambient Controls shelf (Theme Mode, Pure Black, Navigation Tabs),
 * organized settings groups with complete route linking, and streamlined profile sheets.
 */
@ValueScore(
    score = 98,
    importance = Importance.CRITICAL,
    description = "Completely improvised settings hub with live search, ambient profile hero, quick controls shelf, and unified route groups",
    category = "Settings"
)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(navController: NavController, viewModel: ScholarViewModel) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val allProfiles by viewModel.allProfiles.collectAsStateWithLifecycle()
    val streakCurrent by viewModel.streakCurrent.collectAsStateWithLifecycle()

    // Quick Settings State Flows
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val pureBlackMode by viewModel.pureBlackMode.collectAsStateWithLifecycle()
    val featureSelfStudyEnabled by viewModel.featureSelfStudyEnabled.collectAsStateWithLifecycle()
    val featureAnalyticsEnabled by viewModel.featureAnalyticsEnabled.collectAsStateWithLifecycle()

    // Modals & Search State
    var searchQuery by remember { mutableStateOf("") }
    var showEditProfileSheet by remember { mutableStateOf(false) }
    var showSwitchProfileSheet by remember { mutableStateOf(false) }
    var showCreateProfileDialog by remember { mutableStateOf(false) }

    // Search index registry
    val searchIndex = remember {
        listOf(
            SettingsSearchItem("Appearance & Themes", "Light/Dark mode, color palettes & AMOLED pure black", "Personalization", "settings/appearance", Icons.Rounded.Palette, Color(0xFF007AFF)),
            SettingsSearchItem("Dark Mode & AMOLED", "Toggle Light, Dark, System or Pure Black OLED", "Personalization", "settings/appearance", Icons.Rounded.DarkMode, Color(0xFF1C1C1E)),
            SettingsSearchItem("Custom Color Palette", "Create custom hex palettes & dynamic lighting presets", "Personalization", "settings/advanced_theme", Icons.Rounded.ColorLens, Color(0xFF5856D6)),
            SettingsSearchItem("Streak Goals & Rules", "Daily study targets, grace days & multiplier", "Habits", "settings/streaks", Icons.Rounded.LocalFireDepartment, Color(0xFFFF9500)),
            SettingsSearchItem("Academic Synergy & System", "Course-subject linking, auto-link & lecture tracking", "Academics", "settings/system", Icons.Rounded.Hub, Color(0xFF34C759)),
            SettingsSearchItem("Pomodoro Auto-Logging", "Automatically log completed focus periods to academic history", "Academics", "settings/system", Icons.Rounded.Timer, Color(0xFFFF3B30)),
            SettingsSearchItem("Notifications & Reminders", "Deadline alerts, class schedules & morning briefing", "Alerts", "settings/notifications", Icons.Rounded.Notifications, Color(0xFF34C759)),
            SettingsSearchItem("Daily Digest Briefing", "Morning schedule summary and assignment deadlines", "Alerts", "settings/notifications", Icons.Rounded.Today, Color(0xFF5AC8FA)),
            SettingsSearchItem("Safety Guard & OLED Protection", "AOD burn-in protection, lockscreen & safe PIN", "Safety", "settings/safety", Icons.Rounded.Security, Color(0xFFFF2D55)),
            SettingsSearchItem("Burn-in Shift & Protection", "Periodic OLED pixel shift and safe auto-off", "Safety", "settings/safety", Icons.Rounded.Shield, Color(0xFFAF52DE)),
            SettingsSearchItem("Data Management & Backups", "Export database, cloud sync & auto-backup", "Storage", "settings/data", Icons.Rounded.Storage, Color(0xFF8E8E93)),
            SettingsSearchItem("Database Defragmentation", "Clean up SQLite cache and optimize performance", "Storage", "settings/data", Icons.Rounded.CleaningServices, Color(0xFF64D2FF)),
            SettingsSearchItem("Experimental Sandbox (Beta)", "Early-access features, action logs & experimental headers", "Beta", "settings/beta", Icons.Rounded.Science, Color(0xFFFF9500)),
            SettingsSearchItem("About Lumia Tracker", "Version 1.0.7 • Open source licenses, release notes & credits", "About", "settings/about", Icons.Rounded.Info, Color(0xFF636366))
        )
    }

    val filteredSearchResults = remember(searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else {
            val q = searchQuery.trim().lowercase()
            searchIndex.filter {
                it.title.lowercase().contains(q) ||
                    it.subtitle.lowercase().contains(q) ||
                    it.category.lowercase().contains(q)
            }
        }
    }

    val cardBg = if (isDark) MaterialTheme.colorScheme.surfaceContainerLow
                 else MaterialTheme.colorScheme.surfaceContainerLowest

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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ==========================================
            // 1. LIVE SEARCH SETTINGS BAR
            // ==========================================
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                Surface(
                    shape = CircleShape,
                    color = cardBg,
                    border = ScholarCardDefaults.border(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    "Search settings, themes, backups...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        if (searchQuery.isNotBlank()) {
                            BouncyIconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // If searching: display instant search results
            if (searchQuery.isNotBlank()) {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    ScholarCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        containerColor = cardBg,
                        border = ScholarCardDefaults.border()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Search Results (${filteredSearchResults.size})",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )

                            if (filteredSearchResults.isEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.SearchOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(36.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No settings found for \"$searchQuery\"",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                filteredSearchResults.forEachIndexed { index, item ->
                                    SettingsActionItemInCard(
                                        title = item.title,
                                        subtitle = item.subtitle,
                                        icon = item.icon,
                                        iconBgColor = item.iconBgColor,
                                        onClick = {
                                            searchQuery = ""
                                            navController.navigate(item.route)
                                        }
                                    )
                                    if (index < filteredSearchResults.lastIndex) {
                                        HorizontalDivider(
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.20f),
                                            modifier = Modifier.padding(start = 48.dp, end = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // ==========================================
                // 2. HERO SCHOLAR PROFILE CARD
                // ==========================================
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    ScholarCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        containerColor = cardBg,
                        border = ScholarCardDefaults.border()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Profile Header Row: Avatar, Name & Alias, Edit badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Avatar Squircle
                                Box(
                                    modifier = Modifier
                                        .size(62.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                                            shape = RoundedCornerShape(18.dp)
                                        )
                                        .clickable { showEditProfileSheet = true },
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
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (activeProfile.alias.isNotBlank()) {
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = "@${activeProfile.alias}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.30f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.VerifiedUser,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "Active",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            // Profile Stats Capsule Strip
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.20f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.LocalFireDepartment,
                                            contentDescription = null,
                                            tint = Color(0xFFFF9500),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "$streakCurrent Day Streak",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.20f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.People,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "${allProfiles.size} Profiles",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            // Quick Action Buttons: Edit, Switch, + New
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                BouncyOutlinedButton(
                                    onClick = { showEditProfileSheet = true },
                                    modifier = Modifier.weight(1f),
                                    shape = CircleShape,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 7.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(5.dp))
                                    Text(
                                        "Edit",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                BouncyButton(
                                    onClick = { showSwitchProfileSheet = true },
                                    modifier = Modifier.weight(1.2f),
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    ),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 7.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.SwapHoriz,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(Modifier.width(5.dp))
                                    Text(
                                        "Switch",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f),
                                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.30f)),
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .bouncyClick { showCreateProfileDialog = true }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Rounded.Add,
                                            contentDescription = "New Profile",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // 3. QUICK AMBIENT CONTROLS SHELF
                // ==========================================
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    ScholarCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        containerColor = cardBg,
                        border = ScholarCardDefaults.border()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Quick Controls",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            // Theme Mode 3-way Segmented Capsule
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f),
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.20f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(3.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf("Light" to "☀️ Light", "Dark" to "🌙 Dark", "System" to "⚙️ Auto").forEach { (mode, label) ->
                                        val isSelected = themeMode.equals(mode, ignoreCase = true)
                                        val btnBg = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else Color.Transparent
                                        val btnBorder = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)) else null
                                        val textColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

                                        Surface(
                                            shape = CircleShape,
                                            color = btnBg,
                                            border = btnBorder,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(CircleShape)
                                                .bouncyClick { viewModel.updateThemeMode(mode) }
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(vertical = 7.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = label,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = textColor
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Quick Toggles Strip
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // AMOLED Pure Black
                                val blackActive = pureBlackMode
                                Surface(
                                    shape = CircleShape,
                                    color = if (blackActive) Color.Black else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f),
                                    border = BorderStroke(
                                        0.5.dp,
                                        if (blackActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.20f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(CircleShape)
                                        .bouncyClick { viewModel.updatePureBlackMode(!pureBlackMode) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = if (blackActive) "🖤 Pure Black ON" else "🖤 AMOLED Black",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (blackActive) FontWeight.Bold else FontWeight.Medium,
                                            color = if (blackActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Tasks Tab Visibility Toggle
                                val tasksActive = featureSelfStudyEnabled
                                Surface(
                                    shape = CircleShape,
                                    color = if (tasksActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f),
                                    border = BorderStroke(
                                        0.5.dp,
                                        if (tasksActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
                                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.20f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(CircleShape)
                                        .bouncyClick { viewModel.updateFeatureSelfStudyEnabled(!featureSelfStudyEnabled) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = if (tasksActive) "📖 Tasks Tab ON" else "📖 Tasks Hidden",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (tasksActive) FontWeight.Bold else FontWeight.Medium,
                                            color = if (tasksActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // 4. GROUP: PERSONALIZATION & THEMES
                // ==========================================
                SettingsGroupCard(
                    title = "Personalization & Themes",
                    icon = Icons.Rounded.Palette
                ) {
                    SettingsActionItemInCard(
                        title = "Appearance & Themes",
                        subtitle = "Light/Dark mode, color palettes & dock layout",
                        icon = Icons.Rounded.Palette,
                        iconBgColor = Color(0xFF007AFF),
                        onClick = { navController.navigate("settings/appearance") }
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.20f),
                        modifier = Modifier.padding(start = 52.dp, end = 8.dp)
                    )

                    SettingsActionItemInCard(
                        title = "Custom Color Palette",
                        subtitle = "Hex color generator & curated theme presets",
                        icon = Icons.Rounded.ColorLens,
                        iconBgColor = Color(0xFF5856D6),
                        onClick = { navController.navigate("settings/advanced_theme") }
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.20f),
                        modifier = Modifier.padding(start = 52.dp, end = 8.dp)
                    )

                    SettingsActionItemInCard(
                        title = "Streak Goals & Requirements",
                        subtitle = "Daily study targets, grace days & multiplier",
                        icon = Icons.Rounded.LocalFireDepartment,
                        iconBgColor = Color(0xFFFF9500),
                        onClick = { navController.navigate("settings/streaks") }
                    )
                }

                // ==========================================
                // 5. GROUP: ACADEMICS & WORKFLOW
                // ==========================================
                SettingsGroupCard(
                    title = "Academics & Workflow",
                    icon = Icons.AutoMirrored.Rounded.MenuBook
                ) {
                    SettingsActionItemInCard(
                        title = "Academic Synergy & System",
                        subtitle = "Course-subject linking & lecture tracking",
                        icon = Icons.Rounded.Hub,
                        iconBgColor = Color(0xFF34C759),
                        onClick = { navController.navigate("settings/system") }
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.20f),
                        modifier = Modifier.padding(start = 52.dp, end = 8.dp)
                    )

                    SettingsActionItemInCard(
                        title = "Notifications & Reminders",
                        subtitle = "Timetable alerts, deadline nudges & daily digests",
                        icon = Icons.Rounded.Notifications,
                        iconBgColor = Color(0xFF5AC8FA),
                        onClick = { navController.navigate("settings/notifications") }
                    )
                }

                // ==========================================
                // 6. GROUP: SAFETY & OLED DISPLAY
                // ==========================================
                SettingsGroupCard(
                    title = "Safety & OLED Display",
                    icon = Icons.Rounded.Security
                ) {
                    SettingsActionItemInCard(
                        title = "Safety & Burn-in Protection",
                        subtitle = "AOD burn-in protection, lockscreen & safe PIN",
                        icon = Icons.Rounded.Security,
                        iconBgColor = Color(0xFFFF2D55),
                        onClick = { navController.navigate("settings/safety") }
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.20f),
                        modifier = Modifier.padding(start = 52.dp, end = 8.dp)
                    )

                    SettingsActionItemInCard(
                        title = "Experimental Features (Beta)",
                        subtitle = "Early-access sandboxes, action logs & diagnostics",
                        icon = Icons.Rounded.Science,
                        iconBgColor = Color(0xFFAF52DE),
                        onClick = { navController.navigate("settings/beta") }
                    )
                }

                // ==========================================
                // 7. GROUP: DATA & STORAGE
                // ==========================================
                SettingsGroupCard(
                    title = "Data & Storage",
                    icon = Icons.Rounded.Storage
                ) {
                    SettingsActionItemInCard(
                        title = "Data Management & Backups",
                        subtitle = "Local database export, auto-backups & sync",
                        icon = Icons.Rounded.Storage,
                        iconBgColor = Color(0xFF8E8E93),
                        onClick = { navController.navigate("settings/data") }
                    )
                }

                // ==========================================
                // 8. GROUP: ABOUT & SUPPORT
                // ==========================================
                SettingsGroupCard(
                    title = "About",
                    icon = Icons.Rounded.Info
                ) {
                    SettingsActionItemInCard(
                        title = "About Lumia Tracker",
                        subtitle = "Version 1.0.7 • Open source licenses & changelog",
                        icon = Icons.Rounded.Info,
                        iconBgColor = Color(0xFF636366),
                        onClick = { navController.navigate("settings/about") }
                    )
                }

                // ==========================================
                // 9. FOOTER SECTION
                // ==========================================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.20f))
                    ) {
                        Text(
                            text = "Lumia Tracker • Ambient Edition",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BouncyTextButton(onClick = { navController.navigate("settings/about") }) {
                            Text("Release Notes", style = MaterialTheme.typography.labelMedium)
                        }
                        Text("•", color = MaterialTheme.colorScheme.outlineVariant)
                        BouncyTextButton(onClick = { navController.navigate("settings/data") }) {
                            Text("Backups", style = MaterialTheme.typography.labelMedium)
                        }
                        Text("•", color = MaterialTheme.colorScheme.outlineVariant)
                        BouncyTextButton(onClick = { navController.navigate("settings/beta") }) {
                            Text("Beta Lab", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
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
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
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
                        .background(cardBg, RoundedCornerShape(20.dp))
                        .border(
                            border = ScholarCardDefaults.border(),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(2.5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
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
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Avatar Picture",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Upload a custom photo or pick an emoji",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    BouncyButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Icon(Icons.Rounded.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Upload", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
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
                        text = "Emoji Avatars",
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
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
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
                        shape = CircleShape
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
                        shape = CircleShape
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
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
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
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("New Profile", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Profile Items List
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    allProfiles.forEach { profile ->
                        val isCurrent = profile.id == activeProfile.id
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else cardBg,
                            border = if (isCurrent) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)) else ScholarCardDefaults.border(),
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
                                        .size(44.dp)
                                        .background(
                                            color = if (isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.45f) else Color.Transparent,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(2.5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
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
                    shape = CircleShape
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
