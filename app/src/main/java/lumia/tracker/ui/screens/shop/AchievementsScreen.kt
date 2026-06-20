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

fun getAchievementDifficultyAndRank(ach: AchievementDef): Pair<String, String> {
    val required = ach.requiredValue
    val type = ach.requiredType
    return when (type) {
        "POINTS" -> {
            when {
                required <= 300 -> "Easy" to "Bronze"
                required <= 1000 -> "Medium" to "Silver"
                required <= 5000 -> "Hard" to "Gold"
                else -> "Mythic" to "Platinum"
            }
        }
        "TASKS" -> {
            when {
                required <= 10 -> "Easy" to "Bronze"
                required <= 75 -> "Medium" to "Silver"
                required <= 200 -> "Hard" to "Gold"
                else -> "Mythic" to "Platinum"
            }
        }
        "SESSIONS" -> {
            when {
                required <= 10 -> "Easy" to "Bronze"
                required <= 75 -> "Medium" to "Silver"
                required <= 200 -> "Hard" to "Gold"
                else -> "Mythic" to "Platinum"
            }
        }
        "STREAK" -> {
            when {
                required <= 5 -> "Easy" to "Bronze"
                required <= 21 -> "Medium" to "Silver"
                required <= 75 -> "Hard" to "Gold"
                else -> "Mythic" to "Platinum"
            }
        }
        else -> "Easy" to "Bronze"
    }
}

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
    val currentStreak by viewModel.currentStreak.collectAsStateWithLifecycle()
    
    var selectedTab by remember { mutableStateOf("All") }
    val tabs = listOf("All", "POINTS", "TASKS", "SESSIONS", "STREAK")
    
    var selectedDifficulty by remember { mutableStateOf("All") }
    
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
                shape = MaterialTheme.shapes.large,
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
                                .clip(MaterialTheme.shapes.medium)
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

            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(listOf("All", "Easy", "Medium", "Hard", "Mythic")) { diff ->
                    val isSelected = selectedDifficulty == diff
                    val rankText = when (diff) {
                        "Easy" -> "Bronze"
                        "Medium" -> "Silver"
                        "Hard" -> "Gold"
                        "Mythic" -> "Plat / Mythic"
                        else -> "All Difficulties"
                    }
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .clickable { selectedDifficulty = diff }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = rankText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            val filteredByCategory = if (selectedTab == "All") {
                allAchievements
            } else {
                allAchievements.filter { it.requiredType == selectedTab }
            }

            val filteredAchievements = if (selectedDifficulty == "All") {
                filteredByCategory
            } else {
                filteredByCategory.filter { ach ->
                    val (diff, _) = getAchievementDifficultyAndRank(ach)
                    diff == selectedDifficulty
                }
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
                        shape = MaterialTheme.shapes.medium,
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
                                val (diff, rank) = getAchievementDifficultyAndRank(ach)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        ach.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    val tagBg = when (rank) {
                                        "Bronze" -> Color(0xFFCD7F32).copy(alpha = 0.15f)
                                        "Silver" -> Color(0xFFC0C0C0).copy(alpha = 0.15f)
                                        "Gold" -> Color(0xFFFFD700).copy(alpha = 0.15f)
                                        else -> Color(0xFFDDA0DD).copy(alpha = 0.2f)
                                    }
                                    val tagText = when (rank) {
                                        "Bronze" -> Color(0xFF8B5A2B)
                                        "Silver" -> Color(0xFF5A5A5A)
                                        "Gold" -> Color(0xFFB8860B)
                                        else -> Color(0xFF8A2BE2)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(tagBg)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = rank,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = tagText
                                        )
                                    }
                                }
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
        val ach = selectedAchievement ?: return
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
                    val (diff, rank) = getAchievementDifficultyAndRank(ach)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "Difficulty: $diff",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.tertiaryContainer)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "Rank: $rank",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
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
                        Spacer(Modifier.height(16.dp))
                        val isSelected = activeProfile.selectedBadge == ach.id
                        Button(
                            onClick = {
                                viewModel.selectProfileBadge(ach.id)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (isSelected) {
                                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Text("Active Profile Badge", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                } else {
                                    Icon(Icons.Rounded.EmojiEvents, contentDescription = null)
                                    Text("Set as Profile Badge", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
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
