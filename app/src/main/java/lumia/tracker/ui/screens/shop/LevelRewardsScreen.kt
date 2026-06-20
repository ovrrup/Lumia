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
import androidx.compose.material.icons.rounded.*
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
import lumia.tracker.viewmodel.ScholarViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

data class LevelMilestone(
    val level: Int,
    val rewardName: String,
    val rewardDesc: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val targetPlusId: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelRewardsScreen(navController: NavController, viewModel: ScholarViewModel) {
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val currentLevel = activeProfile.level

    val milestones = listOf(
        LevelMilestone(1, "Beginning of Wisdom", "Access to the classic study timer interface.", Icons.Rounded.MenuBook),
        LevelMilestone(5, "Theme Expansion Tier", "Eligible to purchase Pastel, Matrix, and Cyberpunk themes.", Icons.Rounded.Palette, "feat_theme_pack"),
        LevelMilestone(10, "Layout & Social unlocks", "Eligible to integrate Advanced Screen Layouts and the Study Leaderboard.", Icons.Rounded.Dashboard, "feat_leaderboard"),
        LevelMilestone(15, "Advanced Typography", "Unlock beautiful global custom font sizing & layout weights.", Icons.Rounded.TextFields),
        LevelMilestone(25, "Creative Freedom", "Purchase Custom Theme customizer module in shop.", Icons.Rounded.AutoAwesome, "feat_custom_theme"),
        LevelMilestone(40, "Zen Minimalist", "Option to enable clutter-free, hyper-focus interface.", Icons.Rounded.OfflineBolt, "feat_minimal_ui"),
        LevelMilestone(55, "Stealth Always-On", "OLED-safe low frequency focus layout.", Icons.Rounded.SettingsBrightness, "feat_true_aod"),
        LevelMilestone(75, "Scholar Vanguard", "Mad Scientist Lab experimental components.", Icons.Rounded.Science, "feat_experimental"),
        LevelMilestone(100, "Immortal Scholar", "Access premium custom dashboard widgets and fluid animations.", Icons.Rounded.WorkspacePremium),
        LevelMilestone(150, "Galactic Wisdom", "Activate adaptive space-themed soundscapes for deep concentration.", Icons.Rounded.CloudQueue),
        LevelMilestone(200, "Cosmic Vanguard", "Award of sovereign state indicator displays inside profiles.", Icons.Rounded.LocalFireDepartment),
        LevelMilestone(250, "Universal Legend", "Exotic high-contrast deep twilight styling modules.", Icons.Rounded.BrightnessHigh),
        LevelMilestone(300, "Eternal Luminary", "Final ultimate level completion badge of godhead enlightenment.", Icons.Rounded.Psychology)
    )

    var showUnboxDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Level Milestones", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(Modifier.height(12.dp))
                // Beautiful RPG Experience Progress Card
                val xpNeeded = viewModel.getXpNeededForNextLevel(currentLevel)
                val currentXp = activeProfile.experience
                val xpProgress = (currentXp.toFloat() / xpNeeded.toFloat()).coerceIn(0f, 1f)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.secondary
                                            )
                                        ),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.MilitaryTech,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(
                                    "Level $currentLevel",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    "XP Progression: $currentXp / $xpNeeded XP",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        // M3 Linear Progress Indicator with rounded track
                        LinearProgressIndicator(
                            progress = { xpProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        )
                        
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Focus Points: ${activeProfile.points}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                "${(xpProgress * 100).toInt()}% towards Level ${currentLevel + 1}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Interactive Surprise Box Alert Banner if present
                if (activeProfile.pendingSurpriseBoxes > 0) {
                    Spacer(Modifier.height(16.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showUnboxDialog = true },
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(MaterialTheme.colorScheme.tertiary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Rounded.CardGiftcard,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onTertiary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "Surprise Boxes Available!",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    Text(
                                        "You have ${activeProfile.pendingSurpriseBoxes} box(es). Tap to open!",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                            Button(
                                onClick = { showUnboxDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary,
                                    contentColor = MaterialTheme.colorScheme.onTertiary
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("OPEN", fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                Text(
                    "Progression Timeline",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                )
            }

            items(milestones) { milestone ->
                val isUnlocked = currentLevel >= milestone.level
                val isUnlockedShop = milestone.targetPlusId?.let { activeProfile.isFeatureUnlocked(it) } ?: true
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Timeline Node
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(48.dp)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    if (isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = milestone.level.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isUnlocked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        // Visual connecting line
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(40.dp)
                                .background(
                                    if (isUnlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                        )
                    }

                    Spacer(Modifier.width(16.dp))

                    // Detail Card
                    Card(
                        modifier = Modifier
                            .weight(1f),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUnlocked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isUnlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        milestone.icon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        milestone.rewardName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Level ${milestone.level} Milestone",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                milestone.rewardDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(Modifier.height(10.dp))
                            // Status Badge
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isUnlocked) {
                                    Icon(
                                        Icons.Rounded.CheckCircle,
                                        contentDescription = "Eligible",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = if (isUnlockedShop) "Available / Active" else "Milestone Unlocked (Purchase in Shop)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Icon(
                                        Icons.Rounded.Lock,
                                        contentDescription = "Locked",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "Locked - Reach Level ${milestone.level}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            item {
                Spacer(Modifier.height(48.dp))
            }
        }
    }

    // Interactive Surprise Box Opening Dialog with elegant tapping stages & animations
    if (showUnboxDialog) {
        var shakeStage by remember { mutableStateOf(0) }
        var isExploding by remember { mutableStateOf(false) }
        var result by remember { mutableStateOf<ScholarViewModel.SurpriseBoxResult?>(null) }
        val scope = rememberCoroutineScope()

        AlertDialog(
            onDismissRequest = { 
                if (result != null) {
                    showUnboxDialog = false
                }
            },
            title = {
                Text(
                    text = if (result != null) "LOOT UNBOXED!" else "Surprise Box",
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (result == null) {
                        // Game-like interactive opening stages
                        Text(
                            text = "Tap the Surprise Box 3 times to crack the lock!",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(Modifier.height(24.dp))

                        // Box Icon with animated bounce/shake properties
                        val scale = if (shakeStage > 0) 1.15f - (shakeStage * 0.05f) else 1.0f
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.tertiary,
                                            MaterialTheme.colorScheme.tertiaryContainer
                                        )
                                    )
                                )
                                .clickable(enabled = !isExploding) {
                                    if (shakeStage < 2) {
                                        shakeStage++
                                    } else {
                                        shakeStage = 3
                                        isExploding = true
                                        // Wait a little bit for realistic suspense explosion delay
                                        scope.launch {
                                            kotlinx.coroutines.delay(1000)
                                            result = viewModel.claimSurpriseBox()
                                             result?.let { res ->
                                                 viewModel.postNotification(
                                                     title = "Reward Equipped!",
                                                     message = res.detailText,
                                                     type = res.type
                                                 )
                                             }
                                            isExploding = false
                                        }
                                    }
                                }
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (shakeStage >= 3) Icons.Rounded.AutoAwesome else Icons.Rounded.CardGiftcard,
                                contentDescription = "Surprise Box",
                                tint = MaterialTheme.colorScheme.onTertiary,
                                modifier = Modifier.size(64.dp)
                            )
                        }

                        Spacer(Modifier.height(24.dp))

                        if (isExploding) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.tertiary)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "BOOM! Opening...",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        } else {
                            Text(
                                "Stage: $shakeStage / 3 Hits",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = when (shakeStage) {
                                    0 -> "Give it a solid tap!"
                                    1 -> "It's wobbling! Tap again!"
                                    2 -> "Almost cracked! ONE MORE HIT!"
                                    else -> "Exploding!"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        // Display unboxed prizes
                        result?.let { unboxed ->
                            val prizeColor = when (unboxed.type) {
                                "POINTS" -> MaterialTheme.colorScheme.primary
                                "CREDITS" -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.tertiary
                            }
                            
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .background(prizeColor.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (unboxed.type) {
                                        "POINTS" -> Icons.Rounded.MilitaryTech
                                        "CREDITS" -> Icons.Rounded.Savings
                                        else -> Icons.Rounded.WorkspacePremium
                                    },
                                    contentDescription = null,
                                    tint = prizeColor,
                                    modifier = Modifier.size(52.dp)
                                )
                            }
                            
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = unboxed.detailText,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = prizeColor
                            )
                            
                            if (unboxed.featureName.isNotEmpty()) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Temporary Premium Unlock activated! Go explore the customized settings in menu layout overlays.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (result != null) {
                    Button(
                        onClick = { 
                            showUnboxDialog = false 
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("EQUIP & CLAIM", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        )
    }
}
