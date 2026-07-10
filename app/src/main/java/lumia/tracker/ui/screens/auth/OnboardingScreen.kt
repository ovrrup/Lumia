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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState

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
                        title = "Glassmorphism & Style",
                        description = "Experience Lumia's distinctive Frosted Glass layouts! Custom theme overlays, floating adaptive navigation rails, and tactile actions elevate your routine.",
                        isActive = pagerState.currentPage == page
                    )
                    1 -> OnboardingPage(
                        icon = Icons.Rounded.Timer,
                        title = "Basic Focus Timer",
                        description = "Our classic Pomodoro study space and course organizers are 100% free with local offline data security.",
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

            // Bottom navigation bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Page Indicators
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(6) { index ->
                        val isSelected = pagerState.currentPage == index
                        val width by animateDpAsState(if (isSelected) 24.dp else 8.dp, label = "indicator_width")
                        val color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(width)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }

                // Check general button visibility
                val isButtonVisible = pagerState.currentPage in listOf(0, 1, 2, 4, 5)
                
                if (isButtonVisible) {
                    Button(
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
                        shape = RoundedCornerShape(24.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
                    ) {
                        Text(if (pagerState.currentPage == 5) "Let's Go!" else "Next", fontWeight = FontWeight.Bold)
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
            text = "Welcome Back?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Restore academic data from a previous backup file, or start fresh with a clean profile setup.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (!importExportStatus.isNullOrBlank() && importExportStatus?.contains("successfully", ignoreCase = true) != true) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = importExportStatus ?: "",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Two beautifully styled choice cards for representation
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            ),
            onClick = { filePickerLauncher.launch(arrayOf("*/*")) }
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
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
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Import Local Backup",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Restore all courses, settings, profiles, and logs instantly via your .bin or .json file.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
            ),
            onClick = onSkip
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
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
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "I'm a New User",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Create a beautiful custom student profile and start tracking your academic journey from scratch.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
        
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Lumia is optimized and ready. Explore the dynamic screens inside your cockpit:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Visual Tour Cards in Grid or beautiful rows
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            TourItemCard(
                icon = Icons.Rounded.Dashboard,
                title = "Home Dashboard",
                desc = "Track schedules, daily streaks, task progress, and live indicators.",
                tint = MaterialTheme.colorScheme.primary
            )
            TourItemCard(
                icon = Icons.Rounded.MenuBook,
                title = "Courses & Subjects",
                desc = "Organize classes, calculate grade analytics, and manage coursework.",
                tint = MaterialTheme.colorScheme.secondary
            )
            TourItemCard(
                icon = Icons.Rounded.Timer,
                title = "Pomodoro Timer",
                desc = "Engage in deep focused study sessions with integrated statistics.",
                tint = MaterialTheme.colorScheme.tertiary
            )
            TourItemCard(
                icon = Icons.Rounded.Settings,
                title = "Aesthetic Settings",
                desc = "Customize glass transparency, accent themes, and widgets easily.",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TourItemCard(icon: ImageVector, title: String, desc: String, tint: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.12f)),
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
