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
import lumia.tracker.ui.screens.auth.components.*
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
