package lumia.tracker.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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

            // Bottom bar
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

                // Wait logic: we only allow advancing to Permissions on our own, but Backup (we skip or import).
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
    val scale by animateFloatAsState(if (isActive) 1f else 0.8f, tween(600), label = "backup_scale")
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
    
    // Automatically advance if imported successfully
    LaunchedEffect(importExportStatus) {
        if (importExportStatus?.contains("successfully", ignoreCase = true) == true) {
            android.widget.Toast.makeText(context, "Backup Restored Successfully!", android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearImportExportStatus() // Make sure to reset it
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
        Icon(
            imageVector = Icons.Rounded.CloudDownload,
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                .padding(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Welcome Back?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "If you have a previous Lumia backup file (.bin or .json), you can import it now. All your courses, subjects and settings will be restored instantly.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        if (!importExportStatus.isNullOrBlank() && importExportStatus?.contains("successfully", ignoreCase = true) != true) {
            Text(
                text = importExportStatus ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = { filePickerLauncher.launch(arrayOf("*/*")) },
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Import Backup Data", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = onSkip,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("I'm a New User", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }
    }
}

@Composable
fun VisualTourPage(isActive: Boolean) {
    val scale by animateFloatAsState(if (isActive) 1f else 0.8f, tween(600), label = "tour_scale")
    val alpha by animateFloatAsState(if (isActive) 1f else 0f, tween(600), label = "tour_alpha")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .scale(scale)
            .alpha(alpha),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Explore,
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .background(MaterialTheme.colorScheme.tertiaryContainer, CircleShape)
                .padding(20.dp),
            tint = MaterialTheme.colorScheme.tertiary
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Your setup is complete!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TourItem(icon = Icons.Rounded.Dashboard, title = "Home Dashboard", desc = "Overview of your day and next classes")
                TourItem(icon = Icons.Rounded.MenuBook, title = "Courses & Subjects", desc = "Manage your studying material and schedule")
                TourItem(icon = Icons.Rounded.Timer, title = "Pomodoro Timer", desc = "Focus and track your study sessions")
                TourItem(icon = Icons.Rounded.Settings, title = "Settings", desc = "Customize aesthetics and advanced toggles")
            }
        }
    }
}

@Composable
fun TourItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, desc: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

