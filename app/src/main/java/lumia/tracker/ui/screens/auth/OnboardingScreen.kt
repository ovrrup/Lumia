package lumia.tracker.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Leaderboard
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Token
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
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import lumia.tracker.viewmodel.ScholarViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(navController: NavController, viewModel: ScholarViewModel) {
    val pagerState = rememberPagerState(pageCount = { 6 })
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var isLegalAgreed by remember { mutableStateOf(false) }

    // First user setup temporary state variables
    var firstProfileName by remember { mutableStateOf("Main User") }
    var firstProfileAlias by remember { mutableStateOf("Student") }
    var firstProfileTheme by remember { mutableStateOf("Ocean") }
    var firstProfileAvatar by remember { mutableStateOf("") }
    var firstProfileGamificationEnabled by remember { mutableStateOf(true) }

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
                    .fillMaxWidth()
            ) { page ->
                when (page) {
                    0 -> LegalConsentPage(
                        isActive = pagerState.currentPage == page,
                        isAgreed = isLegalAgreed,
                        onAgreedChange = { isLegalAgreed = it }
                    )
                    1 -> OnboardingPage(
                        icon = Icons.Rounded.AutoAwesome,
                        title = "Glassmorphism & Style",
                        description = "Experience Lumia's distinctive Frosted Glass layouts! Custom theme overlays, floating adaptive navigation rails, and bouncy physics tactile actions elevate your everyday routine.",
                        isActive = pagerState.currentPage == page
                    )
                    2 -> OnboardingPage(
                        icon = Icons.Rounded.Timer,
                        title = "Basic Focus Timer (Free)",
                        description = "Our classic Pomodoro study space and course organizers are 100% free with local offline data security. Complete sessions to earn points and rank up!",
                        isActive = pagerState.currentPage == page
                    )
                    3 -> OnboardingPage(
                        icon = Icons.Rounded.Token,
                        title = "Dual Currency Plus Economy",
                        description = "Manage Focus Points & Credits with an active conversion! Spin the Lucky Chest for premium Grade-SS drops, rent features for 24-hour periods, or unlock custom layouts and database diagnostics permanently.",
                        isActive = pagerState.currentPage == page
                    )
                    4 -> ProfileSetupPage(
                        isActive = pagerState.currentPage == page,
                        onSaved = { name, alias, theme, avatar, gamification ->
                            firstProfileName = name
                            firstProfileAlias = alias
                            firstProfileTheme = theme
                            firstProfileAvatar = avatar
                            firstProfileGamificationEnabled = gamification
                        }
                    )
                    5 -> PermissionsPage(
                        isActive = pagerState.currentPage == page,
                        onComplete = {
                            // Ensure setup is applied when completing
                            viewModel.setupFirstProfile(
                                name = firstProfileName.ifBlank { "Main User" },
                                alias = firstProfileAlias.ifBlank { "Student" },
                                avatar = firstProfileAvatar,
                                starterTheme = firstProfileTheme,
                                gamificationEnabled = firstProfileGamificationEnabled
                            )
                            viewModel.completeOnboarding()
                            navController.navigate("dashboard") {
                                popUpTo("onboarding") { inclusive = true }
                            }
                        }
                    )
                }
            }

            // Bottom Navigation Bar
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

                // Next / Get Started Button
                Button(
                    onClick = {
                        if (pagerState.currentPage < 5) {
                            if (pagerState.currentPage == 4) {
                                // Save profile setups right when advancing from profile page
                                viewModel.setupFirstProfile(
                                    name = firstProfileName.ifBlank { "Main User" },
                                    alias = firstProfileAlias.ifBlank { "Student" },
                                    avatar = firstProfileAvatar,
                                    starterTheme = firstProfileTheme,
                                    gamificationEnabled = firstProfileGamificationEnabled
                                )
                            }
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            viewModel.setupFirstProfile(
                                name = firstProfileName.ifBlank { "Main User" },
                                alias = firstProfileAlias.ifBlank { "Student" },
                                avatar = firstProfileAvatar,
                                starterTheme = firstProfileTheme,
                                gamificationEnabled = firstProfileGamificationEnabled
                            )
                            viewModel.completeOnboarding()
                            navController.navigate("dashboard") {
                                popUpTo("onboarding") { inclusive = true }
                            }
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
                    enabled = pagerState.currentPage != 0 || isLegalAgreed
                ) {
                    Text(if (pagerState.currentPage == 5) "Finish" else "Next", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
