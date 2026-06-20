package lumia.tracker.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import kotlinx.coroutines.launch
import lumia.tracker.model.PlusFeature
import lumia.tracker.model.PlusShop
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.viewmodel.ScholarViewModel
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PlusShopScreen(navController: NavController, viewModel: ScholarViewModel) {
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val availableFeatures = PlusShop.features
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Categories and filter state
    val categories = listOf("All") + availableFeatures.map { it.category }.distinct()
    var selectedCategory by remember { mutableStateOf("All") }

    // Dialog state for Currency Information / Explanation
    var showInfoDialog by remember { mutableStateOf(false) }

    // Mystery Wheel rolling state
    var isRollingWheel by remember { mutableStateOf(false) }
    var rolledFeatureResult by remember { mutableStateOf<PlusFeature?>(null) }
    var showRollResultDialog by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text("Scholar Economy Shop", fontWeight = FontWeight.Black)
                        Text(
                            "Earn and balance points & credits to unlock features",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
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
            // Currencies Status Bar - Modern dual-currency high-contrast visual display
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Points Counter Pill
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.MilitaryTech, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Focus Points", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        }
                        Text("${activeProfile.points}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    }
                }

                // Credits Counter Pill
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Savings, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Credits Block", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                        }
                        Text("${activeProfile.credits}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.secondary)
                    }
                }

                // Currency Valuation Info Icon Trigger
                IconButton(
                    onClick = { showInfoDialog = true },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(Icons.Rounded.Info, contentDescription = "Explain Currency Value", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Category Quick Filter Scrollable Row
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(selectedCategory),
                edgePadding = 16.dp,
                divider = {},
                indicator = {}
            ) {
                categories.forEach { category ->
                    val isSelected = selectedCategory == category
                    Tab(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp, vertical = 4.dp)
                                .clip(MaterialTheme.shapes.extraLarge)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Products list
            val filteredFeatures = if (selectedCategory == "All") {
                availableFeatures
            } else {
                availableFeatures.filter { it.category == selectedCategory }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // mystery chest gacha banner item
                item {
                    MysteryRarityChestCard(
                        credits = activeProfile.credits,
                        isRolling = isRollingWheel,
                        onRoll = {
                            if (activeProfile.credits >= 150) {
                                isRollingWheel = true
                                scope.launch {
                                    kotlinx.coroutines.delay(1800) // Animated suspense delay
                                    val result = viewModel.rollMysteryWheel()
                                    isRollingWheel = false
                                    rolledFeatureResult = result
                                    showRollResultDialog = true
                                }
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("❌ You need 150 Credits to spin the Lucky Star chest!")
                                }
                            }
                        }
                    )
                }

                items(filteredFeatures, key = { it.id }) { feature ->
                    val isLockedPermanently = !activeProfile.unlockedFeatures.contains(feature.id)
                    val rentExpiry = activeProfile.rentedFeatures[feature.id] ?: 0L
                    val isRented = rentExpiry > System.currentTimeMillis()
                    val isUnlocked = !isLockedPermanently || isRented
                    
                    val levelSufficient = activeProfile.level >= feature.requiredLevel
                    val pointsSufficient = activeProfile.points >= feature.pricePoints
                    val creditsSufficient = activeProfile.credits >= feature.priceCredits
                    val rentCreditsSufficient = activeProfile.credits >= feature.rentCostCredits
                    
                    // Style by Grade with high-end academic designator tags
                    val (rankLabel, rankColor) = when (feature.rank) {
                        "SS" -> "SS Grade (1 in 250 Rare, 0.4%)" to Color(0xFFFF3D00)
                        "S" -> "S Grade (1 in 100 Rare, 1.0%)" to Color(0xFFE040FB)
                        "A+" -> "A+ Grade (1 in 50 Hard, 2.0%)" to Color(0xFFFF9100)
                        "A" -> "A Grade (1 in 30 Unlocking, 3.3%)" to Color(0xFF2979FF)
                        else -> "B Grade (1 in 15 Baseline, 6.7%)" to Color(0xFF00E676)
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItemPlacement(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUnlocked) {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        ),
                        border = borderStrokeForFeature(isUnlocked, pointsSufficient || creditsSufficient, levelSufficient)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Top Row: Category Badge + Class Tag
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        feature.category,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                
                                Text(
                                    text = rankLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = rankColor
                                )
                            }

                            Spacer(Modifier.height(10.dp))

                            // Name and Description
                            Text(
                                text = feature.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = feature.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(Modifier.height(12.dp))

                            // Display current status
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    if (!isLockedPermanently) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Rounded.Verified, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Owned Permanently", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        }
                                    } else if (isRented) {
                                        val hoursLeft = TimeUnit.MILLISECONDS.toHours(rentExpiry - System.currentTimeMillis()).coerceAtLeast(0)
                                        val minsLeft = (TimeUnit.MILLISECONDS.toMinutes(rentExpiry - System.currentTimeMillis()) % 60).coerceAtLeast(0)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Rounded.HourglassBottom, contentDescription = null, tint = Color(0xFFFF9100), modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Active Rent: ${hoursLeft}h ${minsLeft}m left", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFFFF9100))
                                        }
                                    } else {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                if (levelSufficient) Icons.Rounded.LockOpen else Icons.Rounded.Lock,
                                                contentDescription = null,
                                                tint = if (levelSufficient) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                text = "Required Level: ${feature.requiredLevel}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (levelSufficient) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }

                            // Interactive Acquisition Panel
                            if (isLockedPermanently) {
                                Spacer(Modifier.height(16.dp))
                                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                Spacer(Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Rent Button
                                    Button(
                                        onClick = {
                                            if (viewModel.rentFeatureWithCredits(feature.id, feature.rentCostCredits, 1)) {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("⏳ Rented: ${feature.name} for 24 hours successfully!")
                                                }
                                            }
                                        },
                                        enabled = rentCreditsSufficient && levelSufficient,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Rent 24h", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                            Text("${feature.rentCostCredits} Credits", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }

                                    // Purchase with Credits
                                    Button(
                                        onClick = {
                                            if (viewModel.purchaseFeatureWithCredits(feature.id, feature.priceCredits)) {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("🔑 Unlocked Permanently: ${feature.name}!")
                                                }
                                            }
                                        },
                                        enabled = creditsSufficient && levelSufficient,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.secondary,
                                            contentColor = MaterialTheme.colorScheme.onSecondary
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1.2f)
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Buy Permanent", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                            Text("${feature.priceCredits} Credits", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }

                                    // Purchase with Points
                                    Button(
                                        onClick = {
                                            if (viewModel.purchaseFeatureWithPoints(feature.id, feature.pricePoints)) {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("💎 Unlocked Permanently: ${feature.name}!")
                                                }
                                            }
                                        },
                                        enabled = pointsSufficient && levelSufficient,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1.2f)
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Buy Premium", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                            Text("${feature.pricePoints} Points", style = MaterialTheme.typography.labelSmall)
                                        }
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
    }

    // Economy Valuation Explanation Dialog
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Economy Valuation", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "Points and Credits are completely different and independent currencies designed to serve separate purposes in the Scholar economy.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.MilitaryTech, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Focus Points (Progression)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("Linked directly to long-term academic accomplishments and permanent progression achievements.", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Savings, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Credits Block (Utility)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            Text("Modular tokens earned frequently for renting features or buying items in the shop of rarity grades.", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "Relative Valuation:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "1 Focus Point is equal valuable and rare as 50 Credits at one time.",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("Understood", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Mystery Wheel Suspense Result Dialog
    if (showRollResultDialog) {
        AlertDialog(
            onDismissRequest = { showRollResultDialog = false },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Icon(
                        Icons.Rounded.WorkspacePremium,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (rolledFeatureResult != null) "RARE DRAFT DROP!" else "LUCKY ROLL EXTRA",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val res = rolledFeatureResult
                    if (res != null) {
                        Text(
                            "You pulled the capsule containing:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    res.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Grade Rank: ${res.rank}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            res.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    } else {
                        Text(
                            "Amazing! You already own all dynamic library parts! Spin Cost of 150 Credits refunded. (Double Drop prevention activated)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showRollResultDialog = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Equip & Enjoy!", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun MysteryRarityChestCard(
    credits: Int,
    isRolling: Boolean,
    onRoll: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.4f), MaterialTheme.shapes.large),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Lucky Star Mystery Chest", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                    Text("Spend 150 credits to acquire a random premium lock of rarity grades!", style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(12.dp))

            // Probabilities breakdown collapsed style
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(8.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("B: 1-in-15", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF00E676))
                    Text("A: 1-in-30", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF2979FF))
                    Text("A+: 1-in-50", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFFFF9100))
                    Text("S: 1-in-100", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFFE040FB))
                    Text("SS: 1-in-250", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFFFF3D00))
                }
            }

            Spacer(Modifier.height(14.dp))

            Button(
                onClick = onRoll,
                enabled = !isRolling && credits >= 150,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFD700),
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isRolling) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onSecondaryContainer)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Casino, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Draft Capsule Chest (150 Credits)", fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
private fun borderStrokeForFeature(isUnlocked: Boolean, pointsSufficient: Boolean, levelSufficient: Boolean): androidx.compose.foundation.BorderStroke? {
    val borderColor = if (isUnlocked) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    } else if (pointsSufficient && levelSufficient) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    }
    return androidx.compose.foundation.BorderStroke(2.dp, borderColor)
}
