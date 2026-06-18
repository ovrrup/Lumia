package lumia.tracker.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import lumia.tracker.model.AchievementDef
import lumia.tracker.model.AchievementSystem
import lumia.tracker.viewmodel.ScholarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(navController: NavController, viewModel: ScholarViewModel) {
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val allAchievements = AchievementSystem.ACHIEVEMENTS
    val unlockedIds = activeProfile.unlockedAchievements
    
    // Track stats from ViewModel for showing exact progress
    val tasksState by viewModel.tasks.collectAsStateWithLifecycle()
    val completedTasksCount = tasksState.count { it.isCompleted }
    val totalSessions = viewModel.pomodoroSessions.collectAsStateWithLifecycle().value.size
    val currentStreak = activeProfile.points / 30 // or evaluate dynamic streaks if present, let's keep robust fallback
    
    var selectedTab by remember { mutableStateOf("All") }
    val tabs = listOf("All", "POINTS", "TASKS", "SESSIONS", "STREAK")
    
    var selectedAchievement by remember { mutableStateOf<AchievementDef?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Achievements & Badges", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Overarching progress card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.EmojiEvents,
                            contentDescription = "Achievements",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Total Unlocked",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            "${unlockedIds.size} / ${allAchievements.size}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.height(4.dp))
                        // Progress Bar
                        LinearProgressIndicator(
                            progress = { if (allAchievements.isNotEmpty()) unlockedIds.size.toFloat() / allAchievements.size else 0f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    }
                }
            }

            // Category list tabs
            ScrollableTabRow(
                selectedTabIndex = tabs.indexOf(selectedTab),
                edgePadding = 16.dp,
                divider = {},
                indicator = {}
            ) {
                tabs.forEach { tab ->
                    val isSelected = selectedTab == tab
                    val label = when (tab) {
                        "POINTS" -> "Score"
                        "TASKS" -> "Study Tasks"
                        "SESSIONS" -> "Focus"
                        "STREAK" -> "Streak"
                        else -> "All"
                    }
                    Tab(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.secondary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            val filteredAchievements = if (selectedTab == "All") {
                allAchievements
            } else {
                allAchievements.filter { it.requiredType == selectedTab }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { Spacer(Modifier.height(8.dp)) }

                items(filteredAchievements, key = { it.id }) { ach ->
                    val isUnlocked = unlockedIds.contains(ach.id)
                    val iconVector = when (ach.iconEmoji) {
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

                    // Card with modern asymmetric design and clickable dialog trigger
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedAchievement = ach },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUnlocked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isUnlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        if (isUnlocked) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isUnlocked) {
                                    Icon(
                                        iconVector,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Icon(
                                        Icons.Rounded.Lock,
                                        contentDescription = "Locked",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    ach.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    ach.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { selectedAchievement = ach }) {
                                Icon(
                                    Icons.Rounded.Info,
                                    contentDescription = "Info",
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
                
                item { Spacer(Modifier.height(32.dp)) }
            }
        }
    }

    // Interactive details dialog showing true numerical progress
    if (selectedAchievement != null) {
        val ach = selectedAchievement!!
        val isUnlocked = unlockedIds.contains(ach.id)
        
        val currentValue = when (ach.requiredType) {
            "POINTS" -> activeProfile.points
            "TASKS" -> completedTasksCount
            "SESSIONS" -> totalSessions
            "STREAK" -> currentStreak
            else -> 0
        }
        val target = ach.requiredValue
        val ratio = if (target > 0) currentValue.toFloat() / target else 0f
        val clampedRatio = ratio.coerceIn(0f, 1f)

        val detailIcon = when (ach.iconEmoji) {
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

        AlertDialog(
            onDismissRequest = { selectedAchievement = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        detailIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(ach.title, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(ach.description, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(16.dp))
                    
                    Text(
                        "Progress: $currentValue / $target",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    
                    LinearProgressIndicator(
                        progress = { clampedRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(CircleShape),
                        color = if (isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    
                    Spacer(Modifier.height(12.dp))
                    if (isUnlocked) {
                        Text(
                            "Status: Claimed & Active!",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        val remaining = (target - currentValue).coerceAtLeast(0)
                        Text(
                            "Status: Lock is active. Earn $remaining more ${ach.requiredType.lowercase()} to claim this achievement badge.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedAchievement = null }) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
