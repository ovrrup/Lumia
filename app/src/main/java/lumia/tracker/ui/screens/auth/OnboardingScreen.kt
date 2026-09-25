package lumia.tracker.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import lumia.tracker.viewmodel.ScholarViewModel
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import lumia.tracker.ui.components.BouncyButton
import lumia.tracker.ui.components.GlassCapsule
import lumia.tracker.ui.components.ScholarCard
import lumia.tracker.ui.components.ScholarCardDefaults

@Composable
fun OnboardingScreen(navController: NavController, viewModel: ScholarViewModel) {
    // Sequence: Intro1, Intro2, Permissions, Backup, Setup Profile, Tour
    val pagerState = rememberPagerState(pageCount = { 6 })
    val coroutineScope = rememberCoroutineScope()
    
    // First user setup temporary state variables
    var firstProfileName by remember { mutableStateOf("Main User") }
    var firstProfileAlias by remember { mutableStateOf("Student") }
    var firstProfileTheme by remember { mutableStateOf("Ocean") }
    var firstProfileAvatar by remember { mutableStateOf("") }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                userScrollEnabled = false
            ) { page ->
                when (page) {
                    0 -> OnboardingPage(
                        icon = Icons.Rounded.AutoAwesome,
                        title = "Personalization & Style",
                        description = "Personalize themes, streamline focus sessions, and organize academic workspaces.",
                        isActive = pagerState.currentPage == page
                    )
                    1 -> OnboardingPage(
                        icon = Icons.Rounded.Timer,
                        title = "Focus & Organization",
                        description = "Integrated Pomodoro timer, syllabus tracking, and offline data privacy.",
                        isActive = pagerState.currentPage == page
                    )
                    2 -> PermissionsPage(
                        isActive = pagerState.currentPage == page,
                        onComplete = {
                            coroutineScope.launch { pagerState.animateScrollToPage(3) }
                        }
                    )
                    3 -> BackupOptionPage(
                        isActive = pagerState.currentPage == page,
                        viewModel = viewModel,
                        onBackupImported = {
                            coroutineScope.launch { pagerState.animateScrollToPage(4) }
                        },
                        onSkip = {
                            coroutineScope.launch { pagerState.animateScrollToPage(4) }
                        }
                    )
                    4 -> ProfileSetupPage(
                        isActive = pagerState.currentPage == page,
                        viewModel = viewModel,
                        onSaved = { name, alias, theme, avatar ->
                            firstProfileName = name
                            firstProfileAlias = alias
                            firstProfileTheme = theme
                            firstProfileAvatar = avatar
                        }
                    )
                    5 -> VisualTourPage(
                        isActive = pagerState.currentPage == page
                    )
                }
            }

            // Modern Bottom Navigation Bar with Capsule Elements
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Modern Pager Indicator Capsule Pill
                GlassCapsule(
                    modifier = Modifier.padding(vertical = 4.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(6) { index ->
                            val isSelected = pagerState.currentPage == index
                            val width by animateDpAsState(
                                targetValue = if (isSelected) 24.dp else 8.dp,
                                animationSpec = tween(300),
                                label = "indicator_width"
                            )
                            val color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            }
                            Box(
                                modifier = Modifier
                                    .height(8.dp)
                                    .width(width)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                        }
                    }
                }

                // Action Button in CircleShape Capsule Pill
                val isButtonVisible = pagerState.currentPage in listOf(0, 1, 2, 4, 5)
                
                if (isButtonVisible) {
                    BouncyButton(
                        onClick = {
                            if (pagerState.currentPage == 4) {
                                viewModel.setupFirstProfile(
                                    name = firstProfileName.ifBlank { "Main User" },
                                    alias = firstProfileAlias.ifBlank { "Student" },
                                    avatar = firstProfileAvatar,
                                    starterTheme = firstProfileTheme
                                )
                                coroutineScope.launch { pagerState.animateScrollToPage(5) }
                            } else if (pagerState.currentPage == 5) {
                                viewModel.completeOnboarding()
                                navController.navigate("dashboard") {
                                    popUpTo("onboarding") { inclusive = true }
                                }
                            } else {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = if (pagerState.currentPage == 5) "Let's Go!" else "Next",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = if (pagerState.currentPage == 5) Icons.Rounded.RocketLaunch else Icons.Rounded.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(64.dp))
                }
            }
        }
    }
}

@Composable
fun BackupOptionPage(
    isActive: Boolean,
    viewModel: ScholarViewModel,
    onBackupImported: () -> Unit,
    onSkip: () -> Unit
) {
    val scale by animateFloatAsState(if (isActive) 1f else 0.85f, tween(600), label = "backup_scale")
    val alpha by animateFloatAsState(if (isActive) 1f else 0f, tween(600), label = "backup_alpha")
    val context = LocalContext.current
    
    val importExportStatus by viewModel.importExportStatus.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                viewModel.importData(uri)
            }
        }
    )
    
    LaunchedEffect(importExportStatus) {
        if (importExportStatus?.contains("successfully", ignoreCase = true) == true) {
            android.widget.Toast.makeText(context, "Backup Restored Successfully!", android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearImportExportStatus() // Reset state
            onBackupImported()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .scale(scale)
            .alpha(alpha)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.SettingsBackupRestore,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Data & Workspace",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (!importExportStatus.isNullOrBlank() && importExportStatus?.contains("successfully", ignoreCase = true) != true) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = CircleShape
            ) {
                Text(
                    text = importExportStatus ?: "",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        val isDark = isSystemInDarkTheme()

        // Modern Glassmorphic Choice Card: Import Backup
        ScholarCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isDark) 0.2f else 0.3f),
            border = ScholarCardDefaults.glassBorder(isDark, accentColor = MaterialTheme.colorScheme.primary),
            onClick = { filePickerLauncher.launch(arrayOf("*/*")) }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular icon ring
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FolderOpen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Import Local Backup",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Restore courses, profiles, and logs from backup file.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                GlassCapsule(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = "Import",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Modern Glassmorphic Choice Card: New Profile
        ScholarCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            containerColor = if (isDark) MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
            border = ScholarCardDefaults.glassBorder(isDark, accentColor = MaterialTheme.colorScheme.tertiary),
            onClick = onSkip
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular icon ring
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f))
                        .border(2.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f), CircleShape)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "I'm a New User",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Create a custom profile and configure academic goals.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                GlassCapsule(
                    containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = "Fresh",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun VisualTourPage(isActive: Boolean) {
    val scale by animateFloatAsState(if (isActive) 1f else 0.85f, tween(600), label = "tour_scale")
    val alpha by animateFloatAsState(if (isActive) 1f else 0f, tween(600), label = "tour_alpha")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .scale(scale)
            .alpha(alpha)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.tertiaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Explore,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.tertiary
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Setup Complete!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Visual Tour Cards with Glassmorphism and Circular Rings
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            TourItemCard(
                icon = Icons.Rounded.Dashboard,
                title = "Dashboard",
                desc = "Track schedules, daily streaks, and task progress.",
                tint = MaterialTheme.colorScheme.primary
            )
            TourItemCard(
                icon = Icons.Rounded.MenuBook,
                title = "Academics",
                desc = "Organize courses, syllabus topics, and assignments.",
                tint = MaterialTheme.colorScheme.secondary
            )
            TourItemCard(
                icon = Icons.Rounded.Timer,
                title = "Focus Timer",
                desc = "Deep study sessions with Pomodoro and analytics.",
                tint = MaterialTheme.colorScheme.tertiary
            )
            TourItemCard(
                icon = Icons.Rounded.Settings,
                title = "Settings",
                desc = "Personalize theme accents, layouts, and display modes.",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TourItemCard(icon: ImageVector, title: String, desc: String, tint: Color) {
    val isDark = isSystemInDarkTheme()
    ScholarCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        containerColor = if (isDark) MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
        border = ScholarCardDefaults.glassBorder(isDark, accentColor = tint)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular avatar/icon ring
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.12f))
                    .border(1.5.dp, tint.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
