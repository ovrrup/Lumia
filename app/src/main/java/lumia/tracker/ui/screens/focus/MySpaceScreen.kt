package lumia.tracker.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.BouncyIconButton
import lumia.tracker.ui.components.BouncyTextButton
import lumia.tracker.ui.components.GlassCard
import lumia.tracker.viewmodel.ScholarViewModel

enum class FocusVibe(
    val displayName: String,
    val emoji: String,
    val defaultMinutes: Int,
    val description: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val quote: String
) {
    CALM("Calm", "🌿", 30, "Slow, peaceful learning. Breathe deeply and take your time.", Color(0xFF00796B), Color(0xFF4DB6AC), "Every step is progress. Enjoy the quiet moment of growth."),
    ENERGIZED("Energized", "⚡", 15, "Short, intense burst of productivity. Let's crush this!", Color(0xFFD84315), Color(0xFFFF8A65), "Pure focus, high energy. Let's make things happen!"),
    DEEP_FOCUS("Deep Focus", "🌌", 25, "Pure concentration in deep space. No distractions.", Color(0xFF1A237E), Color(0xFF5C6BC0), "Silence the noise. Your potential lies in your dedication."),
    JUST_CHILLING("Just Chilling", "🍃", 0, "No stress, no pressure. Learn at your own pace (Flowtime stopwatch).", Color(0xFF880E4F), Color(0xFFF06292), "Take it easy. Learning is a journey, not a race.")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MySpaceScreen(navController: NavController, viewModel: ScholarViewModel) {
    val context = LocalContext.current
    val isGlass = lumia.tracker.ui.theme.LocalGlassMode.current

    var selectedVibe by remember { mutableStateOf(FocusVibe.CALM) }
    var isRunning by remember { mutableStateOf(false) }
    
    // Timer state
    var timeLeftSeconds by remember { mutableStateOf(selectedVibe.defaultMinutes * 60) }
    var elapsedSeconds by remember { mutableStateOf(0) }
    var totalFocusedSeconds by remember { mutableStateOf(0) }

    // Dropdowns and selection links
    val subjects by viewModel.subjects.collectAsStateWithLifecycle(emptyList())
    val tasks by viewModel.tasks.collectAsStateWithLifecycle(emptyList())
    
    var selectedSubjectId by remember { mutableStateOf<Int?>(null) }
    var selectedTaskId by remember { mutableStateOf<Int?>(null) }
    
    var showSubjectDropdown by remember { mutableStateOf(false) }
    var showTaskDropdown by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }

    // Sync timer when vibe changes
    LaunchedEffect(selectedVibe) {
        if (!isRunning) {
            timeLeftSeconds = selectedVibe.defaultMinutes * 60
            elapsedSeconds = 0
        }
    }

    // Main ticking loop
    LaunchedEffect(isRunning, selectedVibe) {
        if (isRunning) {
            while (isRunning) {
                delay(1000)
                if (selectedVibe.defaultMinutes > 0) {
                    // Countdown mode
                    if (timeLeftSeconds > 0) {
                        timeLeftSeconds--
                        totalFocusedSeconds++
                    } else {
                        isRunning = false
                        // Auto-log session on complete
                        viewModel.addPomodoroSession(
                            durationMinutes = selectedVibe.defaultMinutes,
                            subjectId = selectedSubjectId,
                            taskId = selectedTaskId
                        )
                        showSaveDialog = true
                    }
                } else {
                    // Count up (Stopwatch / Just Chilling) mode
                    elapsedSeconds++
                    totalFocusedSeconds++
                }
            }
        }
    }

    // Smooth color animation based on vibe selection
    val animatedPrimaryColor by animateColorAsState(
        targetValue = selectedVibe.primaryColor,
        animationSpec = tween(1000),
        label = "primary_color_anim"
    )
    val animatedSecondaryColor by animateColorAsState(
        targetValue = selectedVibe.secondaryColor,
        animationSpec = tween(1000),
        label = "secondary_color_anim"
    )

    // Breathing pulse animations
    val infiniteTransition = rememberInfiniteTransition(label = "aura_pulse")
    val auraScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura_scale"
    )
    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura_alpha"
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "My Space",
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (totalFocusedSeconds >= 60) {
                            // Prompt or auto log before leaving
                            val mins = totalFocusedSeconds / 60
                            viewModel.addPomodoroSession(
                                durationMinutes = mins,
                                subjectId = selectedSubjectId,
                                taskId = selectedTaskId
                            )
                            android.widget.Toast.makeText(context, "Logged $mins mins of focus!", android.widget.Toast.LENGTH_SHORT).show()
                        }
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = "What is My Space?",
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
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            
            // 1. Serene Vibe quote / notification message
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = animatedPrimaryColor.copy(alpha = 0.08f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = selectedVibe.displayName.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = animatedPrimaryColor,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "\"${selectedVibe.quote}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }

            // 2. Main Pulsing Breathing Timer Area
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                // Soft breathing aura background
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .scale(auraScale)
                        .alpha(auraAlpha)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(animatedSecondaryColor, Color.Transparent)
                            ),
                            shape = CircleShape
                        )
                )

                // Outer border ring
                Box(
                    modifier = Modifier
                        .size(210.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp).copy(alpha = 0.75f),
                            shape = CircleShape
                        )
                        .graphicsLayer {
                            shadowElevation = 8f
                            shape = CircleShape
                            clip = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = selectedVibe.emoji,
                            fontSize = 32.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        
                        // Formatted Timer string
                        val timerStr = if (selectedVibe.defaultMinutes > 0) {
                            val mins = timeLeftSeconds / 60
                            val secs = timeLeftSeconds % 60
                            String.format("%02d:%02d", mins, secs)
                        } else {
                            val mins = elapsedSeconds / 60
                            val secs = elapsedSeconds % 60
                            String.format("%02d:%02d", mins, secs)
                        }
                        
                        Text(
                            text = timerStr,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Black,
                            color = animatedPrimaryColor,
                            letterSpacing = (-1).sp
                        )
                        
                        Text(
                            text = if (selectedVibe.defaultMinutes > 0) "focusing" else "flowtime",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // 3. Simple Dynamic Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectedVibe.defaultMinutes > 0) {
                    // Minus 5m adjustment
                    BouncyIconButton(
                        onClick = {
                            if (timeLeftSeconds > 300) {
                                timeLeftSeconds -= 300
                            } else {
                                timeLeftSeconds = 10
                            }
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.RemoveCircleOutline,
                            contentDescription = "Minus 5 Minutes",
                            tint = animatedPrimaryColor.copy(alpha = 0.8f)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }

                // Play / Pause FAB
                FloatingActionButton(
                    onClick = { isRunning = !isRunning },
                    containerColor = animatedPrimaryColor,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Start Focus",
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Reset Button
                BouncyIconButton(
                    onClick = {
                        isRunning = false
                        timeLeftSeconds = selectedVibe.defaultMinutes * 60
                        elapsedSeconds = 0
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Reset Timer",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (selectedVibe.defaultMinutes > 0) {
                    Spacer(modifier = Modifier.width(16.dp))
                    // Plus 5m adjustment
                    BouncyIconButton(
                        onClick = {
                            timeLeftSeconds += 300
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AddCircleOutline,
                            contentDescription = "Plus 5 Minutes",
                            tint = animatedPrimaryColor.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 4. Personalized Vibe Picker Pills
            Text(
                text = "Choose Vibe:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.align(Alignment.Start)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FocusVibe.values().forEach { vibe ->
                    val isSelected = selectedVibe == vibe
                    val bg = if (isSelected) animatedPrimaryColor else animatedPrimaryColor.copy(alpha = 0.08f)
                    val textCol = if (isSelected) Color.White else animatedPrimaryColor

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(bg)
                            .clickable {
                                isRunning = false
                                selectedVibe = vibe
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = vibe.emoji, fontSize = 20.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = vibe.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = textCol
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 5. Optional Connection Section (Zero distraction, highly integrated but compact)
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Link,
                                contentDescription = null,
                                tint = animatedPrimaryColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Link Focus Session (Optional)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Subject Link Pill
                        Box(modifier = Modifier.weight(1f)) {
                            val subjectLabel = selectedSubjectId?.let { sId ->
                                subjects.find { it.id == sId }?.name ?: "Subject"
                            } ?: "Subject"
                            
                            AssistChip(
                                onClick = { showSubjectDropdown = true },
                                label = { Text(subjectLabel, maxLines = 1) },
                                leadingIcon = { Icon(Icons.Rounded.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            DropdownMenu(
                                expanded = showSubjectDropdown,
                                onDismissRequest = { showSubjectDropdown = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("None (Generic Study)") },
                                    onClick = {
                                        selectedSubjectId = null
                                        showSubjectDropdown = false
                                    }
                                )
                                subjects.forEach { sub ->
                                    DropdownMenuItem(
                                        text = { Text(sub.name) },
                                        onClick = {
                                            selectedSubjectId = sub.id
                                            showSubjectDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Task Link Pill
                        Box(modifier = Modifier.weight(1f)) {
                            val taskLabel = selectedTaskId?.let { tId ->
                                tasks.find { it.id == tId }?.title ?: "Task"
                            } ?: "Task"
                            
                            AssistChip(
                                onClick = { showTaskDropdown = true },
                                label = { Text(taskLabel, maxLines = 1) },
                                leadingIcon = { Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            DropdownMenu(
                                expanded = showTaskDropdown,
                                onDismissRequest = { showTaskDropdown = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("None") },
                                    onClick = {
                                        selectedTaskId = null
                                        showTaskDropdown = false
                                    }
                                )
                                tasks.filter { !it.isCompleted }.forEach { t ->
                                    DropdownMenuItem(
                                        text = { Text(t.title) },
                                        onClick = {
                                            selectedTaskId = t.id
                                            showTaskDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 1. Sleek Info Dialog (Keeps description text out of main UI)
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = null,
                    tint = animatedPrimaryColor
                )
            },
            title = {
                Text(
                    text = "Welcome to My Space",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "My Space is a flexible, highly personalized focus companion built with zero friction in mind.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• Quick Vibes: Tap Calm, Energized, Deep Focus, or Just Chilling to instantly morph colors and time defaults on-the-fly.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• Flowtime Mode: Chilling vibe acts as a stopwatch. Work as long as you feel comfortable without timer pressure.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• No Rigid Goals: Any completed session lasting at least 1 minute is safely logged. There are no fail screens or guilt trips.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("Let's Flow", color = animatedPrimaryColor, fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }

    // 2. Beautiful Completion Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Celebration,
                    contentDescription = null,
                    tint = animatedPrimaryColor,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Focus Complete!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Fabulous work! You successfully spent ${selectedVibe.defaultMinutes} minutes in your custom focus environment. Your streak is glowing!",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                BouncyButton(
                    onClick = { showSaveDialog = false }
                ) {
                    Text("Splendid!")
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }
}
